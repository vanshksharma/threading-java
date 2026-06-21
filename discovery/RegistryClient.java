package discovery;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RegistryClient {
    private final HttpClient client;
    private final String nodeAddress;

    public RegistryClient(String nodeAddress) {
        this.nodeAddress = nodeAddress;
        this.client = HttpClient.newHttpClient();
    }

    private String buildUrl(String prefix){
        return "http://" + nodeAddress + prefix;
    }

    private HttpResponse<String> callNode(HttpRequest request) throws IOException, InterruptedException{
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void register(String name, String host, int port){
        try {
            String apiUrl = buildUrl("/register/" + name);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).POST(BodyPublishers.ofString(host + "|" + port)).build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() == 409){
                throw new IllegalStateException(response.body());
            }
            if(response.statusCode() != 200){
                throw new RuntimeException(response.body());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void heartbeat(String name, String host, int port){
        try {
            String apiUrl = buildUrl("/heartbeat/" + name);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).POST(BodyPublishers.ofString(host + "|" + port)).build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() == 409){
                throw new IllegalStateException(response.body());
            }
            if(response.statusCode() != 200){
                throw new RuntimeException(response.body());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deRegister(String name, String host, int port){
        try {
            String apiUrl = buildUrl("/deregister/" + name);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).method("DELETE",BodyPublishers.ofString(host + "|" + port)).build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() == 409){
                throw new IllegalStateException(response.body());
            }
            if(response.statusCode() != 200){
                throw new RuntimeException(response.body());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public List<String> getInstances(String name){
        try {
            String apiUrl = buildUrl("/instances/" + name);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).GET().build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() == 409){
                throw new IllegalStateException(response.body());
            }
            if(response.statusCode() != 200){
                throw new RuntimeException(response.body());
            }

            List<String> instances = new ArrayList<>();
            String body = response.body().replaceAll("[\\[\\]{}]", "");
            if (body.isBlank()) return instances;
            for (String entry : body.split(",(?=\"host\")")) {
                String host = entry.replaceAll(".*\"host\":\"([^\"]+)\".*", "$1");
                String port = entry.replaceAll(".*\"port\":([0-9]+).*", "$1");
                instances.add(host + ":" + port);
            }
            return instances;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    } 
}
