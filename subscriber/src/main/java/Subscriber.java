import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by anon on 21/10/2015.
 */
public class Subscriber {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8078));

        String message = "Unmodified";
        ByteBuffer byteBuffer = ByteBuffer.allocate(512);

        while(socketChannel.isConnectionPending()){
            Thread.sleep(100);
        }

        while(byteBuffer.hasRemaining()){
            int bytesRead = socketChannel.read(byteBuffer);
            if(bytesRead == -1){
                socketChannel.close();
                continue;
            }

            byteBuffer.flip();
            Charset charset = Charset.forName("ISO-8859-1");
            CharsetDecoder decoder = charset.newDecoder();

            message = decoder.decode(byteBuffer).toString();
        }

            System.out.println("String is: '" + message + "'");

    }
}
