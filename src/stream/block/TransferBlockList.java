package stream.block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 使用空闲块系统来管理内存
 */
public class TransferBlockList {
    private final BlockList src = new BlockList();
    private final BlockList dest = new BlockList();

    public TransferBlockList() {

    }

    public int readFrom(InputStream is, boolean select) throws IOException {
        if (select) return src.readFrom(is);
        return dest.readFrom(is);
    }

    public boolean writeTo(OutputStream os, boolean select) throws IOException {
        if (select) return src.writeTo(os);
        return dest.writeTo(os);
    }

    public boolean flush(OutputStream os) throws IOException {
        return BlockList.flush(os);
    }
}
