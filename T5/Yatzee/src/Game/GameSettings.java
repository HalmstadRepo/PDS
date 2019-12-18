package Game;

public class GameSettings {
    private int numberOfThrows;
    private int numberOfDice;

    public GameSettings(){
        numberOfThrows = 2;
        numberOfDice = 5;
    }

    public int getNumberOfDice() {
        return numberOfDice;
    }

    public int getNumberOfThrows() {
        return numberOfThrows;
    }
}
