package Game.Combinations;

public class Ones extends Combination {

    @Override
    public int calculateScore(int[] dice) {
        int s = 0;
        int value = 1;
        for (int i = 0; i < dice.length; i++){
            if (dice[i] == value){
                s += value;
            }
        }
        return s;
    }

    @Override
    public String getName() {
        return "Ones";
    }
}
