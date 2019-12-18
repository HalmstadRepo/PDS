package Game;

import java.util.HashMap;
import java.util.List;

public interface IGameSubscriber {

    void sendRegisterCombination(Long clientId, CombinationDetails combinations);

    void sendRound(GameRoom gameRoom, HashMap<Long, RoundDetails> roundDetails);

    void sendObserversWelcome(List<Long> clients);

    void sendGameOver(List<Long> clients, String text);
}
