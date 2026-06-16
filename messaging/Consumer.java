package messaging;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class Consumer {
    private final String brokerUrl;
    private final HttpClient client;

    public Consumer(String brokerUrl){
        this.brokerUrl = brokerUrl;
        this.client = HttpClient.newHttpClient();
    }

    private HttpResponse<String> callNode(HttpRequest request) throws IOException, InterruptedException{
        return client.send(request, BodyHandlers.ofString());
    }

    private String consumeTopic(String topic){
        try{
            String apiAddress = "http://" + brokerUrl + "/consume/" + topic;
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiAddress)).GET().build();
            HttpResponse<String> response = callNode(request);
            if (response.statusCode() == 204) return null;
            if(response.statusCode() != 200){
                throw new RuntimeException("Error from the broker: " + response.body());
            }
            
            return response.body();
        }
        catch (IOException | URISyntaxException | InterruptedException e){
            throw new RuntimeException("Unable to connect to broker");
        }
    }

    public String consume(String topic, long timeout) throws InterruptedException{
        long deadline = System.currentTimeMillis() + timeout;
        while(System.currentTimeMillis() < deadline){
            String message = consumeTopic(topic);
            if(message != null) return message;
            Thread.sleep(100);
        }

        return null;
    }

    public String consume(String topic) {
        return consumeTopic(topic);
    }
}
