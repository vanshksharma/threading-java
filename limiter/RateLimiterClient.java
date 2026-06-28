package limiter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RateLimiterClient {
    private final String nodeAddress;
    private final HttpClient client;

    public RateLimiterClient(String nodeAddress) {
        this.nodeAddress = nodeAddress;
        this.client = HttpClient.newHttpClient();
    }

    private String buildUrl(String suffix){
        return "http://"  + nodeAddress + "/check/" + suffix; 
    }

    private HttpResponse<String> callNode(HttpRequest request) throws IOException, InterruptedException{
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public boolean isAllowed(String ip){
        try {
            String apiUrl = buildUrl(ip);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> response = callNode(request);
            switch (response.statusCode()) {
                case 200 -> {
                    return true;
                }
                case 429 -> {
                    return false;
                }
                default -> throw new RuntimeException("Error from the cluster" + response.body());
            }
        } catch (IOException | InterruptedException | RuntimeException | URISyntaxException e) {
            throw new RuntimeException("Unable to connect to the cluster", e);
        }
    }
}
