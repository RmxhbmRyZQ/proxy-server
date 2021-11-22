package proxy.shake;

import callback.OnSelect;
import io.Register;
import stream.ChannelStream;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ShakeHand implements OnSelect {
    private final SocketChannel sc;
    private final Register register;
    private final ChannelStream stream;
    private Socks socks;

    public ShakeHand(SocketChannel sc, Register register) {
        this.sc = sc;
        this.register = register;
        stream = new ChannelStream(sc);
    }

    @Override
    public void onAccept(SelectionKey key) throws IOException {

    }

    @Override
    public void onConnect(SelectionKey key) throws ClosedChannelException {

    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();  // 转成客户端连接
        try {
            int protocol = stream.read();
            if (protocol == -1) {  // 直接当服务器关闭处理，当然这样处理是有问题的
                register.cancel(sc);
                return;
            }

            if (protocol == SocksConst.PROTO_VERS4) {
                socks = new Socks4(stream, register);
                socks.solve();
            } else if (protocol == SocksConst.PROTO_VERS){
                socks = new Socks5(stream, register);
                socks.solve();
            } else {  // 出错
                register.register(socketChannel, SelectionKey.OP_WRITE, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            register.cancel(socketChannel);
        }
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        byte[] b = {0, 0x5b, 0, 0, 0, 0, 0, 0};  // 错误信息
        stream.write(b);
        stream.flush();
        register.cancel(sc);
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        if (socks != null)
            socks.onError(key, e);  // 握手期间出错转发处理
    }
}
