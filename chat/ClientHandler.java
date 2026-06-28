package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler {
    private final String username;
    private final Socket socket;
    private final PrintWriter writer;
    private Room room;
    private final BufferedReader reader;
    private final ChatServer server;

    public Socket getSocket() {
        return socket;
    }

    public ClientHandler(Socket socket, String username, ChatServer server) throws IOException {
        this.socket = socket;
        this.username = username;
        this.server = server;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.room = null;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void run() throws IOException{
        try {
            String line;
            while((line = reader.readLine()) != null){
                String[] brokenLine = line.split(" ");
                String command = brokenLine[0];
                switch (command) {
                    case "JOIN" -> {
                        if(room != null){
                            writer.println("ERROR, you have already joined a room");
                        }
                        else{
                            this.room = server.getOrCreateRoom(brokenLine[1]);
                            room.addClient(this);
                            writer.println("JOINED " + room.getName());
                            room.broadcast(username + " has joined the room", username);
                        }
                    }
                    case "LEAVE" -> {
                        if(room == null) writer.println("ERROR, you have not joined any room");
                        else{
                            String roomName = room.getName();
                            room.removeClient(username);
                            room = null;
                            writer.println("LEFT " + roomName);
                        }
                    }
                    case "MSG" -> {
                        if (room == null) writer.println("ERROR you must join a room first");
                        else{
                            String completeMsg = line.substring("MSG ".length());
                            room.broadcast("MSG " + username + ": " + completeMsg, username);
                        }
                    }
                    case "QUIT" -> {
                        return;
                    }

                    default -> throw new RuntimeException("Invalid Command");
                }
            }    
        }
        finally{
            if (room != null) {
                room.broadcast(username + " has left the room", username);
                room.removeClient(username);
            }
            writer.close();
            reader.close();
            socket.close();
        }
    }

    public String getUsername() {
        return username;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public Room getRoom() {
        return room;
    }
}
