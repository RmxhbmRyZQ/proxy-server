package stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * 使用空闲块系统来管理内存
 */
public class BlockList {
    private final static int BLOCK_SIZE = 4096;

    private final LinkedList<Block> src = new LinkedList<>();
    private final LinkedList<Block> dest = new LinkedList<>();
    static private final LinkedList<Block> free = new LinkedList<>();  // 全局空闲块内存
    private Block srcBuffer;
    private Block destBuffer;

    public BlockList() {
        byte[] src = new byte[BLOCK_SIZE];
        srcBuffer = new Block(src, 0, 0);
        byte[] dest = new byte[BLOCK_SIZE];
        destBuffer = new Block(dest, 0, 0);
    }

    /**
     * 从全局空闲块申请一个块
     */
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

    /**
     * 添加一个块到全局空闲块中
     */
    private void add(Block block) {
        synchronized (BlockList.class) {
            free.add(block);
        }
    }

    /**
     * 把数据读入这里的内存块
     * @return 读了的字节数
     */
    public int readFrom(InputStream is, boolean select) throws IOException {
        int len = 0, r;
        Block buffer = select ? srcBuffer : destBuffer;
        while (true) {
            r = buffer.read(is);
            if (r == -1) break;  // EOF
            len += r;
            buffer.incLimit(r);
            if (buffer.isFull()) {  // 如果写满了放入相应的写入队列
                if (select) {
                    src.add(buffer);
                } else {
                    dest.add(buffer);
                }
                buffer = poll();
            }
        }
        if (select) srcBuffer = buffer;
        else destBuffer = buffer;
        return len == 0 ? -1 : len;
    }

    /**
     * 把写入队列的块写到 os 里面
     * @return true 全写进去；false 没有全写进去
     */
    public boolean writeTo(OutputStream os, boolean select) throws IOException {
        int w;
        while (true) {
            Block block = select ? src.poll() : dest.poll();  // 从写入队列取出块来写
            if (block == null) {  // 写如队列为空，写 BUFFER
                block = select ? srcBuffer : destBuffer;
                if (block.isEmpty()) break;
                w = block.write(os);
                block.incPos(w);
                if (block.isEmpty()) block.reset();
                else  return false;
                break;
            }
            w = block.write(os);
            block.incPos(w);
            if (block.isEmpty()) {
                add(block);
            } else {
                if (select) {
                    src.addFirst(block);
                } else {
                    dest.addFirst(block);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * @return true，缓冲区的数据都写入了；false，缓没有全部写入
     */
    public boolean flush(OutputStream os) throws IOException {
        try{
            os.flush();
            return true;
        } catch (SystemBufferOverflowException e) {
            return false;
        }
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
