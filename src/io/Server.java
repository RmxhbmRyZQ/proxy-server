package io;

import callback.OnSelect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class Server implements OnSelect {
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

    public void configureServerSocket(ServerSocketChannel ssc, InetSocketAddress bind) throws IOException {
        ssc.configureBlocking(false);
        ServerSocket socket = ssc.socket();
        socket.setReuseAddress(false);
        socket.bind(bind);
    }
}
