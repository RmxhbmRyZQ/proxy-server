package stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static stream.ChannelStream.BUFFER_CAPACITY;

public class SocksOutputStream extends OutputStream {
    private final SocketChannel sc;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    private final byte[] bytes = new byte[BUFFER_CAPACITY];
    private int count = 0;

    public SocksOutputStream(SocketChannel sc) {
        this.sc = sc;
    }

    @Override
    public void write(int b) throws IOException {
        if (count == BUFFER_CAPACITY) {
            flush();
        }
        bytes[count++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int offset = 0, write;
        if (len + count >= BUFFER_CAPACITY && count > 0) {
            flush();
        }

        while (offset < len) {
            if (len - offset < BUFFER_CAPACITY) {
                System.arraycopy(b, off + offset, bytes, count, len - offset);
                count += len - offset;
                offset += len - offset;
            } else {
                byteBuffer.clear();
                byteBuffer.put(b, off + offset, BUFFER_CAPACITY);
                byteBuffer.flip();
                write = sc.write(byteBuffer);
                if (write < BUFFER_CAPACITY) {  // 如果系统缓冲区满了
                    throw new SystemBufferOverflowException(offset);
                }
                offset += write;
            }
        }
    }

    @Override
    public void flush() throws IOException {
        byteBuffer.clear();
        byteBuffer.put(bytes, 0, count);
        byteBuffer.flip();
        int write = sc.write(byteBuffer);
        if (write != count) {
            if (write > 0) {
                System.arraycopy(bytes, 0, bytes, write, count - write);
                count -= write;
            }
            throw new SystemBufferOverflowException(0);
        }
        count = 0;
    }

    @Override
    public String toString() {
        return "SocksOutputStream{" +
                "bytes=" + new String(bytes, 0, count) +
                '}';
    }
}
