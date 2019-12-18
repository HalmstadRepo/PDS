package Game.Combinations;

import java.util.HashMap;

public class Yahtzee extends Combination{
    @Override
    public int calculateScore(int[] dice) {
        HashMap<Integer, Integer> map = gatherValues(dice);
        if (!map.containsValue(5)){
            return 0;
        }
        return 50;
    }
    @Override
    public String getName() {
        return "Yahtzee";
    }
}
