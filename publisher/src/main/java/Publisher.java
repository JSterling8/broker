import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.UUID;

/**
 * Created by Jonathan Sterling on 21/10/2015.
 */
public class Publisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);

    private SocketChannel socketChannel;

    public void connect(){
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(ServerSettings.BROKER_HOSTNAME, ServerSettings.PUBLISHER_PORT));

            while(socketChannel.isConnectionPending()){
                    Thread.sleep(50);
            }
        } catch (IOException e1) {
            LOGGER.error("Failed to connect to message broker");
        } catch (InterruptedException e){
            LOGGER.error("Connection pending sleep was interrupted.", e);
        }
    }

    public void sendCustomObject(CustomObject customObject){
        CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonEncodedMessage = objectMapper.writeValueAsString(customObject);
            MessageWrapper messageWrapper = new MessageWrapper("CustomObject", jsonEncodedMessage);
            String jsonEncodedMessageWrapper = objectMapper.writeValueAsString(messageWrapper);

            socketChannel.write(encoder.encode(CharBuffer.wrap(jsonEncodedMessageWrapper)));

            LOGGER.debug("Sending CustomObject with message: " + customObject.getMessage() +
                    "\nAnd UUID: " + customObject.getId());

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to json message", e);
        } catch (IOException e) {
            LOGGER.error("Failed to send json message");
        }
    }

    public void sendCustomObject2(CustomObject2 customObject2){
        CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonEncodedMessage = objectMapper.writeValueAsString(customObject2);
            MessageWrapper messageWrapper = new MessageWrapper("CustomObject", jsonEncodedMessage);
            String jsonEncodedMessageWrapper = objectMapper.writeValueAsString(messageWrapper);

            socketChannel.write(encoder.encode(CharBuffer.wrap(jsonEncodedMessageWrapper)));

            LOGGER.debug("Sending CustomObject with message: " + customObject2.getMessage() +
                    "\nAnd UUID: " + customObject2.getId() +
                    "\nAnd aThirdField: " + customObject2.getaThirdField());

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to json message", e);
        } catch (IOException e) {
            LOGGER.error("Failed to send json message");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Publisher publisher = new Publisher();
        publisher.connect();

        CustomObject customObject = new CustomObject("A test publisher message",
                new UUID(System.currentTimeMillis(), System.currentTimeMillis() - 41134234l));
        publisher.sendCustomObject(customObject);

        Thread.sleep(200);

        CustomObject2 customObject2 = new CustomObject2("A test publisher message",
                new UUID(System.currentTimeMillis(), System.currentTimeMillis() - 41134234l),
                "Now we're cooking with gas!");
        publisher.sendCustomObject2(customObject2);
    }
}
