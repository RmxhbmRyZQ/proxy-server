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
        stream.write(0);  // 表示监听
        stream.write(bind.getAddress().getAddress());  // 发送 ip 和端口
        stream.write((bind.getPort() >> 8) & 0xff);
        stream.write((bind.getPort() >> 0) & 0xff);
        register.register(stream.getChannel(), SelectionKey.OP_WRITE, this);
    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        boolean close = true;
        while (true) {
            long l = stream.readLong();  // 读取 ID 标识
            if (l == -1) {
                if (close) {  // 对方已经关闭服务器了
                    register.cancel(stream.getChannel());
                    System.exit(0);
                } else break;
            }
            close = false;
            Client client = new Client(register, l);  // 建立与服务器的交换连接
            client.connect(proxy, local);
        }
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        stream.flush();
        register.register(stream.getChannel(), SelectionKey.OP_READ, this);
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        // 关闭所有连接
        for (Client client : Client.set) {
            try {
                client.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        try {
            register.cancel(stream.getChannel());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        System.exit(0);
    }
}
