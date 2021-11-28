package traverse.connect;

import io.Register;
import stream.ChannelStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ProxyClient extends io.Client {
    private final InetSocketAddress proxy;
    private final InetSocketAddress bind;
    private final InetSocketAddress local;
    private final Register register;
    private final ChannelStream stream;

    public ProxyClient(InetSocketAddress proxy, InetSocketAddress bind, InetSocketAddress local,
                       Register register) throws IOException {
        this.proxy = proxy;
        this.bind = bind;
        this.local = local;
        this.register = register;
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(proxy);
        stream = new ChannelStream(socketChannel);
    }

    public void register() throws ClosedChannelException {
        register.register(stream.getChannel(), SelectionKey.OP_CONNECT, this);
    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {
        stream.getChannel().finishConnect();
        stream.write(0);
        stream.write(bind.getAddress().getAddress());
        stream.write((bind.getPort() >> 8) & 0xff);
        stream.write((bind.getPort() >> 0) & 0xff);
        register.register(stream.getChannel(), SelectionKey.OP_WRITE, this);
    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        long l = stream.readLong();
        Client client = new Client(register, l);
        client.connect(proxy, local);
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        stream.flush();
        register.register(stream.getChannel(), SelectionKey.OP_READ, this);
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        try {
            for (Client client : Client.set) {
                client.close();
            }
            register.cancel(stream.getChannel());
            System.exit(0);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
