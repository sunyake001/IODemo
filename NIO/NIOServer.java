import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.net.InetSocketAddress;


public class NIOServer {

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8999));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(true) {
            int readyChannels = selector.select();
            if (readyChannels == 0) {
                continue;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if(key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }else if (key.isReadable()) {
                    System.out.println("readable");
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int num = socketChannel.read(byteBuffer);
                        if (num > 0) {
                            byteBuffer.flip();
                            socketChannel.write(byteBuffer);
                            socketChannel.register(selector, SelectionKey.OP_WRITE);
                            System.out.println("recv");
                        } else if(num == -1) {
                            System.out.println("close");
                            socketChannel.close();
                        }
                    } catch (IOException e) {
                        socketChannel.close();
                    }
                    
                } else if(key.isWritable()) {
                    System.out.println("writable");
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
        }
    }
}