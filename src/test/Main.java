package test;

import callback.OnEven;
import callback.OnSelect;
import io.Client;
import stream.block.TransferBlockList;
import threadpool.ThreadPool;
import threadpool.Worker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws IOException {
        OnSelect a = new Client();
    }
}
