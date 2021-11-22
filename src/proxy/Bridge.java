package proxy;

import callback.OnSelect;
import io.Register;
import stream.ChannelStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Bridge implements OnSelect {
    private final SocketChannel source;
    private final ChannelStream srcStream;
    private final SocketChannel destination;
    private final ChannelStream destStream;
    private final Register register;
    private final ByteArrayOutputStream destinationOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream sourceOut = new ByteArrayOutputStream();
    private boolean close = false;
    private final byte[] bytes = new byte[1024];

    public Bridge(ChannelStream srcStream, ChannelStream destStream, Register register) {
        this.source = srcStream.getChannel();
        this.srcStream = srcStream;
        this.destination = destStream.getChannel();
        this.destStream = destStream;
        this.register = register;
    }

    @Override
    public void onAccept(SelectionKey key) throws IOException {

    }

    @Override
    public void onConnect(SelectionKey key) {

    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();  // 转成客户端连接

        boolean select = socketChannel == source;
        ByteArrayOutputStream outputStream = select ? destinationOut : sourceOut;
        ChannelStream stream = select ? srcStream : destStream;
        int len;
        try {
            while (true) {
                len = stream.read(bytes);  // 读取,如果没有数据是-1
                if (len == -1) {
                    if (outputStream.size() == 0) {  // 如果对面断开连接了，就关闭
                        register.cancel(socketChannel);
                        close = true;
                    }
                    break;
                }
                outputStream.write(bytes, 0, len);  // 把数据读到数组，用于等会发送给另一个 channel
                if (outputStream.size() > 1024 * 32)  // 当数组过大时，分配转发
                    break;
            }
            register(select ? destination : source, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            close();
        }
    }

    private void close() throws IOException {
        register.cancel(source);
        register.cancel(destination);
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        boolean select = socketChannel == source;
        ByteArrayOutputStream outputStream = select ? sourceOut : destinationOut;
        ChannelStream stream = select ? srcStream : destStream;

        outputStream.writeTo(stream.getSocksOutputStream());
        stream.flush();
        outputStream.reset();

        if (close)  // 如果对端关闭了，本端也关闭
            register.cancel(socketChannel);
        else
            register(socketChannel, SelectionKey.OP_READ);
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        try {
            close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void registerAll(int even) throws ClosedChannelException {
        register(source, even);
        register(destination, even);
    }

    public void register(SelectableChannel channel, int even) throws ClosedChannelException {
        register.register(channel, even, this);
    }
}
