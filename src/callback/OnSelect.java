package callback;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;

public interface OnSelect {
    public void onAccept(SelectionKey key) throws IOException;

    public void onConnect(SelectionKey key) throws IOException;

    public void onRead(SelectionKey key) throws IOException;

    public void onWrite(SelectionKey key) throws IOException;

    public void onError(SelectionKey key, Exception e);
}
