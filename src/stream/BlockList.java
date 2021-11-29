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
    private Block buffer;

    public BlockList() {
        byte[] bytes = new byte[BLOCK_SIZE];
        buffer = new Block(bytes, 0, 0);
    }

    static private Block poll() {
        Block block;
        synchronized (BlockList.class) {
            block = free.poll();
        }
        if (block == null) {
            block = new Block(new byte[BLOCK_SIZE], 0, 0);
        } else {
            block.pos = block.limit = 0;
        }
        return block;
    }

    static private void add(Block block) {
        synchronized (BlockList.class) {
            free.add(block);
        }
    }

    public int readFrom(InputStream is, boolean select) throws IOException {
        int len = 0, r;
        while (true) {
            r = buffer.read(is);
            if (r == -1) break;
            len += r;
            buffer.incLimit(r);
            if (buffer.isFull()) {
                if (select) {
                    src.add(buffer);
                } else {
                    dest.add(buffer);
                }
                buffer = BlockList.poll();
            }
        }
        return len == 0 ? -1 : len;
    }

    public int writeTo(OutputStream os, boolean select) throws IOException {
        int len = 0, w;
        while (true) {
            Block block = select ? src.poll() : dest.poll();
            if (block == null) break;
            w = block.write(os);
            if (w == 0) break;
            len += w;
            if (block.isEmpty()) {
                BlockList.add(block);
            } else {
                if (select) {
                    src.addFirst(block);
                } else {
                    dest.addFirst(block);
                }
                block.incPos(w);
            }
        }
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

        public void incPos(int pos) {
            this.pos += pos;
        }

        public int read(InputStream is) throws IOException {
            return is.read(bytes, pos, bytes.length - limit);
        }

        public int write(OutputStream os) throws IOException {
            int len;
            try {
                os.write(bytes, pos, limit - pos);
                len = limit - pos;
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
