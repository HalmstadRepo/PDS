package Game;

import java.io.Serializable;

// Details about a round
public class RoundDetails implements Serializable {
    private boolean requestInput;
    private String text;
    private int round;

    public RoundDetails(boolean yourTurn, String text) {
        this.requestInput = yourTurn;
        this.text = text;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public boolean isRequestingInput() {
        return requestInput;
    }

    public String getText() {
        return text;
    }

    public void setRequestInput(boolean yourTurn) {
        this.requestInput = yourTurn;
    }

    public void setText(String text) {
        this.text = text;
    }
}
