import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.UUID;

/**
 * Created by anon on 21/10/2015.
 */
public class Publisher {

    public static final String BROKER_HOST = "127.0.0.1";
    public static final int BROKER_PUBLISHER_PORT = 8079;

    public void connect(){
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(BROKER_HOST, BROKER_PUBLISHER_PORT));

            while(socketChannel.isConnectionPending()){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();

            CustomObject customObject = new CustomObject("A test publisher message",
                    new UUID(System.currentTimeMillis(), System.currentTimeMillis() - 41134234l));

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonEncodedObject = objectMapper.writeValueAsString(customObject);

            socketChannel.write(encoder.encode(CharBuffer.wrap(jsonEncodedObject)));

            while(true){
                // Stay open... Do some stuff?
            }

        } catch (CharacterCodingException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args){
        Publisher main = new Publisher();
        main.connect();
    }

}
