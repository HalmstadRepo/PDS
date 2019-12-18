package Game.Combinations;

import java.util.HashMap;

public class FullHouse extends Combination{
    @Override
    public int calculateScore(int[] dice) {
        HashMap<Integer, Integer> map = gatherValues(dice);
        boolean three = map.containsValue(3);
        boolean two = map.containsValue(2);
        if (!three || !two){
            return 0;
        }
        return 25;
    }
    @Override
    public String getName() {
        return "Full House";
    }
}
