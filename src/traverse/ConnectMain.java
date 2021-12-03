package traverse;

import io.Register;
import traverse.connect.ProxyClient;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 内网穿透额客户端
 * @author RmxhbmRyZQ 2021.12.2
 */
public class ConnectMain {
    public static void main(String[] args) {
        try {
            Register register = new Register();
            InetSocketAddress proxy = new InetSocketAddress("163.142.219.164", 8090);  // 目的端地址
//            InetSocketAddress proxy = new InetSocketAddress("0.0.0.0", 8090);
            InetSocketAddress bind = new InetSocketAddress("0.0.0.0", 8091);  // 目的端监听的地址
            InetSocketAddress local = new InetSocketAddress("127.0.0.1", 3366);  // 本地服务器的地址
            ProxyClient client = new ProxyClient(proxy, bind, local, register);
            client.register();
            register.getLoop().loop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
