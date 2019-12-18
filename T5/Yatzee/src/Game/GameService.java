package Game;

import Core.Logger;
import Game.Combinations.Combination;

import java.util.*;
import java.util.stream.Collectors;

public class GameService {
    private Random random;
    private GameCollection gameCollection;
    private GameSettings gameSettings;
    private List<IGameSubscriber> subscribers;

    public GameService() {
        gameSettings = new GameSettings();
        gameCollection = new GameCollection();
        subscribers = new ArrayList<>();
        random = new Random();
    }

    // get combination pretty print
    public static StringBuilder getCombinationsText(List<Combination> combinations) {
        StringBuilder sb = new StringBuilder("\nName / Current / Preview\n");
        for (int i = 0; i < combinations.size(); i++) {
            Combination t = combinations.get(i);
            String s = String.format("%2d: %16s\t%6s %6s\n", i + 1, t.getName(), t.getScore(), t.getPreviewScore());
            sb.append(s);
        }
        return sb;
    }

    // create a room and add to data collection
    public boolean createRoom(String name) {
        boolean roomCreated = gameCollection.addGameRoom(name);
        if (roomCreated) {
            // make rooms start in lobby
            GameRoom gameRoom = getGameRoomByName(name);
            setLobby(gameRoom);
            Logger.getInstance().log(String.format("Created room: %s", name));
            return true;
        }
        return false;
    }

    // is room empty or has only observers?
    public boolean emptyOrOnlyObservers(GameRoom gameRoom) {
        boolean emptyOrOnlyObservers = true;

        var players = gameRoom.getPlayers();
        if (!players.isEmpty()) {
            for (var value : players.values()) {
                if (!value.isObserver()) {
                    emptyOrOnlyObservers = false;
                    break;
                }
            }
        }

        return emptyOrOnlyObservers;
    }

    // find the game room that contains player
    public GameRoom findGameRoomWithClientId(long clientId) {
        var rooms = getGameCollection().getGameRooms();
        for (var room : rooms.values()) {
            if (room.getPlayers().containsKey(clientId)) {
                return room;
            }
        }
        return null;
    }

    // determine winner and reset game on game over
    public void gameOver(GameRoom gameRoom) {
        var players = gameRoom.getPlayers();
        var playerIds = new ArrayList<>(players.keySet());

        // Determine winner
        long bestId = -1;
        int bestScore = -1;

        for (var pair : players.entrySet()) {
            int score = pair.getValue().getSumScore();
            if (score > bestScore) {
                bestId = pair.getKey();
                bestScore = score;
            }
        }

        // determine if winner is placed on highscore
        String s = String.format("Game over! Player %o was victorious with a score of %d!\n", bestId, bestScore);
        var hs = gameCollection.getHighScore();
        if (hs == null || bestScore > hs.getValue()) {
            gameCollection.setHighScore(new Pair(bestId, bestScore));
            s += String.format("Player %o was placed on the high score!\n", bestId);
        }

        // send message that game is over
        sendGameOver(playerIds, s);
        setLobby(gameRoom);
    }

    public GameCollection getGameCollection() {
        return gameCollection;
    }

    public GameRoom getGameRoomByName(String name) {
        return gameCollection.getGameRooms().getOrDefault(name, null);
    }

    // get players that are not observers
    public int getPlayerCountExcludeObservers(GameRoom gameRoom) {
        return gameRoom.getPlayers()
                .values()
                .stream()
                .filter(t -> !t.isObserver())
                .collect(Collectors.toList())
                .size();
    }

    // get ids of players that aren't observers
    public List<Long> getPlayingPlayerIds(GameRoom gameRoom) {
        List<Long> ids = new ArrayList<>();

        gameRoom.getPlayers().forEach((id, playerData) -> {
            if (!playerData.isObserver()) {
                ids.add(id);
            }
        });

        return ids;
    }

    public Random getRandom() {
        return random;
    }

    // find any room with name
    public boolean hasGameRoomWithName(String name) {
        return gameCollection.getGameRooms().containsKey(name);
    }

    // let client join a room
    public boolean joinRoom(Long clientId, GameRoom gameRoom, boolean isObserver) {
        var players = gameRoom.getPlayers();

        // become observer if wanted to be or game is playing
        boolean joinAsObserver = willBecomeObserver(gameRoom, isObserver);
        players.put(clientId, new PlayerData(joinAsObserver));

        // queue for joining in next game
        if (joinAsObserver) {
            gameRoom.getPlayerQueue().offer(clientId);
        }

        return true;
    }

    // let client with id leave room
    public void leaveRoom(long clientId) {
        GameRoom gameRoom = getRoomByClientId(clientId);
        if (gameRoom == null) {
            return;
        }

        // remove played
        var players = gameRoom.getPlayers();
        players.remove(clientId);


        // if empty, set room to lobby
        if (emptyOrOnlyObservers(gameRoom)) {
            setLobby(gameRoom);
        } else {
            startGameIfPlayersReady(gameRoom);

            // if was leaving players turn, let someone else play
            if (gameRoom.getCurrentPlayerThisTurn() == clientId) {
                playNextTurn(gameRoom);
            }
        }
    }

    // let player roll dice
    public void playDice(Long clientId, HashSet<Integer> diceToKeep) {
        GameRoom gameRoom = getRoomByClientId(clientId);
        if (gameRoom == null) {
            return;
        }
        PlayerData player = gameRoom.getPlayers().get(clientId);

        // get dice roll for player
        int[] playerDice = player.getDice();
        int[] dice = getDiceRolls(gameSettings.getNumberOfDice());
        if (playerDice == null) {
            playerDice = dice;
        }

        // determine which dice player should keep
        int noOfThrows = player.getNumberOfThrows();
        for (int i = 0; i < dice.length; i++) {
            if (noOfThrows > 0) {
                if (diceToKeep.contains(i)) {
                    continue;
                }
            }
            playerDice[i] = dice[i];
        }

        // save dice and increase throws
        player.setDice(playerDice);
        int round = noOfThrows + 1;
        player.setNumberOfThrows(round);

        // send dice to players
        String diceString = Arrays.toString(playerDice);
        if (round <= gameSettings.getNumberOfThrows()) {
            String textOther = String.format("Player %o' has rolled: %s", clientId, diceString);
            String textPlayer = String.format("You rolled: %s", diceString);

            // Send different messages to current player and others
            HashMap<Long, RoundDetails> roundDetails = getRoundDetails(gameRoom, textOther);
            RoundDetails detailsCurrentPlayer = roundDetails.get(clientId);
            detailsCurrentPlayer.setText(textPlayer);
            detailsCurrentPlayer.setRound(round);
            detailsCurrentPlayer.setRequestInput(true);

            sendRoundDetailsToSubs(gameRoom, roundDetails);
        } else {
            // if client is registering score
            // set preview score of current dice
            List<Combination> combinations = player.getCombinations();
            for (Combination combination : combinations) {
                int score = combination.calculateScore(playerDice);
                combination.setPreviewScore(score);
            }
            // send details about final roll
            String text = String.format("Final roll: %s", diceString);
            HashMap<Long, RoundDetails> roundDetails = getRoundDetails(gameRoom, text);
            RoundDetails detailsCurrentPlayer = roundDetails.get(clientId);
            detailsCurrentPlayer.setRound(round);

            // send score combination list to player
            StringBuilder sb = getCombinationsText(combinations);
            CombinationDetails combinationDetails = new CombinationDetails(sb.toString(), combinations.size());

            sendRoundDetailsToSubs(gameRoom, roundDetails);
            subscribers.forEach(t -> t.sendRegisterCombination(clientId, combinationDetails));
        }
    }

    // advance turns
    public void playNextTurn(GameRoom gameRoom) {
        // get next turn, if no players or valid turns => game over
        boolean hasNextTurn = gameRoom.nextTurn();
        if (!hasNextTurn) {
            gameOver(gameRoom);
            return;
        }

        long playerId = gameRoom.getCurrentPlayerThisTurn();

        // send turn details to all players
        String text = String.format("Player %o's turn.", playerId);
        HashMap<Long, RoundDetails> roundDetails = getRoundDetails(gameRoom, text);
        RoundDetails detailsCurrentPlayer = roundDetails.get(playerId);
        detailsCurrentPlayer.setText("Your turn!");
        detailsCurrentPlayer.setRequestInput(true);

        sendRoundDetailsToSubs(gameRoom, roundDetails);
    }

    // randomize which player starts
    public void randomizeInitiative(GameRoom gameRoom) {
        // all players roll dice
        List<Pair> rolls = new ArrayList<>();
        gameRoom.getPlayers().forEach((id, playerData) -> {
            if (!playerData.isObserver()) {
                int[] playerDiceRolls = getDiceRolls(5);
                int score = Arrays.stream(playerDiceRolls).sum();
                rolls.add(new Pair(id, score));
            }
        });

        // order dice
        rolls.sort(Comparator.comparingDouble(Pair::getValue).reversed());

        StringBuilder sb = new StringBuilder("Game started! Player rolls are:\n");
        rolls.forEach(t -> {
            String s = String.format("Player %o: %d\n", t.getKey(), t.getValue());
            sb.append(s);
        });
        sb.append("\n");

        String text = sb.toString();
        HashMap<Long, RoundDetails> roundDetails = getRoundDetails(gameRoom, text);
        Queue<Long> playerOrder = gameRoom.getPlayerOrder();

        // set player order
        rolls.forEach(t -> playerOrder.add(t.getKey()));


        // send details about player order
        Long firstPlayer = playerOrder.peek();
        RoundDetails detailsCurrentPlayer = roundDetails.get(firstPlayer);
        detailsCurrentPlayer.setText(text + "You are starting!\n");
        sendRoundDetailsToSubs(gameRoom, roundDetails);
    }

    // let player ready up
    public void readyUp(Long id) {
        GameRoom gameRoom = findGameRoomWithClientId(id);
        if (gameRoom == null) {
            return;
        }

        // find playerData with id and set ready
        var player = gameRoom.getPlayers().getOrDefault(id, null);
        if (player != null) {
            player.setReady(true);
        }

        // start game room
        startGameIfPlayersReady(gameRoom);
    }

    // start game room if players ready
    public void startGameIfPlayersReady(GameRoom gameRoom) {
        if (gameRoom.getGameStatus() == GameStatus.Lobby) {
            if (areAllPlayersReady(gameRoom)) {
                setPlaying(gameRoom);
            }
        }
    }

    // register score combination
    public void registerCombination(Long id, int index) {
        GameRoom gameRoom = getRoomByClientId(id);
        if (gameRoom == null) {
            return;
        }
        PlayerData player = gameRoom.getPlayers().get(id);
        int[] dice = player.getDice();
        if (dice == null) {
            return;
        }

        // calculate score from dice and set score
        Combination combination = player.getCombinations().get(index);
        int score = combination.calculateScore(dice);
        combination.setScore(score);

        // send details about scoring to all players
        String text = String.format("Player %o's scored %d on %s.", id, score, combination.getName());
        HashMap<Long, RoundDetails> roundDetails = getRoundDetails(gameRoom, text);
        sendRoundDetailsToSubs(gameRoom, roundDetails);

        // play next turn
        playNextTurn(gameRoom);
    }

    // start game room
    public void setPlaying(GameRoom gameRoom) {
        gameRoom.setGameStatus(GameStatus.Playing);
        randomizeInitiative(gameRoom);
        playNextTurn(gameRoom);
    }

    // add listener
    public void subscribe(IGameSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    // determine new player status
    public boolean willBecomeObserver(GameRoom gameRoom, boolean isObserver) {
        return gameRoom.getGameStatus() == GameStatus.Playing || isObserver;
    }

    // check if all players are ready
    private boolean areAllPlayersReady(GameRoom gameRoom) {
        var players = gameRoom.getPlayers();
        for (PlayerData player : players.values()) {
            if (!player.isObserver() && !player.isReady()) {
                return false;
            }
        }
        return true;
    }

    // get random dice
    private int[] getDiceRolls(int numberOfDice) {
        int[] dice = new int[numberOfDice];
        for (int i = 0; i < numberOfDice; i++) {
            dice[i] = random.nextInt(6) + 1;
        }
        return dice;
    }

    // get game room by client id
    private GameRoom getRoomByClientId(Long id) {
        HashMap<String, GameRoom> rooms = gameCollection.getGameRooms();
        for (GameRoom room : rooms.values()) {
            HashMap<Long, PlayerData> players = room.getPlayers();
            if (players.containsKey(id)) {
                return room;
            }
        }
        return null;
    }

    // get a map of empty round details for all players
    private HashMap<Long, RoundDetails> getRoundDetails(GameRoom gameRoom, String text) {
        HashMap<Long, RoundDetails> roundDetails = new HashMap<>();
        HashMap<Long, PlayerData> players = gameRoom.getPlayers();

        // create a round details for every player with a text
        for (Long playerId : players.keySet()) {
            RoundDetails details = new RoundDetails(false, text);
            roundDetails.put(playerId, details);
        }

        return roundDetails;
    }

    // send game over to players
    private void sendGameOver(List<Long> players, String text) {
        subscribers.forEach(t -> t.sendGameOver(players, text));
    }

    // send round details to players
    private void sendRoundDetailsToSubs(GameRoom gameRoom, HashMap<Long, RoundDetails> roundDetails) {
        subscribers.forEach(t -> t.sendRound(gameRoom, roundDetails));
    }

    // send welcome to observers on new game
    private void sendWelcomeToObservers(List<Long> players) {
        subscribers.forEach(t -> t.sendObserversWelcome(players));
    }

    // set game room to lobby
    private void setLobby(GameRoom gameRoom) {
        // reset game room
        gameRoom.setGameStatus(GameStatus.Lobby);
        gameRoom.reset();

        // let waiting players join
        var queue = gameRoom.getPlayerQueue();
        while (!queue.isEmpty()) {
            Long p = queue.poll();
            joinRoom(p, gameRoom, false);
        }

        // send welcome to to waiting players
        var playersIds = getPlayingPlayerIds(gameRoom);
        sendWelcomeToObservers(playersIds);
    }
}
