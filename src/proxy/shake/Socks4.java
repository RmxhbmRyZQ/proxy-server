package proxy.shake;

import io.Register;
import proxy.Bridge;
import stream.ChannelStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Socks4 extends Socks {
    private ChannelStream srcStream;
    private ChannelStream destStream;
    private final SocketChannel channel;
    private final Register register;
    private int error = 0;
    private SocketChannel dest;

    public Socks4(ChannelStream stream, Register register) {
        this.srcStream = stream;
        this.channel = stream.getChannel();
        this.register = register;
    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();  // 转成客户端连接
        if (socketChannel.isConnectionPending()) {
            socketChannel.finishConnect();
            register.register(channel, SelectionKey.OP_WRITE, this);  // 目的端连接成功后可以响应信息了
            register.register(dest, 0, null);  // 监听 OP_CONNECT 时可能 selector 会一直返回长度为 0 的 key，需要修改状态
        }
    }

    @Override
    public void solve() throws IOException {
        if (srcStream.read() != SocksConst.CONNECT) {
            error = 1;
            register.register(channel, SelectionKey.OP_WRITE, this);
        } else {
            // 连接被代理端需要连接的服务器
            int port = srcStream.readPort();
            String ip = srcStream.readIP();
            dest = SocketChannel.open();
            configureSocket(dest);
            dest.connect(new InetSocketAddress(ip, port));
            destStream = new ChannelStream(dest);
            register.register(dest, SelectionKey.OP_CONNECT, this);
            srcStream.clear();
        }
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        byte[] b = {0, 0x5a, 0, 0, 0, 0, 0, 0};  // 正确的返回数据
        b[1] += error;  // 检查错误
        srcStream.write(b);
        srcStream.flush();

        // 没有出错进入互发消息阶段
        if (error != 0) {
            register.cancel(channel);
            register.cancel(dest);
        } else {
            Bridge bridge = new Bridge(srcStream, destStream, register);
            bridge.registerAll(SelectionKey.OP_READ);
        }
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        if (key.channel() == dest) {  // 目的端连接失败
            error = 2;
            try {
                register.register(channel, SelectionKey.OP_WRITE, this);
            } catch (ClosedChannelException closedChannelException) {
                closedChannelException.printStackTrace();
            }
        } else {
            try {
                register.cancel(dest);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
