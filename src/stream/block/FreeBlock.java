package stream.block;

import java.util.LinkedList;

public class FreeBlock {
    public final static int BLOCK_SIZE = 4096;

    static private final LinkedList<Block> free = new LinkedList<>();  // 全局空闲块内存

    /**
     * 从全局空闲块申请一个块
     */
    public static Block poll() {
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
    public static void add(Block block) {
        synchronized (BlockList.class) {
            free.add(block);
        }
    }
}
