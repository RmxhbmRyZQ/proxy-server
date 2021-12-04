package traverse.accept.server;

import io.Register;
import io.Server;
import stream.ChannelStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AcceptServer extends Server {
    private final ChannelStream sc;
    private final ServerSocketChannel ssc;
    private final Register register;
    private static long count = 0;
    public static final Map<Long, AcceptServer> map = new HashMap<>();
    private final Queue<SocketChannel> list = new LinkedList<>();

    public AcceptServer(ChannelStream sc, InetSocketAddress bind, Register register) throws IOException {
        this.sc = sc;
        this.ssc = ServerSocketChannel.open();
        configureServerSocket(ssc, bind);
        this.register = register;
    }

    public void register() throws ClosedChannelException {
        register.register(ssc, SelectionKey.OP_ACCEPT, this);
        register.register(sc.getChannel(), SelectionKey.OP_READ, this);
    }

    @Override
    public void onAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();  // 转成服务器连接
        SocketChannel socketChannel = serverSocketChannel.accept();  // 与客户端建立连接
        socketChannel.configureBlocking(false);  // 给新连接设立异步非阻塞
        map.put(count, this);
        list.add(socketChannel);  // 放入等待池
        sc.writeLong(count);  // 把 ID 交给对方
        count++;
        register.register(sc.getChannel(), SelectionKey.OP_WRITE, this);
        register.register(socketChannel, 0, this);
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        sc.flush();
        register.register(sc.getChannel(), SelectionKey.OP_READ, this);
    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        close();
    }

    @Override
    public void onError(SelectionKey key, Exception e) {
        SelectableChannel channel = key.channel();
        if (channel == ssc || channel == sc.getChannel()) {
            close();
        } else {
            list.remove((SocketChannel) channel);
        }
    }

    /**
     * 关闭有关的所有连接
     */
    private void close() {
        for (SocketChannel c : list) {
            try {
                register.cancel(c);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        try {
            register.cancel(sc.getChannel());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        try {
            register.cancel(ssc);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        list.clear();
    }

    public SocketChannel getSocketChannel() {
        return list.poll();
    }
}
