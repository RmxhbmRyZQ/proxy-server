package traverse;

import io.Register;
import traverse.connect.ProxyClient;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ConnectMain {
    public static void main(String[] args) {
        try {
            Register register = new Register();
            InetSocketAddress proxy = new InetSocketAddress("163.142.219.164", 8090);
//            InetSocketAddress proxy = new InetSocketAddress("0.0.0.0", 8090);
            InetSocketAddress bind = new InetSocketAddress("0.0.0.0", 8091);
            InetSocketAddress local = new InetSocketAddress("127.0.0.1", 3366);
            ProxyClient client = new ProxyClient(proxy, bind, local, register);
            client.register();
            register.getLoop().loop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
