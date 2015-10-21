import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Created by anon on 21/10/2015.
 */
public class Publisher {
    public void connect(){
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8079));

            while(socketChannel.isConnectionPending()){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();

            socketChannel.write(encoder.encode(CharBuffer.wrap("A publisher message")));
            Thread.sleep(5000);
        } catch (CharacterCodingException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Publisher main = new Publisher();
        main.connect();
    }

}
