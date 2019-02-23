import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

public class BIOServer {

    public static void main(String[] args) throws IOException{
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8999));
        while(true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            new Thread(()->{
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                try {
                    while(socketChannel.read(byteBuffer) > 0) {
                        byteBuffer.flip();
                        socketChannel.write(byteBuffer);
                        byteBuffer.clear();
                    } 
                    socketChannel.close();
                    System.out.println("close");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }).start();
        }
    }
}