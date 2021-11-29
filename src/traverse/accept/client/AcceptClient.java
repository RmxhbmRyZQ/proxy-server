package traverse.accept.client;

import traverse.accept.server.AcceptServer;
import callback.OnSolve;
import io.Register;
import stream.ChannelStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

public class AcceptClient implements OnSolve {
    @Override
    public void solve(ChannelStream sc, Register register) throws IOException {
        String s = sc.readIP();
        int i = sc.readPort();
        AcceptServer acceptServer = new AcceptServer(sc, new InetSocketAddress(s, i), register);
        acceptServer.register();
    }

    @Override
    public void error(SelectionKey key, Exception e) {
        // 错误应该关闭与服务器有关的连接
    }
}
