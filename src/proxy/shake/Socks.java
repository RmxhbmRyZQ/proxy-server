package proxy.shake;

import io.Client;

import java.io.IOException;

public abstract class Socks extends Client {
    public abstract void solve() throws IOException;
}
