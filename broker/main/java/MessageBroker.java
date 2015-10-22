import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Jonathan Sterling on 21/10/2015.
 *
 */
public class MessageBroker {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBroker.class);

    private ConcurrentLinkedQueue<SubscriberSession> subscriberSessions = new ConcurrentLinkedQueue<SubscriberSession>();
    private ConcurrentLinkedQueue<String> pendingMessages = new ConcurrentLinkedQueue<String>();

    private HashMap<SelectionKey, PublisherSession> publisherMap = new HashMap<SelectionKey, PublisherSession>();

    public MessageBroker(){}

    public void startBrokerServer() {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            LOGGER.error("Failed to open Selector", e);
        }

        configureServerSocketChannels(selector);

        while(true) {
            try {
                selector.selectNow();
            } catch (IOException e) {
                LOGGER.error("Failed to get set of keys from Selector", e);
                System.exit(-1);
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey selectedKey = selectedKeys.next();

                if (selectedKey.isAcceptable()) {
                    SocketChannel socketChannel = null;
                    try {
                        socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
                    } catch (IOException e) {
                        LOGGER.error("Failed to accept ServerSocketChannel connection", e);
                    }
                    if(socketChannel != null) {
                        try {
                            socketChannel.configureBlocking(false);
                        } catch (IOException e) {
                            LOGGER.error("Failed to configure blocking on SocketChannel", e);
                        }

                        Socket socket = socketChannel.socket();

                        if(socket.getLocalPort() == ServerSettings.PUBLISHER_PORT){
                            try {
                                SelectionKey readKey = socketChannel.register(selector, SelectionKey.OP_READ);
                                publisherMap.put(readKey, new PublisherSession(readKey, socketChannel));
                            } catch (ClosedChannelException e) {
                                LOGGER.error("Failed to attach read selector to publisher");
                            }
                        } else {
                            subscriberSessions.add(new SubscriberSession(selectedKey, socketChannel));
                            sendMessageBacklog();
                        }
                    }
                } else if (selectedKey.isReadable()) {
                    PublisherSession publisherSession = publisherMap.get(selectedKey);
                    if(publisherSession == null){
                        continue;
                    }
                    String message = publisherSession.read();

                    if(StringUtils.isNotBlank(message)){
                        sendMessageToSubscribers(message);
                    }
                }
            }
        }
    }

    private void sendMessageBacklog() {
        if(pendingMessages.size() > 0){
            Iterator iterator = pendingMessages.iterator();
            while(iterator.hasNext()){
                String message = (String) iterator.next();
                sendMessageToSubscribers(message);
                iterator.remove();
            }
        }
    }

    private void configureServerSocketChannels(Selector selector) {
        ServerSocketChannel publisherServerSocketChannel = null;
        try {
            publisherServerSocketChannel = ServerSocketChannel.open();
            publisherServerSocketChannel.configureBlocking(false);
            publisherServerSocketChannel.socket().bind(new InetSocketAddress(ServerSettings.BROKER_HOSTNAME, ServerSettings.PUBLISHER_PORT));
            publisherServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            LOGGER.error("Failed to open publisher ServerSocketChannel", e);
        }

        try {
            ServerSocketChannel subscriberServerSocketChannel = ServerSocketChannel.open();
            subscriberServerSocketChannel.configureBlocking(false);
            subscriberServerSocketChannel.socket().bind(new InetSocketAddress(ServerSettings.BROKER_HOSTNAME, ServerSettings.SUBSCRIBER_PORT));
            subscriberServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            LOGGER.error("Failed to open subscriber ServerSocketChannel", e);
        }
    }

    private void sendMessageToSubscribers(String message){
        CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();
        int messagesSent = 0;

        Iterator<SubscriberSession> iterator = subscriberSessions.iterator();
        while(iterator.hasNext()){
            SubscriberSession subscriberSession = iterator.next();
            if(subscriberSession.write(message)){
                messagesSent++;
            } else {
                iterator.remove();
            }
        }

        if(messagesSent == 0 ){
            pendingMessages.add(message);
        }

    }

    public static void main(String[] args){
        MessageBroker broker = new MessageBroker();
        broker.startBrokerServer();
    }
}
