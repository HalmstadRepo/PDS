package Test;

import Game.GameRoom;
import Game.GameService;
import Game.GameStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class GameServiceTest {
    @Test
    public void leave() {
        GameService gameService = new GameService();
        gameService.getRandom().setSeed(42);

        String name = "hello";
        gameService.createRoom(name);
        GameRoom gameRoom = gameService.getGameRoomByName(name);

        gameService.joinRoom((long) 1, gameRoom, false);
        gameService.joinRoom((long) 2, gameRoom, false);

        gameService.setPlaying(gameRoom);
        Assert.assertEquals(GameStatus.Playing, gameRoom.getGameStatus());

        long current = gameRoom.getCurrentPlayerThisTurn();
        Assert.assertEquals(2, current);

        gameService.leaveRoom(1);

        current = gameRoom.getCurrentPlayerThisTurn();
        Assert.assertEquals(2, current);

        gameService.leaveRoom(2);
        Assert.assertEquals(GameStatus.Lobby, gameRoom.getGameStatus());
    }

    @Test
    public void observer() {
        GameService gameService = new GameService();
        gameService.getRandom().setSeed(42);

        String name = "hello";
        gameService.createRoom(name);
        GameRoom gameRoom = gameService.getGameRoomByName(name);

        gameService.joinRoom((long) 1, gameRoom, false);
        gameService.joinRoom((long) 2, gameRoom, true);
        gameService.joinRoom((long) 3, gameRoom, true);
        gameService.joinRoom((long) 4, gameRoom, false);
        gameService.setPlaying(gameRoom);
        Assert.assertEquals(GameStatus.Playing, gameRoom.getGameStatus());

        gameService.leaveRoom(1);
        Assert.assertEquals(GameStatus.Playing, gameRoom.getGameStatus());

        gameService.leaveRoom(4);
        Assert.assertEquals(GameStatus.Lobby, gameRoom.getGameStatus());

        var playingPlayers = gameService.getPlayerCountExcludeObservers(gameRoom);
        Assert.assertEquals(2, playingPlayers);

        gameService.leaveRoom(2);
        gameService.leaveRoom(3);
        Assert.assertEquals(GameStatus.Lobby, gameRoom.getGameStatus());
    }

    @Test
    public void gameOver() {
        GameService gameService = new GameService();
        gameService.getRandom().setSeed(42);
        String name = "hello";

        gameService.createRoom(name);
        GameRoom gameRoom = gameService.getGameRoomByName(name);

        gameService.joinRoom((long) 1, gameRoom, false);
        gameService.joinRoom((long) 2, gameRoom, true);
        gameService.setPlaying(gameRoom);

        var player = gameRoom.getPlayers().get((long) 1);
        var combinations = player.getCombinations();
        for (var c : combinations){
            c.setScore(1);
        }

        Assert.assertEquals(GameStatus.Playing, gameRoom.getGameStatus());
        gameService.playNextTurn(gameRoom);

        Assert.assertEquals(GameStatus.Lobby, gameRoom.getGameStatus());
        gameService.setPlaying(gameRoom);
        long current = gameRoom.getCurrentPlayerThisTurn();
        Assert.assertEquals(1, current);
    }

    @Test
    public void turns() {
        GameService gameService = new GameService();
        gameService.getRandom().setSeed(42);
        String name = "hello";

        gameService.createRoom(name);
        GameRoom gameRoom = gameService.getGameRoomByName(name);

        gameService.joinRoom((long) 1, gameRoom, false);
        gameService.joinRoom((long) 2, gameRoom, false);
        gameService.joinRoom((long) 3, gameRoom, true);

        long current = gameRoom.getCurrentPlayerThisTurn();
        Assert.assertEquals(-1, current);

        gameService.randomizeInitiative(gameRoom);

        var order = gameRoom.getPlayerOrder();
        long first = order.peek();
        Assert.assertEquals(2, first);

        gameRoom.nextTurn();
        current = gameRoom.getCurrentPlayerThisTurn();
        Assert.assertEquals(2, current);

        gameRoom.nextTurn();
        current = gameRoom.getCurrentPlayerThisTurn();
        Assert.assertEquals(1, current);

        gameService.playNextTurn(gameRoom);
        current = gameRoom.getCurrentPlayerThisTurn();
        Assert.assertEquals(2, current);
    }
}