import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Jonathan Sterling on 21/10/2015.
 */
public class Subscriber {

    private static Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {           //TODO Try/catch properly
        while (true) {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(ServerSettings.BROKER_HOSTNAME, ServerSettings.SUBSCRIBER_PORT));

            String message = "";
            ByteBuffer byteBuffer = ByteBuffer.allocate(512);

            while (socketChannel.isConnectionPending()) {
                Thread.sleep(100);
            }

            while (byteBuffer.hasRemaining()) {
                int bytesRead = socketChannel.read(byteBuffer);
                if (bytesRead == -1) {
                    socketChannel.close();
                    continue;
                }

                byteBuffer.flip();
                Charset charset = Charset.forName(ServerSettings.DEFAULT_CHARSET);
                CharsetDecoder decoder = charset.newDecoder();

                message = decoder.decode(byteBuffer).toString();
            }

            LOGGER.info("Message received: '" + message + "'");

            try {
                ObjectMapper mapper = new ObjectMapper();
                CustomObject object = mapper.readValue(message, CustomObject.class);

                LOGGER.info("Converted message to CustomObject with message: '" + object.getMessage() + "'\n" +
                            "And UUID: '" + object.getId() + "'");
            } catch (JsonMappingException e){
                LOGGER.error("Failed to decode object...", e);
            }
        }
    }
}
