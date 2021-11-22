package proxy;

import io.Register;
import proxy.shake.ShakeHand;
import server.Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ProxyServer extends Server {

    public ProxyServer(Register register) throws IOException {
        super(register);
    }

    @Override
    public void onAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();  // 转成服务器连接
        SocketChannel sc = serverSocketChannel.accept();  // 与客户端建立连接
        sc.configureBlocking(false);  // 给新连接设立异步非阻塞
        ShakeHand hand = new ShakeHand(sc, register);
        register.register(sc, SelectionKey.OP_READ, hand);
    }
}
