package cache;

import java.io.IOException;

public class ClusterTest {
    public static void main(String[] args) throws IOException {
        System.out.println("[Cluster] Initializing cluster with 10 virtual nodes");
        CacheCluster cluster = new CacheCluster(10);

        System.out.println("[Cluster] Creating nodes...");
        CacheNode node1 = new CacheNode(8080, 100, 10, 1);
        CacheNode node2 = new CacheNode(8081, 100, 10, 2);
        CacheNode node3 = new CacheNode(8082, 100, 10, 3);

        System.out.println("[Node 1] Starting on port 8080");
        node1.start();
        System.out.println("[Node 2] Starting on port 8081");
        node2.start();
        System.out.println("[Node 3] Starting on port 8082");
        node3.start();

        System.out.println("[Cluster] Registering nodes...");
        cluster.addNode(1, node1.getAddress());
        System.out.println("[Cluster] Registered Node 1 at " + node1.getAddress());
        cluster.addNode(2, node2.getAddress());
        System.out.println("[Cluster] Registered Node 2 at " + node2.getAddress());
        cluster.addNode(3, node3.getAddress());
        System.out.println("[Cluster] Registered Node 3 at " + node3.getAddress());

        CacheClient client = new CacheClient(cluster);
        System.out.println("[Client] CacheClient ready");

        System.out.println("[Client] PUT foo=bar");
        client.put("foo", "bar");

        System.out.println("[Client] PUT name=abc");
        client.put("name", "abc");

        String value;

        System.out.println("[Client] GET foo");
        value = client.get("foo");
        System.out.println("[Client] Got: " + value);

        System.out.println("[Client] GET name");
        value = client.get("name");
        System.out.println("[Client] Got: " + value);

        System.out.println("[Client] DELETE foo");
        client.delete("foo");

        System.out.println("[Client] GET foo (expecting 404)");
        try {
            client.get("foo");
        } catch (RuntimeException e) {
            System.out.println("[Client] Expected error: " + e.getMessage());
        }

        System.out.println("[Client] GET name (expecting a value since it is not deleted)");
        value = client.get("name");
        System.out.println("[Client] Got: " + value);

        System.out.println("[Node 1] Stopping");
        node1.stop();
        System.out.println("[Node 2] Stopping");
        node2.stop();
        System.out.println("[Node 3] Stopping");
        node3.stop();
    }

}
