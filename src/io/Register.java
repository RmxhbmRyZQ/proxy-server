package io;

import callback.OnEven;
import callback.OnSelect;
import stream.SystemBufferOverflowException;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public class Register implements OnEven {
    private final IoLoop loop;

    public Register() throws IOException {
        loop = new IoLoop(this);
    }

    @Override
    public void callback(SelectionKey key) {
        OnSelect select = (OnSelect) key.attachment();
        if (select == null)
            return;
        try {
            if (key.isAcceptable())  // accept 事件
                select.onAccept(key);
            if (key.isConnectable())  // connect 事件
                select.onConnect(key);
            if (key.isReadable())  // read 事件
                select.onRead(key);
            if (key.isWritable())  // write 事件
                select.onWrite(key);
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
     * 注册事件
     */
    public void register(SelectableChannel sc, int even, OnSelect select)
            throws ClosedChannelException {
        if (!sc.isOpen()) return;
        loop.register(sc, even, select);
    }

    /**
     * 取消事件，并关闭连接
     */
    public void cancel(SelectableChannel channel) throws IOException {
        if (channel == null || !channel.isOpen()) return;
        channel.close();
    }

    public IoLoop getLoop() {
        return loop;
    }
}
