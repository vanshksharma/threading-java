package distributed;

import server.HttpResponse;
import server.HttpServer;

import java.io.IOException;

public class DistributedLockManagerNode {
    private final DistributedLockManager manager;
    private final HttpServer server;

    public DistributedLockManagerNode(int port, int poolSize) throws IOException {
        manager = new DistributedLockManager();
        server = new HttpServer(port, poolSize);
    }

    public void start(){
        server.post("/acquire/:lockName", (request) -> {
           try{
               String lockName = request.getPathParams().get("lockName");
               long ttl = Long.parseLong(request.getBody());
               String token = manager.acquire(lockName, ttl);
               return HttpResponse.ok(token);
           } catch (IllegalStateException e) {
               return HttpResponse.conflict("{\"message\": \"" + e.getMessage() + "\"}");
           } catch (Exception e) {
               return HttpResponse.serverError("{\"message\": \"" + e.getMessage() + "\"}");
           }
        });

        server.post("/release/:lockName", (request) -> {
            try{
                String lockName = request.getPathParams().get("lockName");
                String token = request.getBody();
                manager.release(lockName, token);
                return HttpResponse.ok("{\"message\": \"Lock released\"}");
            } catch (IllegalStateException | IllegalCallerException e){
                return HttpResponse.conflict("{\"message\": \"" + e.getMessage() + "\"}");
            } catch (Exception e){
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() + "\"}");
            }
        });

        Thread runner = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException("Failed to start Lock Manager Node");
            }
        }, "Manager Thread");

        runner.start();
    }

    public void stop() throws IOException {
        server.stop();
    }
}