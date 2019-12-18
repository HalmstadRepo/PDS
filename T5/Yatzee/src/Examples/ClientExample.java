package Examples;

import Client.Client;

public class ClientExample {
    public static void main(String[] args) {
        Client c1 = new Client();
        c1.connect("localhost", 4242);
        c1.run();
    }
}
