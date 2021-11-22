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
        int offset = 0;
        if (len + count > BUFFER_CAPACITY && count > 0) {
            flush();
        }
        while (offset < len) {
            if (len - offset < BUFFER_CAPACITY) {
                System.arraycopy(b, off + offset, bytes, 0, len - offset);
                count = len - offset;
            } else {
                byteBuffer.clear();
                byteBuffer.put(b, off + offset, BUFFER_CAPACITY);
                byteBuffer.flip();
                sc.write(byteBuffer);
            }
            offset += BUFFER_CAPACITY;
        }
    }

    @Override
    public void flush() throws IOException {
        byteBuffer.clear();
        byteBuffer.put(bytes, 0, count);
        byteBuffer.flip();
        sc.write(byteBuffer);
        count = 0;
    }
}
