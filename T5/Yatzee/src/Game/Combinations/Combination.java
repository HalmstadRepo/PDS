package Game.Combinations;

import java.io.Serializable;
import java.util.HashMap;

// Score combination
public abstract class Combination implements Serializable {
    private int score;
    private int previewScore;
    public Combination() {
    }

    public static HashMap<Integer, Integer> gatherValues(int[] dice) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < dice.length; i++){
            int value = dice[i];
            int prev = 0;
            if (map.containsKey(value)){
                prev = map.get(value);
            }
            prev++;
            map.put(value, prev);
        }
        return map;
    }

    public abstract int calculateScore(int[] dice);

    public abstract String getName();

    public int getPreviewScore() {
        return previewScore;
    }

    public void setPreviewScore(int previewScore) {
        this.previewScore = previewScore;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
