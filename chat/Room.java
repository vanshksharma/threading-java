package chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final String name;
    private final Map<String, ClientHandler> clients;

    public Room(String name) {
        this.name = name;
        this.clients = new ConcurrentHashMap<>();
    }

    public void addClient(ClientHandler handler){
        clients.putIfAbsent(handler.getUsername(), handler);
    }

    public void removeClient(String username){
        clients.remove(username);
    }

    public void broadcast(String message, String sender){
        clients.values().forEach((handler) -> {
            if(!handler.getUsername().equals(sender)){
                handler.getWriter().println(message);
            }
        });
    }

    public String getName() {
        return name;
    }

    public int getClientCount(){
        return clients.size();
    }
}
