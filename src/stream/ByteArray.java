package stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 源码不能乱改，要复制过来改
 */
public class ByteArray extends OutputStream {
    protected byte buf[];
    protected int count;
    protected int pos;

    public ByteArray() {
        this(32);
    }

    public ByteArray(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: "
                    + size);
        }
        buf = new byte[size];
    }

    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        buf = Arrays.copyOf(buf, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public void write(int b) {
        ensureCapacity(count + 1);
        buf[count] = (byte) b;
        count += 1;
    }

    public void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, pos, count - pos);
    }

    public void seek(int pos) {
        this.pos += pos;
    }

    public synchronized void reset() {
        count = 0;
        pos = 0;
    }

    public byte toByteArray()[] {
        return Arrays.copyOf(buf, count);
    }

    public int size() {
        return count;
    }

    public String toString() {
        return new String(buf, 0, count);
    }

    public String toString(String charsetName)
            throws UnsupportedEncodingException {
        return new String(buf, 0, count, charsetName);
    }

    @Deprecated
    public String toString(int hibyte) {
        return new String(buf, hibyte, 0, count);
    }

    public void close() throws IOException {
    }
}
