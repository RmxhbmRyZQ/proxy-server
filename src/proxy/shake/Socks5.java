package proxy.shake;

import io.Register;
import proxy.Bridge;
import stream.ChannelStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Socks5 extends Socks {
    private static final boolean AUTHENTICATION = true;

    private final ChannelStream channel;
    private ChannelStream destStream;
    private SocketChannel dest;
    private final Register register;
    private int step = 0;
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private boolean close = false;
    private boolean finish = false;

    public Socks5(ChannelStream channel, Register register) {
        this.channel = channel;
        this.register = register;
    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();  // 转成客户端连接
        if (socketChannel.isConnectionPending()) {
            socketChannel.finishConnect();

            byte[] reply = {5, SocksConst.REQUEST_OK, 0, SocksConst.IPV4, 0, 0, 0, 0, 0, 0};  // 代理成功
            stream.write(reply);
            finish = true;  // 完成

            register.register(channel.getChannel(), SelectionKey.OP_WRITE, this);  // 目的端连接成功后可以响应信息了
            register.register(dest, 0, null);  // 监听 OP_CONNECT 时可能 selector 会一直返回长度为 0 的 key，需要修改状态
        }
    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        if (AUTHENTICATION) authorize();
        else direct();
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        stream.writeTo(channel.getSocksOutputStream());  // 发送信息
        channel.flush();
        stream.reset();
        register(SelectionKey.OP_READ);
        if (close) {  // 出错，关闭
            register.cancel(channel.getChannel());
            register.cancel(dest);
        } else if (finish) {  // 完成开始数据转发
            Bridge bridge = new Bridge(channel, destStream, register);
            bridge.registerAll(SelectionKey.OP_READ);
        }
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        if (key.channel() == dest) {  // 目的端连接失败
            try {
                error(SocksConst.CONN_REFUSED);
            } catch (IOException closedChannelException) {
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

    @Override
    public void solve() throws IOException {
        if (AUTHENTICATION) authorize();
        else direct();
    }

    /**
     * 直接连接方式
     */
    private void direct() throws IOException {
        switch (step) {
            case 0:
                switch (channel.read()) {  // 选择客户端支持的认证方式
                    case 1:
                        switch (channel.read()) {
                            case SocksConst.NO_AUTH:  // 不需要认证
                                stream.write(SocksConst.PROTO_VERS);  // 协议 5
                                stream.write(SocksConst.NO_AUTH);
                                break;
                            case SocksConst.USER_PASSW:  // 用户密码认证
                            default:
                                noMethod();
                                break;
                        }
                        break;
                    case 2:
                        channel.read();
                        channel.read();
                        stream.write(SocksConst.PROTO_VERS);
                        stream.write(SocksConst.NO_AUTH);
                        break;
                    default:
                        noMethod();
                        break;
                }
                channel.clear();
                step++;
                register(SelectionKey.OP_WRITE);
                break;
            case 1:
                connect();  // 验证完成，开始连接
                break;
        }
    }

    /**
     * 认证方式
     */
    private void authorize() throws IOException {
        switch (step) {
            case 0:
                switch (channel.read()) {
                    case 1:
                        switch (channel.read()) {
                            case SocksConst.USER_PASSW:
                                stream.write(SocksConst.PROTO_VERS);
                                stream.write(SocksConst.USER_PASSW);
                                break;
                            case SocksConst.NO_AUTH:
                            default:
                                noMethod();
                                break;
                        }
                        break;
                    case 2:
                        channel.read();
                        channel.read();
                        stream.write(SocksConst.PROTO_VERS);
                        stream.write(SocksConst.USER_PASSW);
                        break;
                    default:
                        noMethod();
                        break;
                }
                channel.clear();
                step++;
                register(SelectionKey.OP_WRITE);
                break;
            case 1:
                if (channel.read() == 1) {  // 协商子协议为 1
                    channel.write(1);
                    if (authenticate()) {
                        channel.write(0);
                    } else {
                        channel.write(-1);
                        close = true;
                    }
                }
                channel.clear();
                step++;
                register(SelectionKey.OP_WRITE);
                break;
            case 2:
                connect();
                break;
        }
    }

    /**
     * 验证账号密码
     */
    private boolean authenticate() throws IOException {
        String userName, password = null;
        // 用户名
        int len = channel.read();
        byte[] bytes = new byte[len];
        channel.read(bytes);
        userName = new String(bytes);
        // 密码
        len = channel.read();
        if (len != 0) {
            if (len > bytes.length) bytes = new byte[len];
            channel.read(bytes);
            password = new String(bytes, 0, len);
        }
        // 认证用户名密码
        return userName.equals("RmxhbmRyZQ") && password.equals("123456");
    }

    /**
     * 连接错误
     */
    private void error(int even) throws IOException {
        byte[] bytes = {5, SocksConst.REQUEST_OK, 0, (byte) even};
        stream.reset();
        stream.write(bytes);
        close = true;
        register(SelectionKey.OP_WRITE);
    }

    /**
     * 没有方法
     */
    private void noMethod() throws IOException {
        byte[] bytes = {5, SocksConst.NO_METHODS};
        stream.reset();
        stream.write(bytes);
        close = true;
        register(SelectionKey.OP_WRITE);
    }

    /**
     * 处理连接事件
     */
    private void connect() throws IOException {
        if (channel.read() != SocksConst.PROTO_VERS || channel.read() != SocksConst.CONNECT) {
            error(SocksConst.NOT_ALLOWED);
        } else {
            channel.read();
            int port;
            String domain;
            switch (channel.read()) {
                case SocksConst.DOMAIN_NAME:  // 域名模式
                    domain = channel.readDomain();
                    port = channel.readPort();
                    destConnect(domain, port);
                    break;
                case SocksConst.IPV4:  // ipv4 模式
                    domain = channel.readIP();
                    port = channel.readPort();
                    destConnect(domain, port);
                    break;
                case SocksConst.IPV6:  // ipv6 模式
                    domain = channel.readIPV6();
                    port = channel.readPort();
                    destConnect(domain, port);
                    break;
            }
        }
        channel.clear();
        step++;
    }

    private void destConnect(String domain, int port) throws IOException {
        dest = SocketChannel.open();
        configureSocket(dest);
        dest.connect(new InetSocketAddress(domain, port));
        destStream = new ChannelStream(dest);
        register.register(dest, SelectionKey.OP_CONNECT, this);
    }

    private void register(int even) throws ClosedChannelException {
        register.register(channel.getChannel(), even, this);
    }
}
