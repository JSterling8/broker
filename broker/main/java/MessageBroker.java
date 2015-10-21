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
import java.util.Iterator;

/**
 * Created by anon on 21/10/2015.
 *
 */
public class MessageBroker {
    public static final int PUBLISHER_PORT = 8079;
    public static final int SUBCRIBER_PORT = 8078;
    public static final String HOSTNAME = "127.0.0.1";

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBroker.class);
    private SocketChannel subscriberChannel;

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
                                String message = "";

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

                                if(StringUtils.isNotBlank(message)){
                                    System.out.println(message);

                                    sendToSubscribers(message);
                                }

                                break;
                            } else {
                                subscriberChannel = socketChannel;
                            }
                        }
                    } else if (selectedKey.isReadable()) {
                        SocketChannel socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
                        if(socketChannel != null) {
                            socketChannel.configureBlocking(false);
                            Socket socket = socketChannel.socket();

                            sendToSubscribers("Is readable?");
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
        try {
            CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();

            subscriberChannel.write(encoder.encode(CharBuffer.wrap(message)));
        } catch (IOException e){
            System.out.println(e);
        }
    }

    public static void main(String[] args){
        MessageBroker broker = new MessageBroker();
    }
}