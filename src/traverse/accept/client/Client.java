package traverse.accept.client;

import callback.OnSelect;
import callback.OnSolve;
import io.Register;
import stream.ChannelStream;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Client implements OnSelect {
    private final ChannelStream sc;
    private final Register register;
    private OnSolve solve = null;

    public Client(SocketChannel sc, Register register) {
        this.sc = new ChannelStream(sc);
        this.register = register;
    }

    @Override
    public void onAccept(SelectionKey key) throws IOException {

    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {

    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        switch (sc.read()) {
            case 0:
                solve = new AcceptClient();
                solve.solve(sc, register);
                break;
            case 1:
                solve = new TransferClient();
                solve.solve(sc, register);
                break;
        }
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {

    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        if (solve != null)
            solve.error(key, e);
    }
}
