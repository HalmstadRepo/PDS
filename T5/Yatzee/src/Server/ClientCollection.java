package Server;

import Core.Session;

import java.util.concurrent.ConcurrentHashMap;

// Collection of client sessions
public class ClientCollection {
    private ConcurrentHashMap<Long, Session> sessionMap;
    private long identity;

    public ClientCollection(){
        identity = 1;
        sessionMap = new ConcurrentHashMap<>();
    }

    // add session and give it an id
    public long addSession(Session session) {
        long id = identity;
        sessionMap.put(identity, session);
        identity++;
        return id;
    }
    public void remove(long id){
        sessionMap.remove(id);
    }

    public ConcurrentHashMap<Long, Session> getSessionMap() {
        return sessionMap;
    }
}
