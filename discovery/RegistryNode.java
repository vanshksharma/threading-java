package discovery;

import java.io.IOException;
import java.util.List;
import server.HttpResponse;
import server.HttpServer;

public class RegistryNode {
    private final Registry registry;
    private final HttpServer server;
    
    public RegistryNode(int port, int poolSize, long ttl) throws IOException {
        this.registry = new Registry(ttl);
        this.server = new HttpServer(port, poolSize);
    }

    public void start(){
        server.post("/register/:name", (request) -> {
            try {
                String serviceName = request.getPathParams().get("name");
                String[] info = request.getBody().split("\\|");
                registry.register(serviceName, info[0], Integer.parseInt(info[1]));
                return HttpResponse.ok("{\"message\": \"Service Registered\"}");
            } catch (IllegalStateException e){
                return HttpResponse.conflict("{\"message\": \"" + e.getMessage() +"\"}");
            } catch (NumberFormatException e) {
                return HttpResponse.badRequest("{\"message\": \"" + e.getMessage() +"\"}");
            } catch (Exception e){
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() +"\"}");
            }
        });

        server.post("/heartbeat/:name", (request) -> {
            try {
                String serviceName = request.getPathParams().get("name");
                String[] info = request.getBody().split("\\|");
                registry.heartbeat(serviceName, info[0], Integer.parseInt(info[1]));
                return HttpResponse.ok("{\"message\": \"Heartbeat received\"}");
            } catch (IllegalStateException e){
                return HttpResponse.conflict("{\"message\": \"" + e.getMessage() +"\"}");
            } catch (NumberFormatException e) {
                return HttpResponse.badRequest("{\"message\": \"" + e.getMessage() +"\"}");
            } catch (Exception e){
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() +"\"}");
            }
        });

        server.delete("/deregister/:name", (request) -> {
            try {
                String serviceName = request.getPathParams().get("name");
                String[] info = request.getBody().split("\\|");
                registry.deRegister(serviceName, info[0], Integer.parseInt(info[1]));
                return HttpResponse.ok("{\"message\": \"Service Deregistered\"}");
            } catch (IllegalStateException e){
                return HttpResponse.conflict("{\"message\": \"" + e.getMessage() +"\"}");
            } catch (NumberFormatException e) {
                return HttpResponse.badRequest("{\"message\": \"" + e.getMessage() +"\"}");
            } catch (Exception e){
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() +"\"}");
            }
        });

        server.get("/instances/:name", (request) -> {
            try {
                String serviceName = request.getPathParams().get("name");
                List<ServiceInstance> services = registry.getInstances(serviceName);
                StringBuilder builder = new StringBuilder("[");
                for (int i = 0; i < services.size(); i++) {
                    ServiceInstance s = services.get(i);
                    builder.append("{\"host\":\"").append(s.getHost())
                        .append("\",\"port\":").append(s.getPort()).append("}");
                    if (i < services.size() - 1) builder.append(",");
                }
                builder.append("]");
                return HttpResponse.ok(builder.toString());
            } catch (IllegalStateException e){
                return HttpResponse.conflict("{\"message\": \"" + e.getMessage() +"\"}");
            } catch (NumberFormatException e) {
                return HttpResponse.badRequest("{\"message\": \"" + e.getMessage() +"\"}");
            } catch (Exception e){
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() +"\"}");
            }
        });

        Thread runner = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException("Unable to Start Registry Server");
            }
        }, "Server Thread");

        runner.start();
    }

    public void stop() throws IOException{
        server.stop();
    }
}
