package Game;

// key value pair <Long, Integer>
public class Pair {
    private long key;
    private int value;

    public Pair(long key, int value) {
        this.key = key;
        this.value = value;
    }

    public long getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
