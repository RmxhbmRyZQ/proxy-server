package stream.block;

import stream.SystemBufferOverflowException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import static stream.block.FreeBlock.BLOCK_SIZE;

/**
 * 使用空闲块系统来管理内存
 */
public class BlockList {
    private final LinkedList<Block> queue = new LinkedList<>();
    private Block buffer;

    public BlockList() {
        byte[] bytes = new byte[BLOCK_SIZE];
        buffer = new Block(bytes, 0, 0);
    }

    /**
     * 把数据读入这里的内存块
     * @return 读了的字节数
     */
    public int readFrom(InputStream is) throws IOException {
        int len = 0, r;
        while (true) {
            r = buffer.read(is);
            if (r == -1) break;  // EOF
            len += r;
            buffer.incLimit(r);
            if (buffer.isFull()) {  // 如果写满了放入相应的写入队列
                queue.add(buffer);
                buffer = FreeBlock.poll();
            }
        }
        return len == 0 ? -1 : len;
    }

    /**
     * 把写入队列的块写到 os 里面
     * @return true 全写进去；false 没有全写进去
     */
    public boolean writeTo(OutputStream os) throws IOException {
        int w;
        while (true) {
            Block block = queue.poll();  // 从写入队列取出块来写
            if (block == null) {  // 写如队列为空，写 BUFFER
                if (buffer.isEmpty()) break;
                w = buffer.write(os);
                buffer.incPos(w);
                if (buffer.isEmpty()) buffer.reset();
                else  return false;
                break;
            }
            w = block.write(os);
            block.incPos(w);
            if (block.isEmpty()) {
                FreeBlock.add(block);
            } else {
                queue.addFirst(block);
                return false;
            }
        }
        return true;
    }

    /**
     * @return true，缓冲区的数据都写入了；false，缓没有全部写入
     */
    public static boolean flush(OutputStream os) throws IOException {
        try{
            os.flush();
            return true;
        } catch (SystemBufferOverflowException e) {
            return false;
        }
    }
}
