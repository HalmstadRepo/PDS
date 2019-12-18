package Client;

import java.util.function.Consumer;

// Player choice, a text and callback
public class Choice {
    private final String text;
    private final Consumer<Integer> consumer;

    public Choice(String text, Consumer<Integer> consumer) {
        this.text = text;
        this.consumer = consumer;
    }

    public String getText() {
        return text;
    }

    public Consumer<Integer> getConsumer() {
        return consumer;
    }
}
