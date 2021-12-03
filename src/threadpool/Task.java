package threadpool;

import callback.OnSelect;
import stream.SystemBufferOverflowException;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public class Task {
    private final OnSelect select;
    private final SelectionKey key;

    public Task(OnSelect select, SelectionKey selectionKey) {
        this.select = select;
        this.key = selectionKey;
    }

    public void callback() {
        try {
            if (key.isAcceptable())  // accept 事件
                select.onAccept(key);
            if (key.isConnectable())  // connect 事件
                select.onConnect(key);
            if (key.isReadable())  // read 事件
                select.onRead(key);
            if (key.isWritable())  // write 事件
                select.onWrite(key);
            OnSelect select = (OnSelect) key.attachment();
        } catch (SystemBufferOverflowException e) {
            // TODO: 2021/11/28 nothing
        } catch (CancelledKeyException | IOException e) {
//            e.printStackTrace();
            select.onError(key, e);
            try {
                cancel(key.channel());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 取消事件，并关闭连接
     */
    public void cancel(SelectableChannel channel) throws IOException {
        if (channel == null || !channel.isOpen()) return;
        channel.close();
    }
}
