import io.Register;
import proxy.ProxyServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Register register = new Register();
            ProxyServer server = new ProxyServer("0.0.0.0", 8080, register);
            server.register();
            register.getLoop().loop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
