package balancer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import cache.CacheNode;
import discovery.RegistryClient;
import discovery.RegistryNode;

public class BalancerTest {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        // Start registry
        System.out.println("[Registry] Starting on port 8080");
        RegistryNode registryNode = new RegistryNode(8080, 10, 60000);
        registryNode.start();
        Thread.sleep(500);

        // Start cache nodes
        System.out.println("[Cache] Starting 3 cache nodes on ports 9001, 9002, 9003");
        CacheNode node1 = new CacheNode(9001, 100, 10, 1);
        CacheNode node2 = new CacheNode(9002, 100, 10, 2);
        CacheNode node3 = new CacheNode(9003, 100, 10, 3);
        node1.start();
        node2.start();
        node3.start();
        Thread.sleep(500);

        // Register cache nodes with registry
        System.out.println("[Registry] Registering cache nodes");
        RegistryClient registryClient = new RegistryClient("localhost:8080");
        registryClient.register("cache", "localhost", 9001);
        registryClient.register("cache", "localhost", 9002);
        registryClient.register("cache", "localhost", 9003);

        // Start load balancer
        System.out.println("[LoadBalancer] Starting on port 7000");
        LoadBalancer loadBalancer = new LoadBalancer("localhost:8080", 7000, 10, 5000);
        loadBalancer.start();
        Thread.sleep(500);

        HttpClient httpClient = HttpClient.newHttpClient();

        // Scenario 1 — verify round robin distribution
        System.out.println("\n--- Scenario 1: Round robin distribution ---");
        for (int i = 1; i <= 9; i++) {
            httpClient.send(
                java.net.http.HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:7000/cache/key" + i))
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofString("value" + i))
                    .build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()
            );
            System.out.println("[Client] PUT key" + i);
        }
        System.out.println("[Node 1] Keys: " + getKeysFromNode("localhost:9001"));
        System.out.println("[Node 2] Keys: " + getKeysFromNode("localhost:9002"));
        System.out.println("[Node 3] Keys: " + getKeysFromNode("localhost:9003"));

        // Scenario 2 — TTL expiry forces cache refresh
        System.out.println("\n--- Scenario 2: Cache TTL refresh ---");
        System.out.println("[Client] Waiting 6 seconds for load balancer cache to expire...");
        Thread.sleep(6000);
        httpClient.send(
            java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:7000/cache/after-ttl"))
                .PUT(java.net.http.HttpRequest.BodyPublishers.ofString("value"))
                .build(),
            java.net.http.HttpResponse.BodyHandlers.ofString()
        );
        System.out.println("[Client] PUT after-ttl succeeded after cache refresh");

        // Scenario 3 — deregister a node, verify it's no longer hit
        System.out.println("\n--- Scenario 3: Deregister node, verify not hit ---");
        registryClient.deRegister("cache", "localhost", 9003);
        System.out.println("[Registry] Deregistered node 3");
        System.out.println("[Client] Waiting 6 seconds for load balancer cache to expire...");
        Thread.sleep(6000);
        System.out.println("[Client] Sending 6 requests — none should go to node 3");
        for (int i = 1; i <= 6; i++) {
            httpClient.send(
                java.net.http.HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:7000/cache/post-deregister-" + i))
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofString("value" + i))
                    .build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()
            );
        }
        System.out.println("[Node 1] Keys: " + getKeysFromNode("localhost:9001"));
        System.out.println("[Node 2] Keys: " + getKeysFromNode("localhost:9002"));
        System.out.println("[Node 3] Keys: " + getKeysFromNode("localhost:9003") + " (should have no new keys)");

        // Scenario 4 — unknown service returns 503
        System.out.println("\n--- Scenario 4: Unknown service returns 503 ---");
        java.net.http.HttpResponse<String> response = httpClient.send(
            java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://localhost:7000/unknown-service/foo"))
                .GET()
                .build(),
            java.net.http.HttpResponse.BodyHandlers.ofString()
        );
        System.out.println("[Client] Status: " + response.statusCode() + " (expected 503)");
        System.out.println("[Client] Body: " + response.body());

        // Scenario 5 — concurrent requests through load balancer
        System.out.println("\n--- Scenario 5: Concurrent requests ---");
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            final int idx = i;
            Thread t = new Thread(() -> {
                try {
                    httpClient.send(
                        java.net.http.HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:7000/cache/concurrent-" + idx))
                            .PUT(java.net.http.HttpRequest.BodyPublishers.ofString("value" + idx))
                            .build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                    );
                    System.out.println("[Thread " + idx + "] PUT concurrent-" + idx);
                } catch (Exception e) {
                    System.out.println("[Thread " + idx + "] Error: " + e.getMessage());
                }
            }, "Client Thread " + i);
            threads.add(t);
        }
        threads.forEach(Thread::start);
        for (Thread t : threads) t.join();
        System.out.println("[Node 1] Keys: " + getKeysFromNode("localhost:9001"));
        System.out.println("[Node 2] Keys: " + getKeysFromNode("localhost:9002"));
        System.out.println("[Node 3] Keys: " + getKeysFromNode("localhost:9003"));

        System.out.println("\n[Main] All scenarios done. Shutting down.");
        loadBalancer.stop();
        node1.stop();
        node2.stop();
        node3.stop();
        registryNode.stop();
    }

    private static String getKeysFromNode(String address) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        java.net.http.HttpResponse<String> response = client.send(
            java.net.http.HttpRequest.newBuilder()
                .uri(new URI("http://" + address + "/keys"))
                .GET()
                .build(),
            java.net.http.HttpResponse.BodyHandlers.ofString()
        );
        return response.body();
    }
}
