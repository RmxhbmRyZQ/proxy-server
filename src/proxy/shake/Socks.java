package proxy.shake;

import callback.OnSelect;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public abstract class Socks implements OnSelect {
    @Override
    public void onAccept(SelectionKey key) throws IOException {

    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {

    }

    @Override
    public void onRead(SelectionKey key) throws IOException {

    }

    public abstract void solve() throws IOException;

    @Override
    public void onWrite(SelectionKey key) throws IOException {

    }

    @Override
    public void onError(SelectionKey key, Exception e) {

    }

    protected String intToString(byte i) {
        return String.valueOf((int) i & 0xff);
    }
}
