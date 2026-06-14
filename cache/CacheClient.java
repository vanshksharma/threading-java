package cache;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class CacheClient {
    private final CacheCluster cluster;
    private final HttpClient client;

    public CacheClient(CacheCluster cluster) {
        this.cluster = cluster;
        this.client = HttpClient.newHttpClient();
    }

    private HttpResponse<String> callNode(HttpRequest request) throws IOException, InterruptedException{
        return client.send(request, BodyHandlers.ofString());
    }

    private String buildUrl(String key) {
        return "http://" + cluster.getAddress(key) + "/cache/" + key;
    }

    public String get(String key){
        String apiAddress = buildUrl(key);
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(apiAddress)).GET().build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() != 200){
                throw new RuntimeException("Error from the cluster: " +  response.body());
            }
            return response.body();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException("Unable to connect to cluster", e);
        }
    }

    public void put(String key, String value){
        String apiAddress = buildUrl(key);
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(apiAddress)).PUT(BodyPublishers.ofString(value)).build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() != 200){
                throw new RuntimeException("Error from the cluster: " +  response.body());
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException("Unable to connect to cluster", e);
        }
    }

    public void delete(String key){
        String apiAddress = buildUrl(key);
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(apiAddress)).DELETE().build();
            HttpResponse<String> response = callNode(request);
            if(response.statusCode() != 200){
                throw new RuntimeException("Error from the cluster: " +  response.body());
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException("Unable to connect to cluster", e);
        }
    }
}
