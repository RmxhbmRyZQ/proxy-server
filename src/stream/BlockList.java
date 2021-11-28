package stream;

import java.util.LinkedList;

public class BlockList {
    LinkedList<Block> list = new LinkedList<>();

    public void add(byte[] bytes, int pos, int limit) {
        Block block = new Block(bytes, pos, limit);
        synchronized (this) {
            list.add(block);
        }
    }

    public Block poll() {
        synchronized (this) {
            return list.poll();
        }
    }

    public void addFirst(Block block) {
        synchronized (this) {
            list.addFirst(block);
        }
    }

    public static class Block {
        private byte[] bytes;
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

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }
}
