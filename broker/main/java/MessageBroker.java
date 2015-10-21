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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by anon on 21/10/2015.
 *
 */
public class MessageBroker {
    public static final int PUBLISHER_PORT = 8079;
    public static final int SUBCRIBER_PORT = 8078;
    public static final String HOSTNAME = "127.0.0.1";

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBroker.class);
    private List<SocketChannel> socketChannels = new ArrayList<SocketChannel>();
    private List<String> pendingMessages = new ArrayList<String>();

    public MessageBroker(){
        try{
            Selector selector = Selector.open();

            int[] ports = {PUBLISHER_PORT, SUBCRIBER_PORT};

            ServerSocketChannel publisherServerSocketChannel = ServerSocketChannel.open();
            publisherServerSocketChannel.configureBlocking(false);
            publisherServerSocketChannel.socket().bind(new InetSocketAddress(HOSTNAME, PUBLISHER_PORT));
            publisherServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ServerSocketChannel subscriberServerSocketChannel = ServerSocketChannel.open();
            subscriberServerSocketChannel.configureBlocking(false);
            subscriberServerSocketChannel.socket().bind(new InetSocketAddress(HOSTNAME, SUBCRIBER_PORT));
            subscriberServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while(true) {
                selector.select();

                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey selectedKey = selectedKeys.next();

                    if (selectedKey.isAcceptable()) {
                        SocketChannel socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
                        if(socketChannel != null) {
                            socketChannel.configureBlocking(false);
                            Socket socket = socketChannel.socket();

                            if(socket.getLocalPort() == PUBLISHER_PORT){
                                ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                                String message = null;

                                while(byteBuffer.hasRemaining()){
                                    try {
                                        Thread.sleep(500);                      // FIXME - Find way to get message without sleeping...
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
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

                                if(StringUtils.isNotBlank(message)){
                                    sendToSubscribers(message);
                                }

                                break;
                            } else {
                                socketChannels.add(socketChannel);
                                if(pendingMessages.size() > 0){
                                    Iterator iterator = pendingMessages.iterator();

                                    while(iterator.hasNext()){
                                        String message = (String) iterator.next();
                                        sendToSubscribers(message);
                                        iterator.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to open and listen on both sockets", e);       //TODO This is too coarse.  Add separate try/catch blocks
            System.exit(-1);
        }
    }

    public void sendToSubscribers(String message){
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
