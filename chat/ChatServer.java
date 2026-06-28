package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private final Map<String, Room> rooms;
    private final ServerSocket socket;

    public ChatServer(int port) throws IOException {
        this.rooms = new ConcurrentHashMap<>();
        this.socket = new ServerSocket(port);
    }

    private Runnable createRunnable(Socket socket){
        return () -> {
            try{
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String username = reader.readLine().trim();
                    ClientHandler handler = new ClientHandler(socket, username, this);
                    handler.run();
                }
            } catch (IOException ex) {
                System.out.println("Failed to create connection");
            } catch (Exception e){
                System.out.println("Server Error " + e.getMessage());
            }
        };
    }

    private void listen() throws IOException{
        while(!socket.isClosed()){
            Socket client = socket.accept();
            Thread.ofVirtual().start(createRunnable(client));
        }
    }

    public void start(){
        Thread.ofVirtual().name("Server Thread").start(() -> {
            try {
                listen();
            } catch (IOException e) {
                System.out.println("Failed to start server");
            }
        });
    }

    public Room getOrCreateRoom(String name){
        return rooms.computeIfAbsent(name, Room::new);
    }

    public void stop() throws IOException{
        socket.close();
    }
}
