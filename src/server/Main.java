package server;

import io.Register;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Register register = new Register();
            Server server = new Server(register);
            server.register();
            register.getLoop().loop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
