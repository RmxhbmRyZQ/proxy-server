package io;

import callback.OnEven;
import callback.OnSelect;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class IoLoop {
    /**
     * 开启线程池有两种方法
     * 1.在线程池的任务处理完后才进行
     */
    private final Selector selector;
    private final OnEven even;

    public IoLoop(OnEven even) throws IOException {
        selector = Selector.open();  // 生成选择器
        this.even = even;
    }

    public void register(SelectableChannel channel, int even, OnSelect select) throws ClosedChannelException {
        channel.register(selector, even, select);
    }

    public void loop() throws IOException {
        while (true) {
            selector.select(10);  // 监听,返回事件的数量
            Set<SelectionKey> selectionKeys = selector.selectedKeys();// 返回有事件的连接
            Iterator<SelectionKey> it = selectionKeys.iterator();  // 生成迭代器

            while (it.hasNext()) {
                SelectionKey key = it.next();  // 转型
                even.callback(key);
                it.remove();  // 处理完了就移除
            }
        }
    }
}
