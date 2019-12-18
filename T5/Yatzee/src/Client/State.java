package Client;

import java.util.ArrayList;
import java.util.List;

// Ui state with user choices
public class State {
    private List<Choice> choices;

    public State(){
        choices = new ArrayList<Choice>();
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void add(Choice choice) {
        getChoices().add(choice);
    }
}
