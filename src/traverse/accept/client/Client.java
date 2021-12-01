package traverse.accept.client;

import callback.OnSolve;
import io.Register;
import stream.ChannelStream;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Client extends io.Client {
    private final ChannelStream sc;
    private final Register register;
    private OnSolve solve = null;

    public Client(SocketChannel sc, Register register) {
        this.sc = new ChannelStream(sc);
        this.register = register;
    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        int r = sc.read();
        if (r == -1) {
            register.cancel(key.channel());
            return;
        }
        switch (r) {
            case 0:  // 处理监听操作
                solve = new AcceptClient();
                solve.solve(sc, register);
                break;
            case 1:  // 处理数据交换操作
                solve = new TransferClient();
                solve.solve(sc, register);
                break;
        }
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        if (solve != null)
            solve.error(key, e);
    }
}
