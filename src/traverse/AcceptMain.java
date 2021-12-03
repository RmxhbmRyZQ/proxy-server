package traverse;

import traverse.accept.server.ProxyServer;
import io.Register;

import java.io.IOException;

/**
 * 内网穿透的服务端，在有公网 IP 的服务器上使用
 * @author RmxhbmRyZQ 2021.12.2
 */
public class AcceptMain {
    public static void main(String[] args) {
        try {
            Register register = new Register();
            ProxyServer server = new ProxyServer("0.0.0.0", 8090, register);
            server.register();
            register.getLoop().loop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
