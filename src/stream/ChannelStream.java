package stream;

import java.io.*;
import java.nio.channels.SocketChannel;

public class ChannelStream {
    public final static int BUFFER_CAPACITY = 1024;

    private final SocketChannel sc;
    private final SocksInputStream socksInputStream;
    private final SocksOutputStream socksOutputStream;

    public ChannelStream(SocketChannel sc) {
        socksInputStream = new SocksInputStream(sc);
        socksOutputStream = new SocksOutputStream(sc);
        this.sc = sc;
    }

    public void writeLong(long l) throws IOException {
        write((byte)(l >>> 56));
        write((byte)(l >>> 48));
        write((byte)(l >>> 40));
        write((byte)(l >>> 32));
        write((byte)(l >>> 24));
        write((byte)(l >>> 16));
        write((byte)(l >>> 8));
        write((byte)(l >>> 0));
    }

    public void write(int b) throws IOException {
        socksOutputStream.write(b);
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        socksOutputStream.write(bytes, off, len);
    }

    public void write(byte[] bytes) throws IOException {
        socksOutputStream.write(bytes);
    }

    public void flush() throws IOException {
        socksOutputStream.flush();
    }

    public int read() throws IOException {
        return socksInputStream.read();
    }

    public int read(byte[] bytes) throws IOException {
        return socksInputStream.read(bytes);
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        return socksInputStream.read(bytes, off, len);
    }

    public int readPort() throws IOException {
        return (socksInputStream.read() << 8) + socksInputStream.read();
    }

    public String readIP() throws IOException {
        return socksInputStream.read() + "." +
                socksInputStream.read() + "." +
                socksInputStream.read() + "." +
                socksInputStream.read();
    }

    public String readIPV6() throws IOException {
        int len;
        byte[] bytes;
        len = socksInputStream.read();
        bytes = new byte[len];
        socksInputStream.read(bytes);
        return new String(bytes);
    }

    public String readDomain() throws IOException {
        int len;
        byte[] bytes;
        len = socksInputStream.read();
        bytes = new byte[len];
        socksInputStream.read(bytes);
        return new String(bytes);
    }

    public SocketChannel getChannel() {
        return sc;
    }

    public void clear() {
        socksInputStream.clear();
    }

    public SocksInputStream getSocksInputStream() {
        return socksInputStream;
    }

    public SocksOutputStream getSocksOutputStream() {
        return socksOutputStream;
    }

    public long readLong() throws IOException {
        if(!socksInputStream.require(8)) return -1;
        return (((long)read() << 56) +
                ((long)(read() & 255) << 48) +
                ((long)(read() & 255) << 40) +
                ((long)(read() & 255) << 32) +
                ((long)(read() & 255) << 24) +
                ((read() & 255) << 16) +
                ((read() & 255) <<  8) +
                ((read() & 255) <<  0));
    }
}
