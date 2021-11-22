package server;

import callback.OnSelect;
import io.Register;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client implements OnSelect {
    private final Register register;
    private final SocketChannel channel;

    public Client(SocketChannel sc, Register register) {
        this.register = register;
        this.channel = sc;
    }

    public void register(int even) throws ClosedChannelException {
        register.register(channel, even, this);
    }

    @Override
    public void onAccept(SelectionKey key) {

    }

    @Override
    public void onConnect(SelectionKey key) {

    }

    @Override
    public void onRead(SelectionKey key) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        SocketChannel socketChannel = (SocketChannel) key.channel();  // 转成客户端连接
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);  // 生成字节缓存
        try {
            while (socketChannel.read(byteBuffer) > 0) {  // 读取,如果没有数据是-1
                byteBuffer.flip();  // 移动到头
                byte[] b = new byte[byteBuffer.limit()];  // 新建字节数组大小是缓存的长度
                byteBuffer.get(b);  // 把数据放入字节数组
                byteBuffer.clear();  // 清空缓存
                stringBuffer.append(new String(b, StandardCharsets.UTF_8));  // 转成字符串放进缓存
            }
            System.out.println(stringBuffer);
            register(SelectionKey.OP_WRITE);
        } catch (IOException e) {
//            key.cancel();  // 若发送错误,是客户端出了问题,取消事件
            socketChannel.close();  // 关闭连接
        }
    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);  // 建立字节缓存
        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<h1>hello worlds</h1>\n" +
                "<form action=\"/\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "    <p><input type=\"file\" name=\"upload\"></p>\n" +
                "    <p><input type=\"submit\" value=\"submit\"></p>\n" +
                "</form>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        byteBuffer.put(("HTTP/1.1 200 OK\r\nContent-Length: " + content.length() + "\r\n\r\n" + content).getBytes());  // 把数据放入缓存
        byteBuffer.flip();  // 移到开头
        socketChannel.write(byteBuffer);  // 发送
        register(SelectionKey.OP_READ);
    }

    @Override
    public void onError(SelectionKey key, Exception e) {

    }
}
