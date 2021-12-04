package transfer;

import io.Client;
import io.Register;
import stream.BlockList;
import stream.ChannelStream;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 使用空闲块管理来进行交换数据
 */
public class BlockBridge extends Client {
    private final SocketChannel source;
    private final ChannelStream srcStream;
    private final SocketChannel destination;
    private final ChannelStream destStream;
    private final Register register;
    private final BlockList buffer = new BlockList();
    private boolean close = false;

    public BlockBridge(ChannelStream srcStream, ChannelStream destStream, Register register) {
        this.source = srcStream.getChannel();
        this.srcStream = srcStream;
        this.destination = destStream.getChannel();
        this.destStream = destStream;
        this.register = register;
    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();  // 转成客户端连接

        boolean select = socketChannel == source;
        ChannelStream stream = select ? srcStream : destStream;
        try {
            if (buffer.readFrom(stream.getSocksInputStream(), !select) == -1) {
                register.cancel(socketChannel);
                close = true;
            }
            register(select ? destination : source, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
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
        ChannelStream stream = select ? srcStream : destStream;
        if (!buffer.writeTo(stream.getSocksOutputStream(), select) ||
                !buffer.flush(stream.getSocksOutputStream()))
            return;

        if (close) {  // 如果对端关闭了，本端也关闭
            register.cancel(socketChannel);
        } else
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

    @Override
    public String toString() {
        return "BlockBridge{" +
                "source=" + source +
                ", destination=" + destination +
                '}';
    }
}
