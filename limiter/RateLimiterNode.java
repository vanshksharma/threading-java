package limiter;

import java.io.IOException;

import limiter.strategy.RateLimitStrategy;
import server.HttpResponse;
import server.HttpServer;

public class RateLimiterNode {
    private final RateLimitStrategy strategy;
    private final HttpServer server;

    public RateLimiterNode(int port, int poolSize, RateLimitStrategy strategy) throws IOException {
        this.strategy = strategy;
        this.server = new HttpServer(port, poolSize);
    }

    public void start(){
        server.post("/check/:ip", (request) -> {
            try {
                String ip = request.getPathParams().get("ip");
                boolean isAllowed = strategy.isAllowed(ip);
                if(isAllowed){
                    return HttpResponse.ok("{\"message\": \"Request Allowed\"}");
                }
                return HttpResponse.tooManyRequests("{\"message\": \"Too Many Requests\"}");
            } catch (Exception e) {
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() + "\"}");
            }
        });

        Thread runner = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException("Failed to start Limiter Node");
            }
        }, "Limiter Thread");

        runner.start();
    }

    public void stop() throws IOException{
        server.stop();
    }
}
