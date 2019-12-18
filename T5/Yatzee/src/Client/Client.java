package Client;

import Core.*;
import Game.CombinationDetails;
import Game.Combinations.Combination;
import Game.GameSettings;
import Game.RoundDetails;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Client {

    private final Scanner scanner;
    private final GameSettings gameSettings;

    public Session session;
    private Choice choiceSearchRooms;
    private Choice choiceJoinRoom;
    private Choice choiceCreateRoom;
    private Choice choiceExit;
    private Choice choiceRollDice;
    private Choice choiceObserveRoom;
    private Choice choiceReadyUp;
    private Choice choiceRegisterDice;
    private Choice choiceHighScore;

    private State currentState;
    private State stateMain;
    private State stateLobby;
    private State stateObserver;
    private State stateRollDice;
    private State stateRegisterDice;

    private RoundDetails currentDetails;
    private CombinationDetails currentCombinations;

    public Client() {
        gameSettings = new GameSettings();
        scanner = new Scanner(System.in);

        // Create choices for ui
        choiceSearchRooms = new Choice("Search for rooms", this::searchForRooms);
        choiceJoinRoom = new Choice("Join room", this::joinRoom);
        choiceCreateRoom = new Choice("Create room", this::createRoom);
        choiceObserveRoom = new Choice("Observe room", this::observeRoom);
        choiceReadyUp = new Choice("Ready up", this::readyUp);
        choiceHighScore = new Choice("High score", this::highScore);
        choiceExit = new Choice("Exit", this::exit);

        choiceRollDice = new Choice("Roll dice", this::rollDice);
        choiceRegisterDice = new Choice("Register dice", this::registerDice);

        // Create ui states
        stateMain = new State();
        stateMain.add(choiceSearchRooms);
        stateMain.add(choiceJoinRoom);
        stateMain.add(choiceCreateRoom);
        stateMain.add(choiceObserveRoom);
        stateMain.add(choiceHighScore);
        stateMain.add(choiceExit);

        stateLobby = new State();
        stateLobby.add(choiceReadyUp);
        stateLobby.add(choiceExit);

        stateRollDice = new State();
        stateRollDice.add(choiceRollDice);
        stateRollDice.add(choiceExit);

        stateRegisterDice = new State();
        stateRegisterDice.add(choiceRegisterDice);
        stateRegisterDice.add(choiceExit);

        stateObserver = new State();
        stateObserver.add(choiceExit);

        currentState = stateMain;
    }

    // connect to server in ip:port
    public void connect(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            session = new Session(socket);
            session.run();
            Message m = new Message(MessageType.Connect, "");
            session.send(m);
            Logger.getInstance().log(String
                    .format("Connected to server on %s:%d", ip, port));
        } catch (IOException e) {
            Logger.getInstance().log(String
                    .format("Could not connect to server on %s:%d", ip, port));
        }
    }


    public void run() {
        while (true) {
            // read messages from server
            readMessages();
            Utility.sleep();
        }
    }


    // send request to create room
    public void sendCreateRoom(String name) {
        sendMessage(MessageType.CreateRoom, name);
    }

    // send notice of player ready
    public void sendReadyUp() {
        sendMessage(MessageType.ReadyUp, "");
    }

    // send request to list rooms
    public void sendSearchForRooms() {
        sendMessage(MessageType.SearchRooms, "");
    }

    // create a room
    private void createRoom(Integer value) {
        System.out.print("Creating room...\n");
        System.out.print("Enter room name: ");
        scanner.nextLine();
        String name = scanner.nextLine();

        sendCreateRoom(name);
    }

    // disconnect from server
    private void disconnect() {
        if (!isConnected()) {
            return;
        }
        Logger.getInstance().log("Disconnected from server.");
    }

    // exit application
    private void exit(Integer value) {
        disconnect();
        System.exit(0);
    }

    // send request for high score
    private void highScore(Integer value) {
        sendMessage(MessageType.HighScore, "");
    }

    // is connected to server?
    private boolean isConnected() {
        return session != null && !session.getSocket().isClosed() && session.getSocket().isConnected();
    }

    // join a room on server
    private void joinRoom(Integer value) {
        if (!isConnected()) {
            return;
        }
        System.out.print("Enter room name: ");
        scanner.nextLine();
        String name = scanner.nextLine();

        sendJoinRoom(name);
    }

    // menu choice
    private void menu() {
        if (currentState != null) {
            // print menu
            printState(currentState);

            int input = scanner.nextInt() - 1;
            if (input >= 0 && input < currentState.getChoices().size()) {
                // Choice consumer callback
                Choice c = currentState.getChoices().get(input);
                c.getConsumer().accept(0);
            }
        }
    }

    // observe a room on server
    private void observeRoom(Integer value) {
        if (!isConnected()) {
            return;
        }
        System.out.print("Enter room name: ");
        scanner.nextLine();
        String name = scanner.nextLine();

        sendObserveRoom(name);
    }

    // on game over message received from server
    private void onMessageGameOver(Message message) {
        String s = (String) message.getData();
        Logger.getInstance().log(s);
    }

    // on join room message received from server
    private void onMessageJoinRoom(Message message) {
        // print player status
        boolean observer = (boolean) message.getData();
        String s = observer ? "observer" : "player";
        Logger.getInstance().log(String.format("Joined room as %s!", s));

        // set state depending if player or not
        if (!observer) {
            currentState = stateLobby;
            menu();
        } else {
            currentState = stateObserver;
        }
    }

    // on register combination message received from server
    private void onMessageRegister(Message message) {
        // parse combination details
        CombinationDetails combinationDetails = (CombinationDetails) message.getData();
        currentCombinations = combinationDetails;
        Logger.getInstance().log(combinationDetails.getText());

        // get user input
        currentState = stateRegisterDice;
        menu();
    }

    // on round message received from server
    private void onMessageRound(Message message) {
        // parse round details.
        RoundDetails roundDetails = (RoundDetails) message.getData();
        String s = roundDetails.getText();
        Logger.getInstance().log(s);
        currentDetails = roundDetails;

        // is client the current player?
        if (!roundDetails.isRequestingInput()) {
            return;
        }

        // let player hold dice or not
        int round = currentDetails.getRound();
        if (round <= gameSettings.getNumberOfThrows()) {
            currentState = stateRollDice;
        } else {
            currentState = stateRegisterDice;
        }

        menu();
    }

    // print text messages from server
    private void onMessageText(Message message) {
        String s = (String) message.getData();
        Logger.getInstance().log(s);
        menu();
    }

    // pretty print state
    private void printState(State state) {
        for (int i = 0; i < state.getChoices().size(); i++) {
            Choice c = state.getChoices().get(i);
            int input = i + 1;
            System.out.println(String.format("%d. %s", input, c.getText()));
        }
    }

    // read messages from server
    private void readMessages() {
        if (!isConnected()) {
            return;
        }
        // poll session queue for messages
        var queue = session.getMessages();
        while (!queue.isEmpty()) {
            Message message = queue.poll();
            MessageType type = message.getType();
            switch (type) {
                case Text:
                    onMessageText(message);
                    break;
                case JoinRoom:
                    onMessageJoinRoom(message);
                    break;
                case Round:
                    onMessageRound(message);
                    break;
                case RegisterCombination:
                    onMessageRegister(message);
                    break;
                case GameOver:
                    onMessageGameOver(message);
                    break;
            }
        }
    }

    // notify player ready
    private void readyUp(Integer value) {
        System.out.println("Waiting for other players to ready up...");
        sendReadyUp();
    }

    // register score combination
    private void registerDice(Integer value) {
        System.out.print("Enter index for combination: ");
        scanner.nextLine();
        int choice = -1;
        while (choice < 0) {
            int i = scanner.nextInt() - 1;
            if (i >= 0 && i < currentCombinations.getSize()) {
                choice = i;
                sendMessage(MessageType.RegisterCombination, choice);
            }
        }
    }

    // roll player dice
    private void rollDice(Integer value) {
        int round = currentDetails.getRound();
        System.out.println(String.format("Round: %d", round));

        HashSet<Integer> diceToKeep = new HashSet();
        // keep dice if not first throw
        if (round > 0) {
            System.out.print("Enter dice to keep (index 1-5): ");
            scanner.nextLine();
            String input = scanner.nextLine();

            // parse user input
            var dice = input.split(",");
            for (String s : dice) {
                s = s.trim();
                try {
                    int i = Integer.parseInt(s) - 1;
                    diceToKeep.add(i);

                } catch (Exception ignored) {
                }
            }
        }
        // send input to server
        sendMessage(MessageType.Dice, diceToKeep);
    }

    // search for rooms on server
    private void searchForRooms(Integer value) {
        if (!isConnected()) {
            return;
        }
        System.out.println("Searching for rooms...");
        sendSearchForRooms();
    }

    private void sendJoinRoom(String name) {
        sendMessage(MessageType.JoinRoom, name);
    }

    // send a message to server
    private void sendMessage(MessageType messageType, Object data) {
        Message message = new Message(messageType, data);
        session.send(message);
    }

    private void sendObserveRoom(String name) {
        sendMessage(MessageType.ObserveRoom, name);
    }
}
