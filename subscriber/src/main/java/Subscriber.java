import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Jonathan Sterling on 21/10/2015.
 */
public class Subscriber {
    private static Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);

    private SocketChannel socketChannel;

    public Subscriber(){}

    public void connect(){
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(ServerSettings.BROKER_HOSTNAME, ServerSettings.SUBSCRIBER_PORT));
        } catch (IOException e) {
            LOGGER.error("Failed to connect to broker...", e);
        }
    }

    public void listen(){
        while (true) {
            String message = "";
            ByteBuffer byteBuffer = ByteBuffer.allocate(512);

            while (socketChannel.isConnectionPending()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    LOGGER.error("Connection pending sleep was interrupted", e);
                }
            }

            while (byteBuffer.hasRemaining()) {
                int bytesRead = 0;
                try {
                    bytesRead = socketChannel.read(byteBuffer);
                } catch (IOException e) {
                    LOGGER.error("Failed to read from channel");
                }
                if (bytesRead == -1) {
                    continue;
                }

                byteBuffer.flip();
                Charset charset = Charset.forName(ServerSettings.DEFAULT_CHARSET);
                CharsetDecoder decoder = charset.newDecoder();

                try {
                    message = decoder.decode(byteBuffer).toString();
                } catch (CharacterCodingException e) {
                    LOGGER.error("Failed to decode message from server...", e);
                }
            }

            LOGGER.info("Message received: '" + message + "'");

            try {
                ObjectMapper mapper = new ObjectMapper();
                CustomObject object = mapper.readValue(message, CustomObject.class);

                LOGGER.info("Converted message to CustomObject with message: '" + object.getMessage() + "'\n" +
                        "And UUID: '" + object.getId() + "'");
            } catch (JsonMappingException e){
                LOGGER.error("Failed to decode object...", e);
            } catch (JsonParseException e) {
                LOGGER.error("Failed to decode object...", e);
            } catch (IOException e) {
                LOGGER.error("Failed to decode object...", e);
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Subscriber subscriber = new Subscriber();
        subscriber.connect();
        subscriber.listen();
    }
}
