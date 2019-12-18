package Core;

import java.io.Serializable;

// message that is sent between server and client
// contains a type that controls flow and a data container
public class Message implements Serializable {
    private MessageType type;
    private Object data;

    public Message(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
