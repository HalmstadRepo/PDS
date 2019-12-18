package Game;

import java.io.Serializable;

// Data container for sending score combination over network
public class CombinationDetails implements Serializable {
    private String text;
    private int size;

    public CombinationDetails(String text, int size) {
        this.text = text;
        this.size = size;
    }

    public String getText() {
        return text;
    }

    public int getSize() {
        return size;
    }
}
