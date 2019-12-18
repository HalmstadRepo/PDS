package Test;

import Game.GameService;
import Game.PlayerData;
import org.junit.jupiter.api.Test;

public class FormatTest {

    @Test
    public void combinations() {
        PlayerData p = new PlayerData(false);
        var c = p.getCombinations();
        c.get(5).setScore(100);
        var s = GameService.getCombinationsText(c);
        System.out.println(s);
    }
}