package io;

import callback.OnEven;
import callback.OnSelect;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

public class Register implements OnEven {
    private final IoLoop loop;
    private final Map<SelectableChannel, OnSelect> hashMap;

    public Register() throws IOException {
        loop = new IoLoop(this);
        hashMap = new HashMap<>();
    }

    @Override
    public void callback(SelectionKey key) {
        OnSelect select = hashMap.get(key.channel());
        if (select == null)
            return;
        try {
            if (key.isAcceptable())  // nattraverse.accept 事件
                select.onAccept(key);
            if (key.isConnectable())  // nattraverse.connect 事件
                select.onConnect(key);
            if (key.isReadable())  // read 事件
                select.onRead(key);
            if (key.isWritable())  // write 事件
                select.onWrite(key);
        }catch (CancelledKeyException | IOException e) {
//            e.printStackTrace();
            select.onError(key, e);
            try {
                cancel(key.channel());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void register(SelectableChannel sc, int even, OnSelect select)
            throws ClosedChannelException {
        if (!sc.isOpen()) return;
        loop.register(sc, even);
        hashMap.put(sc, select);
    }

    public void cancel(SelectableChannel channel) throws IOException {
        if (channel == null || !channel.isOpen()) return;
        channel.close();
        hashMap.remove(channel);
    }

    public IoLoop getLoop() {
        return loop;
    }
}
