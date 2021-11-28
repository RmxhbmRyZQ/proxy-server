package traverse.accept.server;

import callback.OnSelect;
import io.Register;
import io.Server;
import stream.ChannelStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

    @Override
    public void onAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();  // 转成服务器连接
        SocketChannel socketChannel = serverSocketChannel.accept();  // 与客户端建立连接
        socketChannel.configureBlocking(false);  // 给新连接设立异步非阻塞
        map.put(count, this);
        list.add(socketChannel);
        sc.writeLong(count);
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
    public void onError(SelectionKey key, Exception e) {
        SelectableChannel channel = key.channel();
        if (channel == ssc) {
            try {
                for (SocketChannel c : list) {
                    register.cancel(c);
                }
                register.cancel(sc.getChannel());
                list.clear();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } else {
            list.remove((SocketChannel) channel);
        }
    }

    public SocketChannel getSocketChannel() {
        return list.poll();
    }
}
