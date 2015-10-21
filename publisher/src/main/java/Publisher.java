import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.UUID;

/**
 * Created by Jonathan Sterling on 21/10/2015.
 */
public class Publisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);

    public void connect(){
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(ServerSettings.BROKER_HOSTNAME, ServerSettings.PUBLISHER_PORT));

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

            LOGGER.debug("Sending CustomObject with message: " + customObject.getMessage() +
                            "\nAnd UUID: " + customObject.getId());
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
