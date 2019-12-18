package Server;

import Core.*;
import Game.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Server implements IGameSubscriber {
    private final ServerSettings serverSettings;
    private GameService gameService;
    private ServerSocket serverSocket;
    private boolean initialized;

    private ClientCollection clientCollection;
    private Thread threadAcceptConnections;
    private boolean isRunning;

    public Server() {
        gameService = new GameService();
        gameService.subscribe(this);
        serverSettings = new ServerSettings();
        clientCollection = new ClientCollection();
        threadAcceptConnections = new Thread(this::acceptConnections);

        int port = serverSettings.getPort();
        try {
            // try to start server on port
            serverSocket = new ServerSocket(port);
            initialized = true;
        } catch (IOException e) {
            Logger.getInstance().log(String.format("Could not bind server to port: %o", port));
        }
    }

    // Start server
    public void run() {
        if (!initialized) {
            Logger.getInstance().log("Can not start non-initialized server!");
            return;
        }

        isRunning = true;
        Logger.getInstance().log("Server started!");

        // start listening for new clients
        threadAcceptConnections.start();

        // read messages and send keep alives.
        while (isRunning) {
            readMessages();
            issueTimeouts();
            Utility.sleep();
        }
    }

    // send welcome to clients
    @Override
    public void sendObserversWelcome(List<Long> clients) {
        clients.forEach(t -> {
            if (clientCollection.getSessionMap().containsKey(t)) {
                var m = new Message(MessageType.JoinRoom, false);
                send(t, m);
            }
        });
    }

    // send request to input score combination
    @Override
    public void sendRegisterCombination(Long clientId, CombinationDetails combinations) {
        send(clientId, new Message(MessageType.RegisterCombination, combinations));
    }

    // send details about round
    @Override
    public void sendRound(GameRoom gameRoom, HashMap<Long, RoundDetails> roundDetails) {
        var players = gameRoom.getPlayers();
        players.forEach((id, data) -> {
            if (roundDetails.containsKey(id)) {
                RoundDetails details = roundDetails.get(id);
                send(id, new Message(MessageType.Round, details));
            }
        });
    }


    // send game over to clients
    @Override
    public void sendGameOver(List<Long> clients, String text) {
        clients.forEach(t -> {
            if (clientCollection.getSessionMap().containsKey(t)) {
                var m = new Message(MessageType.GameOver, text);
                send(t, m);
            }
        });
    }


    // accept new connections
    private void acceptConnections() {
        while (isRunning) {
            Logger.getInstance().log("Awaiting client connection...");
            try {
                Socket socket = serverSocket.accept();
                Session session = new Session(socket);
                session.run();

                // add new client to data
                long id = clientCollection.addSession(session);
                Logger.getInstance().log(String.format("Client %o connected!", id));
                printConnectedClients();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utility.sleep();
        }
    }

    // send keep alives to clients
    private void issueTimeouts() {
        clientCollection.getSessionMap().forEach((id, session) -> {
            long time = System.currentTimeMillis();
            long compare = session.getLastKeepAlive() + serverSettings.getKeepAlive();

            // send every N milliseconds
            if (time > compare) {
                long nextUpdate = time + serverSettings.getKeepAlive();
                session.setLastKeepAlive(nextUpdate);

                // Close session if server couldn't send keep alive message.
                Message message = new Message(MessageType.KeepAlive, "");
                boolean canWrite = session.send(message);
                if (!canWrite) {
                    Logger.getInstance().log(String.format("client %d disconnected", id));
                    session.stop();

                    // remove client
                    removeClientById(id);

                    printConnectedClients();
                }
            }
        });
    }

    // remove client from data and game
    private void removeClientById(Long id) {
        clientCollection.remove(id);

        gameService.leaveRoom(id);
    }


    // send join room to client
    private void joinRoom(Long id, String roomName, boolean isObserver) {
        Message m;
        GameRoom gameRoom = gameService.getGameRoomByName(roomName);

        // get player status
        boolean willBecomeObserver = gameService.willBecomeObserver(gameRoom, isObserver);

        // check if successful joining gmae
        boolean joined = gameService.joinRoom(id, gameRoom, willBecomeObserver);

        if (joined) {
            m = new Message(MessageType.JoinRoom, willBecomeObserver);
        } else {
            m = new Message(MessageType.Text, String
                    .format("Could not join room: %s", roomName));
        }
        send(id, m);
    }

    // on client connected
    private void onMessageConnect(Long id, Message message) {
        String s = String.format("Welcome to the server! Your id is %o", id);
        Message m = new Message(MessageType.Text, s);
        send(id, m);
    }

    // if client want to create a room
    private void onMessageCreateRooms(Long id, Message message) {
        String name = message.getData().toString();
        // try to create a room
        boolean created = gameService.createRoom(name);

        // join room if it was created
        if (created) {
            joinRoom(id, name, false);
        } else {
            sendInvalidRoom(id);
        }
    }

    // received dice to hold from client
    private void onMessageDice(Long id, Message message) {
        HashSet<Integer> diceToKeep = (HashSet<Integer>) message.getData();
        // play turn in game room
        gameService.playDice(id, diceToKeep);
    }

    // received request to display highscore
    private void onMessageHighScore(Long id, Message message) {
        var highScore = gameService.getGameCollection().getHighScore();
        String text = "";
        // if no high score send nothing
        if (highScore == null) {
            text = "No high score... yet!";
        } else {
            // send recorded score
            text = String.format("Highest score: %d by player %o", highScore.getValue(), highScore.getKey());
        }
        send(id, new Message(MessageType.Text, text));
    }

    // received request to join room
    private void onMessageJoinRoom(Long id, Message message) {
        String roomName = message.getData().toString();
        if (gameService.hasGameRoomWithName(roomName)) {
            // join room if it exists
            joinRoom(id, roomName, false);
        } else {
            sendInvalidRoom(id);
        }
    }

    // received request to observe a room
    private void onMessageObserveRoom(Long id, Message message) {
        String roomName = message.getData().toString();
        if (gameService.hasGameRoomWithName(roomName)) {
            // join game as observer if it exists
            joinRoom(id, roomName, true);
        } else {
            sendInvalidRoom(id);
        }
    }

    // received notice that player is ready
    private void onMessageReadyUp(Long id, Message message) {
        gameService.readyUp(id);
    }

    // received a score registration
    private void onMessageRegisterCombination(Long id, Message message) {
        int index = (int) message.getData();

        // register score in game room
        gameService.registerCombination(id, index);
    }

    // received a request to list rooms
    private void onMessageSearchRooms(Long id, Message message) {
        HashMap<String, GameRoom> rooms = gameService.getGameCollection().getGameRooms();
        Message m;
        if (rooms.isEmpty()) {
            // if no rooms
            m = new Message(MessageType.Text, "No rooms found.");
        } else {
            // if has rooms, send info about each one
            var keys = rooms.keySet().toArray();
            StringBuilder sb = new StringBuilder("Game rooms:\n");
            for (int i = 0; i < rooms.size(); i++) {
                String roomName = (String) keys[i];
                GameRoom gameRoom = gameService.getGameRoomByName(roomName);
                sb.append(String.format("%d: %s - %s", i + 1, roomName, gameRoom.getGameStatus()));
            }
            sb.append("\n");
            m = new Message(MessageType.Text, sb.toString());

        }
        send(id, m);
    }

    // print number of connected clients
    private void printConnectedClients() {
        Logger.getInstance().log(String
                .format("Connected clients: %d", clientCollection.getSessionMap().size()));
    }

    // read every message in session message queue
    // parse messages on main thread
    private void readMessages() {
        clientCollection.getSessionMap().forEach((id, session) -> {
            var queue = session.getMessages();
            while (!queue.isEmpty()) {
                Message message = queue.poll();
                MessageType type = message.getType();

                // handle each message type differently
                switch (type) {
                    case SearchRooms:
                        onMessageSearchRooms(id, message);
                        break;
                    case Connect:
                        onMessageConnect(id, message);
                        break;
                    case CreateRoom:
                        onMessageCreateRooms(id, message);
                        break;
                    case JoinRoom:
                        onMessageJoinRoom(id, message);
                        break;
                    case ObserveRoom:
                        onMessageObserveRoom(id, message);
                        break;
                    case ReadyUp:
                        onMessageReadyUp(id, message);
                        break;
                    case Dice:
                        onMessageDice(id, message);
                        break;
                    case RegisterCombination:
                        onMessageRegisterCombination(id, message);
                        break;
                    case HighScore:
                        onMessageHighScore(id, message);
                        break;
                }
            }
        });
    }

    // send a message object to client
    private void send(Long id, Message message) {
        if (message == null || !clientCollection.getSessionMap().containsKey(id)) {
            return;
        }

        Session s = clientCollection.getSessionMap().get(id);
        s.send(message);
    }

    // send message that room was invalid
    private void sendInvalidRoom(Long id) {
        send(id, new Message(MessageType.Text, "No such room"));
    }
}
