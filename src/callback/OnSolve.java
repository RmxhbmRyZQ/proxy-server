package callback;

import io.Register;
import stream.ChannelStream;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface OnSolve {
    void solve(ChannelStream sc, Register register) throws IOException;

    public void error(SelectionKey key, Exception e);
}
