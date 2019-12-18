package Game.Combinations;

import java.util.Arrays;

public class Chance extends Combination {

    @Override
    public int calculateScore(int[] dice) {
        int s = Arrays.stream(dice).sum();
        return s;
    }

    @Override
    public String getName() {
        return "Chance";
    }
}
