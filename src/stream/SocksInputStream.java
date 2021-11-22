package stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static stream.ChannelStream.BUFFER_CAPACITY;

public class SocksInputStream extends InputStream {
    private final SocketChannel sc;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    private final byte[] extra = new byte[BUFFER_CAPACITY];
    private int pos = 0, limit = 0;

    public SocksInputStream(SocketChannel sc) {
        this.sc = sc;
    }

    public void clear() {
        pos = limit = 0;
    }

    @Override
    public int read() throws IOException {
        if (pos >= limit) {  // 没有空间了先读取空间
            int len = sc.read(byteBuffer);
            if (len <= 0) {
                return -1;
            }
            byteBuffer.flip();
            byteBuffer.get(extra, 0, len);
            byteBuffer.clear();
            pos = 0;
            limit = len;
        }
        return (int) extra[pos++] & 0xff;  // 返回缓冲的一个字节
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int offset = 0, r, min;
        if (pos < limit) {  // 缓冲还有数据先从缓冲读
            min = Math.min(len, limit - pos);
            System.arraycopy(extra, pos, b, off, min);
            offset = min;
            if (min != len) {
                pos = 0;
                limit = 0;
            } else {
                pos += min;
            }
        }
        while (offset < len) {  // 若还需要读数据
            r = sc.read(byteBuffer);
            min = Math.min(len - offset, r);
            if (r <= 0) break;  // EOF
            byteBuffer.flip();
            byteBuffer.get(b, off + offset, min);
            if (r != min) {  // 当读取的数据超过要返回的数据时，放入缓冲
                byteBuffer.get(extra, limit, r - min);
                limit += r - min;
            }
            byteBuffer.clear();
            offset += min;
        }
        return offset != 0 ? offset : -1;
    }
}
