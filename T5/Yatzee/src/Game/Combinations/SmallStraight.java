package Game.Combinations;

import java.util.Arrays;

public class SmallStraight extends Combination {

    @Override
    public int calculateScore(int[] dice) {
        int min = Arrays.stream(dice)
                .min()
                .getAsInt();

        for (int i = 0; i < 4; i++) {
            int value = min + i;
            boolean contains = Arrays.stream(dice).anyMatch(t -> t == value);
            if (!contains) {
                return 0;
            }
        }

        return 30;
    }

    @Override
    public String getName() {
        return "Small Straight";
    }
}
