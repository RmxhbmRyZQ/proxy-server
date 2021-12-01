package traverse.accept.server;

import io.Server;
import traverse.accept.client.Client;
import io.Register;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;

public class ProxyServer extends Server {
    ServerSocketChannel ssc;
    protected Register register;

    public ProxyServer(String ip, int port, Register register) throws IOException {
        this.register = register;
        ssc = ServerSocketChannel.open();  // 生成服务器Socket
        configureServerSocket(ssc, new InetSocketAddress(ip, port));
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
        register.register(sc, SelectionKey.OP_READ, client);
    }
}
