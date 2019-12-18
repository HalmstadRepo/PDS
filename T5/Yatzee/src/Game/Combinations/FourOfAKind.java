package Game.Combinations;

import java.util.HashMap;

public class FourOfAKind extends Combination{
    @Override
    public int calculateScore(int[] dice) {
        HashMap<Integer, Integer> map = gatherValues(dice);

        int bestScore = 0;
        int target = 4;

        for (var key : map.keySet()){
            int amount = map.get(key);
            if (amount >= target){
                int s = target * key;
                if (s >= bestScore){
                    bestScore = s;
                }
            }
        }

        return bestScore;
    }



    @Override
    public String getName() {
        return "Four of a kind";
    }
}
