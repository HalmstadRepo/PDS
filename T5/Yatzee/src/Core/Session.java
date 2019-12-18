package Core;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

// reads and writes messages on socket
public class Session {
    private boolean debug = true;
    private final Thread threadRead;
    private final Socket socket;

    private ConcurrentLinkedQueue<Message> messageQueue;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private boolean isRunning;
    private long lastKeepAlive;

    public Session(Socket socket) {
        this.socket = socket;

        messageQueue = new ConcurrentLinkedQueue<>();
        threadRead = new Thread(this::readFromSocket);

        // init streams to send/receive data
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            outputStream = new ObjectOutputStream(os);
            inputStream = new ObjectInputStream(is);
        } catch (IOException e) {
            if (debug) {
                e.printStackTrace();
            }
            isRunning = false;
        }
    }

    // start thread
    public void run() {
        isRunning = true;
        threadRead.start();
    }

    // stop thread and close socket
    public void stop() {
        isRunning = false;
        threadRead.interrupt();
        outputStream = null;
        inputStream = null;
        try {
            socket.close();
        } catch (IOException e) {
            if (debug) {
                e.printStackTrace();
            }
            isRunning = false;
        }
    }

    // send message across socket
    public boolean send(Message message) {
        if (outputStream == null || message == null){
            return  false;
        }
        try {

            outputStream.writeObject(message);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // read message from socket
    private void readFromSocket() {
        while (isRunning) {
            try {
                // Read message from input stream
                Object obj = inputStream.readObject();
                if (obj == null) {
                    return;
                }
                // add to queue to prevent cross thread conflicts
                Message message = (Message) obj;
                messageQueue.offer(message);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e);
                isRunning = false;
            }
            Utility.sleep();
        }
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    public void setLastKeepAlive(long nextUpdate) {
        lastKeepAlive = nextUpdate;
    }

    public ConcurrentLinkedQueue<Message> getMessages() {
        return messageQueue;
    }

    public Socket getSocket() {
        return socket;
    }
}
