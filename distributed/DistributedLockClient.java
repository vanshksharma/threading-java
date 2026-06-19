package distributed;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DistributedLockClient {
    private final String nodeUrl;
    private final HttpClient client;

    public DistributedLockClient(String nodeUrl){
        this.nodeUrl = nodeUrl;
        this.client = HttpClient.newHttpClient();
    }

    private String buildUrl(String suffix){
        return "http://" + nodeUrl + suffix;
    }

    private HttpResponse<String> callNode(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String acquire(String lockName, long ttl){
        try{
            String apiUrl = buildUrl("/acquire/" + lockName);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).POST(HttpRequest.BodyPublishers.ofString(Long.toString(ttl))).build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() == 409){
                throw new IllegalStateException("State Conflict " + response.body());
            }
            else if(response.statusCode() != 200){
                throw new RuntimeException("Error from the cluster " + response.body());
            }

            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException("Unable to connect to the cluster", e);
        }
    }

    public void release(String lockName, String token){
        try{
            String apiUrl = buildUrl("/release/" + lockName);
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).POST(HttpRequest.BodyPublishers.ofString(token)).build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() == 409){
                throw new IllegalStateException("State Conflict " + response.body());
            }
            else if(response.statusCode() != 200){
                throw new RuntimeException("Error from the cluster " + response.body());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException("Unable to connect to the cluster", e);
        }
    }
}
