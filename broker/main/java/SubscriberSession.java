import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by Jonathan Sterling on 22/10/2015.
 */
public class SubscriberSession {
    private Logger LOGGER = LoggerFactory.getLogger(PublisherSession.class);

    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private ByteBuffer buffer;

    public SubscriberSession(SelectionKey selectionKey, SocketChannel socketChannel){
        this.selectionKey = selectionKey;
        this.socketChannel = socketChannel;
    }

    public void disconnect(){
        try{
            if(selectionKey != null){
                selectionKey.cancel();
            }

            if(socketChannel != null){
                socketChannel.close();
            }
        } catch (IOException e) {
            // Ignore
            LOGGER.info("Failed to close channel and/or cancel key.", e);
        }
    }

    public void write() {
        try {
            // write
        } catch (Exception e){
            disconnect();
            LOGGER.error("Failed to write to a subscriber channel", e);
        }
    }
}