package callback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public abstract class OnSelect {
    public int workerIndex = -1;
    public int queueCount = 0;
    public int readOps = 0;

    public abstract void onAccept(SelectionKey key) throws IOException;

    public abstract void onConnect(SelectionKey key) throws IOException;

    public abstract void onRead(SelectionKey key) throws IOException;

    public abstract void onWrite(SelectionKey key) throws IOException;

    public abstract void onError(SelectionKey key, Exception e);
}
