package Server;

public class ServerSettings {
    private int port;
    private int keepAlive;

    public ServerSettings() {
        port = 4242;
        keepAlive = 500;
    }

    public int getPort() {
        return port;
    }

    public int getKeepAlive() {
        return keepAlive;
    }
}
