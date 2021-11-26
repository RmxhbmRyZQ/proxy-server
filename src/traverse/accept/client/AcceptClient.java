package traverse.accept.client;

import traverse.accept.server.AcceptServer;
import callback.OnSolve;
import io.Register;
import stream.ChannelStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class AcceptClient implements OnSolve {
    @Override
    public void solve(ChannelStream sc, Register register) throws IOException {
        String s = sc.readIP();
        int i = sc.readPort();
        ServerSocketChannel ssc = ServerSocketChannel.open();  // 生成服务器Socket
        ssc.configureBlocking(false);  // 设异步非阻塞
        ServerSocket serverSocket = ssc.socket();  // 拿到服务器Socket
        serverSocket.bind(new InetSocketAddress(s, i));  // 绑定地址与端口
        AcceptServer acceptServer = new AcceptServer(sc, ssc, register);
        register.register(ssc, SelectionKey.OP_ACCEPT, acceptServer);
    }

    @Override
    public void error(SelectionKey key, Exception e) {
        // 错误应该关闭与服务器有关的连接
    }
}
