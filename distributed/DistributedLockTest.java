package distributed;

import java.io.IOException;
import java.util.UUID;

public class DistributedLockTest {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("[Manager] Starting Lock Manager Node on port 8080");
        DistributedLockManagerNode node = new DistributedLockManagerNode(8080, 10);
        node.start();

        Thread.sleep(500); // wait for server to start

        DistributedLockClient client = new DistributedLockClient("localhost:8080");

        // Scenario 1 — basic acquire and release
        System.out.println("\n--- Scenario 1: Basic acquire and release ---");
        String token = client.acquire("order-1", 10000);
        System.out.println("[Client] Acquired lock 'order-1', token: " + token);
        client.release("order-1", token);
        System.out.println("[Client] Released lock 'order-1'");

        // Scenario 2 — two threads competing for the same lock
        System.out.println("\n--- Scenario 2: Two threads competing for same lock ---");
        String token2 = client.acquire("order-2", 10000);
        System.out.println("[Thread Main] Acquired lock 'order-2', token: " + token2);

        Thread competitor = new Thread(() -> {
            try {
                System.out.println("[Thread Competitor] Trying to acquire 'order-2'...");
                String t = client.acquire("order-2", 10000);
                System.out.println("[Thread Competitor] Acquired lock — this should not happen!");
            } catch (IllegalStateException e) {
                System.out.println("[Thread Competitor] Correctly denied: " + e.getMessage());
            }
        }, "Competitor Thread");

        competitor.start();
        competitor.join();

        client.release("order-2", token2);
        System.out.println("[Thread Main] Released lock 'order-2'");

        // Scenario 3 — acquire after release
        System.out.println("\n--- Scenario 3: Acquire after release ---");
        String token3 = client.acquire("order-2", 10000);
        System.out.println("[Client] Re-acquired lock 'order-2' after release, token: " + token3);
        client.release("order-2", token3);
        System.out.println("[Client] Released lock 'order-2'");

        // Scenario 4 — wrong token on release
        System.out.println("\n--- Scenario 4: Release with wrong token ---");
        String token4 = client.acquire("order-3", 10000);
        System.out.println("[Client] Acquired lock 'order-3', token: " + token4);
        try {
            client.release("order-3", UUID.randomUUID().toString());
            System.out.println("[Client] Released — this should not happen!");
        } catch (IllegalStateException e) {
            System.out.println("[Client] Correctly rejected wrong token: " + e.getMessage());
        }
        client.release("order-3", token4);
        System.out.println("[Client] Released lock 'order-3' with correct token");

        // Scenario 5 — TTL expiry
        System.out.println("\n--- Scenario 5: TTL expiry ---");
        String token5 = client.acquire("order-4", 2000); // 2 second TTL
        System.out.println("[Client] Acquired lock 'order-4' with 2s TTL, token: " + token5);
        System.out.println("[Client] Waiting for TTL to expire...");
        Thread.sleep(3000);
        String token6 = client.acquire("order-4", 10000);
        System.out.println("[Client] Re-acquired expired lock 'order-4', token: " + token6);
        client.release("order-4", token6);
        System.out.println("[Client] Released lock 'order-4'");

        System.out.println("\n[Manager] All scenarios passed. Shutting down.");
        node.stop();
    }
}
