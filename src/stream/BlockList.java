package stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class BlockList {
    private final static int BLOCK_SIZE = 4096;

    private final LinkedList<Block> src = new LinkedList<>();
    private final LinkedList<Block> dest = new LinkedList<>();
    static private final LinkedList<Block> free = new LinkedList<>();
    private Block srcBuffer;
    private Block destBuffer;
    private static int a, b;

    public BlockList() {
        byte[] src = new byte[BLOCK_SIZE];
        srcBuffer = new Block(src, 0, 0);
        byte[] dest = new byte[BLOCK_SIZE];
        destBuffer = new Block(dest, 0, 0);
    }

    private Block poll() {
        Block block;
        synchronized (BlockList.class) {
            block = free.poll();
        }
        if (block == null) {
            block = new Block(new byte[BLOCK_SIZE], 0, 0);
        } else {
            block.reset();
        }
        return block;
    }

    private void add(Block block) {
        synchronized (BlockList.class) {
            free.add(block);
        }
    }

    public int readFrom(InputStream is, boolean select) throws IOException {
        int len = 0, r;
        Block buffer = select ? srcBuffer : destBuffer;
        while (true) {
            r = buffer.read(is);
            if (r == -1) break;
            len += r;
            buffer.incLimit(r);
            if (buffer.isFull()) {
                if (select) {
                    src.add(buffer);
                    srcBuffer = poll();
                } else {
                    dest.add(buffer);
                    destBuffer = poll();
                }
            }
        }
        a += len;
        System.out.println("a:" + a);
        return len == 0 ? -1 : len;
    }

    public int writeTo(OutputStream os, boolean select) throws IOException {
        int len = 0, w;
        while (true) {
            Block block = select ? src.poll() : dest.poll();
            if (block == null) {
                block = select ? srcBuffer : destBuffer;
                if (block.isEmpty()) break;
                w = block.write(os);
                len += w;
                block.incPos(w);
                if (block.isEmpty()) block.reset();
                else {
                    b += len;
                    System.out.println("b:" + b);
                    throw new SystemBufferOverflowException(len);
                }
                break;
            }
            w = block.write(os);
//            if (w == 0) break;
            len += w;
            block.incPos(w);
            if (block.isEmpty()) {
                add(block);
            } else {
                if (select) {
                    src.addFirst(block);
                } else {
                    dest.addFirst(block);
                }
                b += len;
                System.out.println("b:" + b);
                throw new SystemBufferOverflowException(len);
//                break;
            }
        }
        b += len;
        System.out.println("b:" + b);
        return len;
    }

    public static class Block {
        private final byte[] bytes;
        private int pos;
        private int limit;

        private Block(byte[] bytes, int pos, int limit) {
            this.bytes = bytes;
            this.pos = pos;
            this.limit = limit;
        }

        public void reset() {
            pos = 0;
            limit = 0;
        }

        public void incPos(int pos) {
            this.pos += pos;
        }

        public int read(InputStream is) throws IOException {
            return is.read(bytes, limit, bytes.length - limit);
        }

        public int write(OutputStream os) throws IOException {
            int len = limit - pos;
            try {
                os.write(bytes, pos, len);
            } catch (SystemBufferOverflowException e) {
                len = e.getLen();
            }
            return len;
        }

        public void incLimit(int r) {
            limit += r;
        }

        public boolean isFull() {
            return limit == bytes.length;
        }

        public boolean isEmpty() {
            return pos == limit;
        }
    }
}
