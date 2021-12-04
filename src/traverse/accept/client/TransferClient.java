package traverse.accept.client;

import transfer.BlockBridge;
import traverse.accept.server.AcceptServer;
import callback.OnSolve;
import io.Register;
import stream.ChannelStream;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class TransferClient implements OnSolve {
    @Override
    public void solve(ChannelStream sc, Register register) throws IOException {
        long p = sc.readLong();
        sc.clear();
        AcceptServer acceptServer = AcceptServer.map.get(p);
        if (acceptServer == null) {
            register.cancel(sc.getChannel());
            return;
        }
        SocketChannel socketChannel = acceptServer.getSocketChannel();
        if (socketChannel == null) {
            register.cancel(sc.getChannel());
            return;
        }
        ChannelStream stream = new ChannelStream(socketChannel);
        BlockBridge bridge = new BlockBridge(sc, stream, register);
        bridge.registerAll(SelectionKey.OP_READ);
    }

    @Override
    public void error(SelectionKey key, Exception e) {
        // 错误应该重发
        e.printStackTrace();
    }
}
