package Game;

import java.util.HashMap;

// Collection of high score and game rooms
public class GameCollection {
    private Pair highScore;
    private HashMap<String, GameRoom> gameRoomMap;

    public GameCollection() {
        gameRoomMap = new HashMap<>();
    }

    // add game room to data
    public boolean addGameRoom(String name) {
        if (gameRoomMap.containsKey(name)) {
            return false;
        }
        gameRoomMap.put(name, new GameRoom());
        return true;
    }

    public HashMap<String, GameRoom> getGameRooms() {
        return gameRoomMap;
    }

    public Pair getHighScore() {
        return highScore;
    }

    public void setHighScore(Pair highScore) {
        this.highScore = highScore;
    }
}
