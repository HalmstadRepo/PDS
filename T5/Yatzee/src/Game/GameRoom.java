package Game;

import java.util.*;

// Contains player and player turn order.
public class GameRoom {
    private GameStatus gameStatus;
    private HashMap<Long, PlayerData> players;
    private Queue<Long> playerQueue;
    private Queue<Long> playerOrder;

    private long currentPlayerId;

    public GameRoom() {
        players = new HashMap<>();
        gameStatus = GameStatus.Lobby;
        playerQueue = new LinkedList<>();
        playerOrder = new LinkedList<>();

        reset();
    }

    public long getCurrentPlayerThisTurn() {
        return currentPlayerId;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Queue<Long> getPlayerOrder() {
        return playerOrder;
    }

    public Queue<Long> getPlayerQueue() {
        return playerQueue;
    }

    public HashMap<Long, PlayerData> getPlayers() {
        return players;
    }

    // process next game turn, returns false if game is done
    public boolean nextTurn() {
        if (playerOrder.isEmpty()) {
            return false;
        }

        long playerId = getCurrentPlayerThisTurn();
        PlayerData current = getPlayers().getOrDefault(playerId, null);

        // reset throws for last player
        if (current != null) {
            current.reset();
        }

        // add current player, last to turn queue
        if (currentPlayerId >= 0) {
            Long player = playerOrder.poll();
            PlayerData playerLast = getPlayers().get(player);
            if (playerLast != null && playerLast.hasValidMoves()) {
                playerOrder.offer(player);
            }
        }

        // ensure next player is connected
        while (!playerOrder.isEmpty()){
            long nextId = playerOrder.peek();
            if (!players.containsKey(nextId)){
                playerOrder.poll();
            }
            else{
                break;
            }
        }

        // set next player
        if (playerOrder.isEmpty()) {
            return false;
        } else {
            currentPlayerId = playerOrder.peek();

            return true;
        }
    }

    // reset state
    public void reset() {
        currentPlayerId = -1;
        playerOrder.clear();
        getPlayers().forEach((id, playerData)-> playerData.setReady(false));
    }
}
