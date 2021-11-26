package test;

import sun.net.SocksProxy;
import sun.net.www.http.HttpClient;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {
//        Proxy proxy = SocksProxy.create(new InetSocketAddress("127.0.0.1", 80), 5);
//        Socket socket = new Socket(proxy);
//        try {
//            socket.nattraverse.connect(new InetSocketAddress("www.baidu.com", 80));
//            OutputStream outputStream = socket.getOutputStream();
//            InputStream inputStream = socket.getInputStream();
//            outputStream.write("GET / HTTP/1.1\r\nHost: www.baidu.com\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.UTF_8));
//            char []chars = new char[1024];
//            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//            while (inputStreamReader.read(chars) > 0)
//                System.out.print(chars);
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("192.168.222.128", 8090));
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = {0, 0, 0, 0, 0, 0, 80};
        outputStream.write(bytes);
        System.out.println(inputStream.read());
    }

    native void free();
}
