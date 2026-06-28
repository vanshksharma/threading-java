package chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Start chat server
        System.out.println("[Server] Starting on port 8080");
        ChatServer server = new ChatServer(8080);
        server.start();
        Thread.sleep(500);

        // Scenario 1 — two clients join same room and chat
        System.out.println("\n--- Scenario 1: Two clients join same room and chat ---");
        ChatClient alice = new ChatClient("localhost", 8080, "Alice");
        ChatClient bob = new ChatClient("localhost", 8080, "Bob");

        alice.start(List.of(
            "JOIN general",
            "MSG Hello everyone!",
            "MSG How is everyone doing?",
            "QUIT"
        ));

        bob.start(List.of(
            "JOIN general",
            "MSG Hi Alice!",
            "MSG I am doing great!",
            "QUIT"
        ));

        Thread.sleep(4000);

        // Scenario 2 — client sends message without joining a room
        System.out.println("\n--- Scenario 2: Message without joining room ---");
        ChatClient charlie = new ChatClient("localhost", 8080, "Charlie");
        charlie.start(List.of(
            "MSG this should fail",
            "QUIT"
        ));

        Thread.sleep(2000);

        // Scenario 3 — client joins and leaves a room
        System.out.println("\n--- Scenario 3: Join and leave room ---");
        ChatClient dave = new ChatClient("localhost", 8080, "Dave");
        dave.start(List.of(
            "JOIN general",
            "MSG I am Dave, just passing through",
            "LEAVE",
            "MSG this should fail after leaving",
            "QUIT"
        ));

        Thread.sleep(3000);

        // Scenario 4 — multiple rooms, messages stay within room
        System.out.println("\n--- Scenario 4: Multiple rooms ---");
        ChatClient eve = new ChatClient("localhost", 8080, "Eve");
        ChatClient frank = new ChatClient("localhost", 8080, "Frank");
        ChatClient grace = new ChatClient("localhost", 8080, "Grace");

        eve.start(List.of(
            "JOIN room1",
            "MSG Hello from room1!",
            "QUIT"
        ));

        frank.start(List.of(
            "JOIN room2",
            "MSG Hello from room2!",
            "QUIT"
        ));

        grace.start(List.of(
            "JOIN room1",
            "MSG Grace here in room1",
            "QUIT"
        ));

        Thread.sleep(3000);

        // Scenario 5 — client tries to join two rooms
        System.out.println("\n--- Scenario 5: Join two rooms ---");
        ChatClient henry = new ChatClient("localhost", 8080, "Henry");
        henry.start(List.of(
            "JOIN room1",
            "JOIN room2",  // should get error
            "QUIT"
        ));

        Thread.sleep(2000);

        // Scenario 6 — many concurrent clients (virtual thread stress test)
        System.out.println("\n--- Scenario 6: Many concurrent clients ---");
        List<ChatClient> clients = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ChatClient client = new ChatClient("localhost", 8080, "User" + i);
            clients.add(client);
            client.start(List.of(
                "JOIN stress-test",
                "MSG Hello from User" + i,
                "QUIT"
            ));
        }

        Thread.sleep(5000);

        // Scenario 7 — invalid command
        System.out.println("\n--- Scenario 7: Invalid command ---");
        ChatClient ivan = new ChatClient("localhost", 8080, "Ivan");
        ivan.start(List.of(
            "JOIN general",
            "INVALID this is not a command",
            "QUIT"
        ));

        Thread.sleep(2000);

        System.out.println("\n[Server] All scenarios done. Shutting down.");
        server.stop();
    }
}
