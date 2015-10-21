import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by anon on 21/10/2015.
 */
public class MessageBroker {
    public static final int PUBLISHER_PORT = 8079;
    public static final int SUBCRIBER_PORT = 8078;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBroker.class);

    private static Collection<SocketChannel> publishers = new ConcurrentLinkedQueue<SocketChannel>();
    private static Collection<Socket> subscribers = new ConcurrentLinkedQueue<Socket>();

    private ServerSocketChannel serverSocketChannel;

    public MessageBroker(){
        try{
            int[] ports = {PUBLISHER_PORT, SUBCRIBER_PORT};
            Selector selector = Selector.open();

            //for(int port : ports){
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", 8079));
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //}

            while(true) {
                selector.select();

                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey selectedKey = selectedKeys.next();

                    if (selectedKey.isAcceptable()) {
                        SocketChannel socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
                        socketChannel.configureBlocking(false);
                        Socket socket = socketChannel.socket();

                        switch (socket.getLocalPort()) {
                            case PUBLISHER_PORT:
                                //TODO handle publish
                                    sendToSubscribers(socketChannel);
                                    publishers.add(socketChannel);
                                break;
                            case SUBCRIBER_PORT:
                                //TODO handle subscribe
                                break;
                        }
                    } else if (selectedKey.isReadable()) {
                        sendToSubscribers((SocketChannel)publishers.toArray()[0]);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to open and listen on both sockets", e);       //TODO This is too coarse.  Add separate try/catch blocks
            System.exit(-1);
        }
    }

    public void sendToSubscribers(SocketChannel socketChannel){

    }

}
