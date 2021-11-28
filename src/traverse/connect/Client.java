package traverse.connect;

import io.Register;
import proxy.Bridge;
import stream.ChannelStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashSet;

public class Client extends io.Client {
    private final Register register;
    private final long l;
    private ChannelStream proxy;
    private ChannelStream local;
    private boolean finish = false;
    public static final HashSet<Client> set = new HashSet<>();

    public Client(Register register, long l) {
        this.register = register;
        this.l = l;
    }

    public void connect(InetSocketAddress proxy, InetSocketAddress local) throws IOException {
        SocketChannel s1 = SocketChannel.open();
        configureSocket(s1);
        s1.connect(proxy);
        this.proxy = new ChannelStream(s1);

        SocketChannel s2 = SocketChannel.open();
        configureSocket(s2);
        s2.connect(local);
        this.local = new ChannelStream(s2);

        register.register(s1, SelectionKey.OP_CONNECT, this);
        register.register(s2, SelectionKey.OP_CONNECT, this);
        set.add(this);
    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        if (proxy.getChannel() == key.channel()) {
            System.out.println(l);
            proxy.write(1);
            proxy.writeLong(l);
            register.register(proxy.getChannel(), SelectionKey.OP_WRITE, this);
        } else {
            if (finish) {
                Bridge bridge = new Bridge(proxy, local, register);
                bridge.registerAll(SelectionKey.OP_READ);
            } else {
                register.register(local.getChannel(), 0, this);
            }
            finish = true;
        }
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        proxy.flush();
        if (finish) {
            Bridge bridge = new Bridge(proxy, local, register);
            bridge.registerAll(SelectionKey.OP_READ);
        } else {
            register.register(proxy.getChannel(), 0, this);
        }
        finish = true;
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        try {
            close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void close() throws IOException {
        register.cancel(proxy.getChannel());
        register.cancel(local.getChannel());
        set.remove(this);
    }
}
