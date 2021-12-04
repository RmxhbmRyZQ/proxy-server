package io;

import callback.OnSelect;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Client implements OnSelect {
    @Override
    public void onAccept(SelectionKey key) throws IOException {

    }

    @Override
    public void onConnect(SelectionKey key) throws IOException {

    }

    @Override
    public void onRead(SelectionKey key) throws IOException {

    }

    @Override
    public void onWrite(SelectionKey key) throws IOException {

    }

    @Override
    public void onError(SelectionKey key, Exception e) {

    }

    public void configureSocket(SocketChannel sc) throws IOException {
        sc.configureBlocking(false);
        sc.socket().setReuseAddress(false);
    }
}
