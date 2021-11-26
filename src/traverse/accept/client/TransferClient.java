package traverse.accept.client;

import traverse.accept.server.AcceptServer;
import callback.OnSolve;
import io.Register;
import proxy.Bridge;
import stream.ChannelStream;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class TransferClient implements OnSolve {
    @Override
    public void solve(ChannelStream sc, Register register) throws IOException {
        long p = sc.readLong();
        sc.clear();
        SocketChannel socketChannel = AcceptServer.map.get(p).getSocketChannel();
        if (socketChannel == null) {
            register.cancel(sc.getChannel());
            return;
        }
        ChannelStream stream = new ChannelStream(socketChannel);
        Bridge bridge = new Bridge(sc, stream, register);
        bridge.registerAll(SelectionKey.OP_READ);
    }

    @Override
    public void error(SelectionKey key, Exception e) {
        // 错误应该重发
    }
}
