package cache;

import java.io.IOException;
import server.HttpResponse;
import server.HttpServer;

public class CacheNode {
    private final LRUCache<String,String> cache;
    private final HttpServer server;
    private final int nodeNumber;

    public CacheNode(int port, int cacheCapacity, int poolSize, int nodeNumber) throws IOException{
        cache = new LRUCache<>(cacheCapacity);
        server = new HttpServer(port, poolSize);
        this.nodeNumber = nodeNumber;
    }

    public void start() throws IOException{
        server.get("/cache/:key", (request) -> {
            HttpResponse response = new HttpResponse();
            String key = request.getPathParams().get("key");
            String value = cache.get(key);
            if(value == null){
                response.setStatusCode(404);
                response.setBody("{\"message\": " + "\"" + key + " not found\"}");
            }
            else{
                response.setStatusCode(200);
                response.setBody("{\"value\": \"" + value + "\"}");
            }

            return response;
        });

        server.get("/keys", (request) -> {
            return HttpResponse.ok("{\"message\": " + cache.getKeys().toString() + "}");
        });

        server.put("/cache/:key", (request) -> {
            String key = request.getPathParams().get("key");
            cache.put(key, request.getBody());
            return HttpResponse.ok("{\"message\": \"Key inserted\"}");
        });

        server.delete("/cache/:key", (request) -> {
            String key = request.getPathParams().get("key");
            cache.removeKey(key);
            return HttpResponse.ok("{\"message\": \"Key Removed\"}");
        });

        Thread runner = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException("Server thread " + nodeNumber + " failed", e);
            }
        }, "Server Thread " + nodeNumber);

        runner.start();
    }

    public void stop() throws IOException{
        server.stop();
    }
}   
