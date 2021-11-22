package server;

import callback.OnSelect;
import io.Register;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server implements OnSelect {
    ServerSocketChannel ssc;
    protected Register register;

    public Server(Register register) throws IOException {
        this.register = register;
        ssc = ServerSocketChannel.open();  // 生成服务器Socket
        ssc.configureBlocking(false);  // 设异步非阻塞
        ServerSocket serverSocket = ssc.socket();  // 拿到服务器Socket
        serverSocket.bind(new InetSocketAddress("0.0.0.0", 80));  // 绑定地址与端口
    }

    public void register() throws ClosedChannelException {
        register.register(ssc, SelectionKey.OP_ACCEPT, this);
    }

    @Override
    public void onAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();  // 转成服务器连接
        SocketChannel sc = serverSocketChannel.accept();  // 与客户端建立连接
        sc.configureBlocking(false);  // 给新连接设立异步非阻塞
        Client client = new Client(sc, register);
        client.register(SelectionKey.OP_READ);
    }

    @Override
    public void onConnect(SelectionKey key) {

    }

    @Override
    public void onRead(SelectionKey key) {

    }

    @Override
    public void onWrite(SelectionKey key) {

    }

    @Override
    public void onError(SelectionKey key, Exception e) {

    }
}
