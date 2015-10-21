import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by anon on 21/10/2015.
 *
 */
public class MessageBroker {
    public static final int PUBLISHER_PORT = 8079;
    public static final int SUBCRIBER_PORT = 8078;
    public static final String HOSTNAME = "127.0.0.1";

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBroker.class);
    private ConcurrentLinkedQueue<SocketChannel> socketChannels = new ConcurrentLinkedQueue<SocketChannel>();
    private ConcurrentLinkedQueue<String> pendingMessages = new ConcurrentLinkedQueue<String>();

    public MessageBroker(){
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            LOGGER.error("Failed to open Selector", e);
        }

        int[] ports = {PUBLISHER_PORT, SUBCRIBER_PORT};

        configureServerSocketChannels(selector);

        while(true) {
            try {
                selector.select();
            } catch (IOException e) {
                LOGGER.error("Failed to get set of keys from Selector", e);
                System.exit(-1);                                                // TODO Maybe let it fail a set amount of times before exiting?
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey selectedKey = selectedKeys.next();

                if (selectedKey.isAcceptable()) {
                    SocketChannel socketChannel = null;
                    try {
                        socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
                    } catch (IOException e) {
                        LOGGER.error("Failed to accept ServerSocketChannel connection.", e);
                    }
                    if(socketChannel != null) {
                        try {
                            socketChannel.configureBlocking(false);
                        } catch (IOException e) {
                            LOGGER.error("Failed to configure blocking on SocketChannel", e);
                        }
                        Socket socket = socketChannel.socket();

                        if(socket.getLocalPort() == PUBLISHER_PORT){
                            ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                            String message = null;

                            message = getMessageFromPublisher(socketChannel, byteBuffer, message);

                            if(StringUtils.isNotBlank(message)){
                                sendMessageToSubscribers(message);
                            }

                            break;
                        } else {
                            socketChannels.add(socketChannel);
                            if(pendingMessages.size() > 0){
                                Iterator iterator = pendingMessages.iterator();
                                while(iterator.hasNext()){
                                    String message = (String) iterator.next();
                                    sendMessageToSubscribers(message);
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private String getMessageFromPublisher(SocketChannel socketChannel, ByteBuffer byteBuffer, String message) {
        while(byteBuffer.hasRemaining()){
            try {
                Thread.sleep(500);                      // FIXME - Find way to get full message without sleeping...
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int bytesRead = 0;
            try {
                bytesRead = socketChannel.read(byteBuffer);
            } catch (IOException e) {
                LOGGER.error("Failed to read byteBuffer", e);
            }
            if(bytesRead == -1){
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close SocketChannel");
                }
                continue;
            }

            byteBuffer.flip();

            message = decodeMessage(byteBuffer, message);
        }
        return message;
    }

    private void configureServerSocketChannels(Selector selector) {
        ServerSocketChannel publisherServerSocketChannel = null;
        try {
            publisherServerSocketChannel = ServerSocketChannel.open();
            publisherServerSocketChannel.configureBlocking(false);
            publisherServerSocketChannel.socket().bind(new InetSocketAddress(HOSTNAME, PUBLISHER_PORT));
            publisherServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            LOGGER.error("Failed to open publisher ServerSocketChannel", e);
        }

        try {
            ServerSocketChannel subscriberServerSocketChannel = ServerSocketChannel.open();
            subscriberServerSocketChannel.configureBlocking(false);
            subscriberServerSocketChannel.socket().bind(new InetSocketAddress(HOSTNAME, SUBCRIBER_PORT));
            subscriberServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            LOGGER.error("Failed to open subscriber ServerSocketChannel", e);
        }
    }

    private String decodeMessage(ByteBuffer byteBuffer, String message) {
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        try {
            message = decoder.decode(byteBuffer).toString();
        } catch (CharacterCodingException e) {
            LOGGER.error("Failed to decode message from producer", e);
        }
        return message;
    }

    public void sendMessageToSubscribers(String message){
        CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();

        if(socketChannels.size() > 0) {
            Iterator<SocketChannel> iterator = socketChannels.iterator();
            while (iterator.hasNext()) {
                try {
                    SocketChannel socketChannel = iterator.next();
                    socketChannel.write(encoder.encode(CharBuffer.wrap(message)));
                } catch (IOException e) {
                    LOGGER.info("Failed to write to subscriber.  Removing subscriber from List of subscribers.");
                    iterator.remove();
                }
            }
        } else {
            pendingMessages.add(message);
        }

    }

    public static void main(String[] args){
        MessageBroker broker = new MessageBroker();
    }
}
