package traverse.connect;

import io.Register;
import transfer.BlockBridge;
import stream.ChannelStream;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashSet;

public class Client extends io.Client {
    private final Register register;
    private final long l;
    private InetSocketAddress proxyAddress;
    private ChannelStream proxy;
    private ChannelStream local;
    private boolean finish = false;
    public static final HashSet<Client> set = new HashSet<>();
    private int times = 0;

    public Client(Register register, long l) {
        this.register = register;
        this.l = l;
    }

    public void connect(InetSocketAddress proxy, InetSocketAddress local) throws IOException {
        this.proxy = connect(proxy);  // 连接服务器
        this.local = connect(local);  // 连接本地服务器
        proxyAddress = proxy;  // 用于短线重连
        set.add(this);
    }

    private ChannelStream connect(InetSocketAddress address) throws IOException {
        SocketChannel s1 = SocketChannel.open();
        configureSocket(s1);
        s1.connect(address);
        ChannelStream stream = new ChannelStream(s1);
        register.register(s1, SelectionKey.OP_CONNECT, this);
        return stream;
    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        channel.finishConnect();

        if (proxy.getChannel() == key.channel()) {  // 如果是连接代理服务器把 ID 发送出去
            proxy.write(1);  // 表明用于交互数据
            proxy.writeLong(l);
            register.register(proxy.getChannel(), SelectionKey.OP_WRITE, this);
        } else {
            if (finish) {  // 代理服务器和本地服务器都完成连接，进行数据交换
                BlockBridge bridge = new BlockBridge(proxy, local, register);
                bridge.registerAll(SelectionKey.OP_READ);
            } else {
                register.register(local.getChannel(), 0, this);
            }
            finish = true;
        }
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        proxy.flush();  // 发送数据
        if (finish) {
            BlockBridge bridge = new BlockBridge(proxy, local, register);
            bridge.registerAll(SelectionKey.OP_READ);
        } else {
            register.register(proxy.getChannel(), 0, this);
        }
        finish = true;
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        try {
            if (key.channel() == proxy.getChannel() && times++ < 3) {  // 断线重连
                register.cancel(key.channel());
                proxy = connect(proxyAddress);
            } else close();
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
