package proxy;

import callback.OnSelect;
import io.Register;
import proxy.shake.ShakeHand;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ProxyServer implements OnSelect {
    ServerSocketChannel ssc;
    protected Register register;

    public ProxyServer(Register register) throws IOException {
        this.register = register;
        ssc = ServerSocketChannel.open();  // 生成服务器Socket
        ssc.configureBlocking(false);  // 设异步非阻塞
        ServerSocket serverSocket = ssc.socket();  // 拿到服务器Socket
        serverSocket.bind(new InetSocketAddress("0.0.0.0", 8080));  // 绑定地址与端口
    }

    public void register() throws ClosedChannelException {
        register.register(ssc, SelectionKey.OP_ACCEPT, this);
    }

    @Override
    public void onAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();  // 转成服务器连接
        SocketChannel sc = serverSocketChannel.accept();  // 与客户端建立连接
        sc.configureBlocking(false);  // 给新连接设立异步非阻塞
        ShakeHand hand = new ShakeHand(sc, register);
        register.register(sc, SelectionKey.OP_READ, hand);
    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {

    }

    @Override
    public void onRead(SelectionKey key) throws IOException {

    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {

    }

    @Override
    public void onError(SelectionKey key, Exception e) {

    }
}
