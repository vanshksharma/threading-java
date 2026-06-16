package messaging;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class Producer {
    private final String brokerUrl;
    private final HttpClient client;

    public Producer(String brokerUrl){
        this.brokerUrl = brokerUrl;
        this.client = HttpClient.newHttpClient();
    }

    private HttpResponse<String> callNode(HttpRequest request) throws IOException, InterruptedException{
        return client.send(request, BodyHandlers.ofString());
    }

    private String buildUrl(String path) {
        return "http://" + brokerUrl + path;
    }

    public void createTopic(String name){
        try{
            String apiAddress = buildUrl("/topic/" + name);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiAddress)).POST(BodyPublishers.noBody()).build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() != 200){
                throw new RuntimeException("Error from the broker: " + response.body());
            }
        }
        catch (IOException | URISyntaxException | InterruptedException e){
            throw new RuntimeException("Unable to connect to broker");
        }
    }

    public void publish(String topic, String message){
        try{
            String apiAddress = buildUrl("/publish/" + topic);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiAddress)).POST(BodyPublishers.ofString(message)).build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() != 200){
                throw new RuntimeException("Error from the broker: " + response.body());
            }
        }
        catch (IOException | URISyntaxException | InterruptedException e){
            throw new RuntimeException("Unable to connect to broker");
        }
    }
}
