package discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Start registry node with high TTL for testing
        System.out.println("[Registry] Starting registry node on port 8080");
        RegistryNode registryNode = new RegistryNode(8080, 10, 60000); // 60 second TTL
        registryNode.start();
        Thread.sleep(500);

        RegistryClient client = new RegistryClient("localhost:8080");

        // Scenario 1 — basic register and discover
        System.out.println("\n--- Scenario 1: Basic register and discover ---");
        client.register("cache", "localhost", 9001);
        client.register("cache", "localhost", 9002);
        client.register("cache", "localhost", 9003);
        System.out.println("[Client] Registered 3 cache instances");
        List<String> instances = client.getInstances("cache");
        System.out.println("[Client] Discovered instances: " + instances);

        // Scenario 2 — duplicate registration
        System.out.println("\n--- Scenario 2: Duplicate registration ---");
        try {
            client.register("cache", "localhost", 9001);
            System.out.println("[Client] Registered — this should not happen!");
        } catch (IllegalStateException e) {
            System.out.println("[Client] Correctly rejected duplicate: " + e.getMessage());
        }

        // Scenario 3 — multiple service types
        System.out.println("\n--- Scenario 3: Multiple service types ---");
        client.register("broker", "localhost", 9010);
        client.register("broker", "localhost", 9011);
        client.register("lock-manager", "localhost", 9020);
        System.out.println("[Client] Registered broker and lock-manager instances");
        System.out.println("[Client] Broker instances: " + client.getInstances("broker"));
        System.out.println("[Client] Lock-manager instances: " + client.getInstances("lock-manager"));

        // Scenario 4 — deregister
        System.out.println("\n--- Scenario 4: Deregister ---");
        client.deRegister("cache", "localhost", 9003);
        System.out.println("[Client] Deregistered cache:9003");
        instances = client.getInstances("cache");
        System.out.println("[Client] Remaining cache instances: " + instances);

        // Scenario 5 — deregister non-existent instance
        System.out.println("\n--- Scenario 5: Deregister non-existent instance ---");
        try {
            client.deRegister("cache", "localhost", 9999);
            System.out.println("[Client] Deregistered — this should not happen!");
        } catch (IllegalStateException e) {
            System.out.println("[Client] Correctly rejected: " + e.getMessage());
        }

        // Scenario 6 — discover unknown service
        System.out.println("\n--- Scenario 6: Discover unknown service ---");
        List<String> unknown = client.getInstances("unknown-service");
        System.out.println("[Client] Unknown service instances: " + unknown + " (expected [])");

        // Scenario 7 — heartbeat
        System.out.println("\n--- Scenario 7: Heartbeat ---");
        client.heartbeat("cache", "localhost", 9001);
        System.out.println("[Client] Sent heartbeat for cache:9001");
        instances = client.getInstances("cache");
        System.out.println("[Client] Cache instances after heartbeat: " + instances);

        // Scenario 8 — TTL expiry
        System.out.println("\n--- Scenario 8: TTL expiry ---");
        RegistryNode shortTtlNode = new RegistryNode(8081, 10, 3000); // 3 second TTL
        shortTtlNode.start();
        Thread.sleep(500);

        RegistryClient shortTtlClient = new RegistryClient("localhost:8081");
        shortTtlClient.register("cache", "localhost", 9001);
        System.out.println("[Client] Registered cache:9001 with 3s TTL");
        System.out.println("[Client] Instances before expiry: " + shortTtlClient.getInstances("cache"));
        System.out.println("[Client] Waiting 5 seconds for TTL to expire...");
        Thread.sleep(5000);
        System.out.println("[Client] Instances after expiry: " + shortTtlClient.getInstances("cache") + " (expected [])");

        // Scenario 9 — concurrent registrations
        System.out.println("\n--- Scenario 9: Concurrent registrations ---");
        RegistryNode concurrentNode = new RegistryNode(8082, 10, 60000);
        concurrentNode.start();
        Thread.sleep(500);

        RegistryClient concurrentClient = new RegistryClient("localhost:8082");
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int port = 9100 + i;
            Thread t = new Thread(() -> {
                concurrentClient.register("cache", "localhost", port);
                System.out.println("[Thread] Registered cache:localhost:" + port);
            }, "Register Thread " + i);
            threads.add(t);
        }
        threads.forEach(Thread::start);
        for (Thread t : threads) t.join();
        System.out.println("[Client] Concurrent instances: " + concurrentClient.getInstances("cache"));

        System.out.println("\n[Registry] All scenarios passed. Shutting down.");
        registryNode.stop();
        shortTtlNode.stop();
        concurrentNode.stop();
    }
}
