package traverse.connect;

import io.Register;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        try {
            Register register = new Register();
            InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", 8090);
            InetSocketAddress bind = new InetSocketAddress("0.0.0.0", 80);
            InetSocketAddress local = new InetSocketAddress("127.0.0.1", 3366);
            ProxyClient client = new ProxyClient(proxy, bind, local, register);
            client.register();
            register.getLoop().loop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
