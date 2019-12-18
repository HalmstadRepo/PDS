package Game;

import Game.Combinations.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerData implements Serializable {
    private boolean isObserver;
    private boolean ready;
    private int[] dice;
    private int numberOfThrows;
    private List<Combination> combinations;

    public PlayerData(boolean isObserver) {
        this.isObserver = isObserver;

        // add all possible combinations
        combinations = new ArrayList<>(){{
            add(new Ones());
            add(new Twos());
            add(new Threes());
            add(new Fours());
            add(new Fives());
            add(new Sixes());
            add(new ThreeOfAKind());
            add(new FourOfAKind());
            add(new FullHouse());
            add(new SmallStraight());
            add(new LargeStraight());
            add(new Chance());
            add(new Yahtzee());
        }};
    }

    public List<Combination> getCombinations() {
        return combinations;
    }

    public void setCombinations(List<Combination> combinations) {
        this.combinations = combinations;
    }

    public int[] getDice() {
        return dice;
    }

    public void setDice(int[] dice) {
        this.dice = dice;
    }

    public int getNumberOfThrows() {
        return numberOfThrows;
    }

    public void setNumberOfThrows(int numberOfThrows) {
        this.numberOfThrows = numberOfThrows;
    }

    public  int getSumScore(){
        return combinations.stream()
                .mapToInt(Combination::getScore)
                .sum();

    }

    public boolean hasValidMoves(){
        return combinations
                .stream()
                .anyMatch(t -> t.getScore() == 0);
    }

    public boolean isObserver() {
        return isObserver;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean value) {
        ready = value;
    }

    public void reset() {
        dice = null;
        numberOfThrows = 0;
    }
}
