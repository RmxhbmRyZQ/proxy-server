package test;

import stream.BlockList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        BlockList blockList = new BlockList();
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        ByteArrayInputStream bais;
        ByteArrayOutputStream baos;
        bais = new ByteArrayInputStream(bytes);
        blockList.readFrom(bais, false);
        bais = new ByteArrayInputStream(bytes);
        blockList.readFrom(bais, false);
        baos = new ByteArrayOutputStream();
        blockList.writeTo(baos, true);
        int b = 0;
    }
}
