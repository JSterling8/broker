import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Created by Jonathan Sterling on 22/10/2015.
 */
public class SubscriberSession {
    private final CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();

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

    public boolean write(String message) {
        try {
            socketChannel.write(encoder.encode(CharBuffer.wrap(message)));
            return true;
        } catch (Exception e){
            disconnect();
            LOGGER.error("Failed to write to a subscriber channel", e);
            return false;
        }
    }
}