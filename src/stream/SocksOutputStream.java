package stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static stream.ChannelStream.BUFFER_CAPACITY;

/**
 * 缓冲要发送的数据
 * 缓冲区满了就发送
 */
public class SocksOutputStream extends OutputStream {
    private final SocketChannel sc;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);

    public SocksOutputStream(SocketChannel sc) {
        this.sc = sc;
    }

    @Override
    public void write(int b) throws IOException {
        if (!byteBuffer.hasRemaining()) {
            flush();
        }
        byteBuffer.put((byte) b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int offset = 0, min;
        while (offset < len) {
            if (!byteBuffer.hasRemaining()) {
                try {
                    flush();
                } catch (SystemBufferOverflowException e) {
                    e.setLen(offset);
                    throw e;
                }
            } else {
                min = Math.min(len - offset, byteBuffer.remaining());
                byteBuffer.put(b, offset + off, min);
                offset += min;
            }
        }
    }

    @Override
    public void flush() throws IOException {
        int should, write, p, l;
        p = byteBuffer.position();  // 记录状态
        l = byteBuffer.limit();
        byteBuffer.flip();
        should = byteBuffer.remaining();
        write = sc.write(byteBuffer);
        if (write != should) {  // 当系统缓冲区满时
            if (write > 0) {
                byteBuffer.compact();
            } else {  // 回滚状态
                byteBuffer.position(p);
                byteBuffer.limit(l);
            }
            throw new SystemBufferOverflowException(0);
        }
        byteBuffer.clear();
    }

    @Override
    public String toString() {
        return "SocksOutputStream{" +
                "bytes=" + byteBuffer +
                '}';
    }
}