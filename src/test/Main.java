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
import java.nio.channels.SocketChannel;
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

        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress("117.72.224.129", 443));
        while (!channel.isConnectionPending());
        channel.finishConnect();
        System.out.println(1);
        channel.close();
//        Socket socket = new Socket();
//        socket.connect(new InetSocketAddress("117.72.224.129", 443));
//        System.out.println(1);
//        socket.close();

//        Socket socket = new Socket();
//        socket.connect(new InetSocketAddress("120.82.216.231", 8090));
//        OutputStream outputStream = socket.getOutputStream();
//        InputStream inputStream = socket.getInputStream();
//        byte[] bytes = {0, 0, 0, 0, 0, 0x1f, -101};
//        outputStream.write(bytes);
//        byte[] b = new byte[9];
//        inputStream.read(b);
//        System.out.println(b[0] + " " + b[8]);
//        send(b[8]);
//        inputStream.read(b);
//        System.out.println(b[0] + " " + b[8]);
//        send(b[8]);
//        socket.close();
    }

    private static void send(byte a) throws IOException {

        byte[] bb = {1, 0, 0, 0, 0, 0, 0, 0, a};
        Socket s = new Socket();
        s.connect(new InetSocketAddress("120.82.216.231", 8090));
        OutputStream os = s.getOutputStream();
        InputStream is = s.getInputStream();
        os.write(bb);
        InputStreamReader reader = new InputStreamReader(is);
        char[] chars = new char[1024];
        int len = reader.read(chars);
        System.out.println(new String(chars, 0, len));
        os.write("HTTP/1.1 200 OK\r\nContent-Length: 22\r\n\r\n<div>Hello world</div>".getBytes(StandardCharsets.UTF_8));
        s.close();
    }

    native void free();
}
