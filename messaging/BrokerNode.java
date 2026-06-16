package messaging;

import java.io.IOException;
import java.net.UnknownHostException;
import server.HttpResponse;
import server.HttpServer;

public class BrokerNode {
    private final Broker broker;
    private final HttpServer server;

    public BrokerNode(int port, int poolSize) throws IOException {
        this.broker = new Broker();
        this.server = new HttpServer(port, poolSize);
    }

    public void start(){
        server.post("/topic/:name", (request) -> {
            try {
                broker.createTopic(request.getPathParams().get("name"));
                return HttpResponse.ok("{\"message\": \"Topic Created\"}");
            } catch (IllegalStateException e) {
                return HttpResponse.badRequest("{\"message\": \"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() + "\"}");
            }
        });

        server.post("/publish/:topic", (request) -> {
            try {
                String topic = request.getPathParams().get("topic");
                Message message = new Message(request.getBody(), topic);
                broker.publish(topic, message);
                return HttpResponse.ok("{\"message\": \"Message published successfully\"}");
            } catch (IllegalStateException e) {
                return HttpResponse.badRequest("{\"message\": \"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() + "\"}");
            }
        });

        server.get("/consume/:topic", (request) -> {
            try {
                String topic = request.getPathParams().get("topic");
                Message message = broker.consume(topic);
                if(message == null){
                    return HttpResponse.noContent();
                }
                return HttpResponse.ok("{\"message\": \"" + message.getContent() + "\"}");
            } catch (IllegalStateException e) {
                return HttpResponse.badRequest("{\"message\": \"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                return HttpResponse.serverError("{\"message\": \"" + e.getMessage() + "\"}");
            }
        });

        Thread runner = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException("Failed to start broker node");
            }
        }, "Broker Thread");

        runner.start();
    }

    public void stop() throws IOException{
        server.stop();
    }

    public String getAddress() throws UnknownHostException{
        return server.getAddress();
    }
}
