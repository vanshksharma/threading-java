package balancer;

import discovery.RegistryClient;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import server.HttpRequest;
import server.HttpResponse;
import server.HttpServer;

public class LoadBalancer {
    private final Map<String, Service> cache;
    private final HttpServer server;
    private final RegistryClient registryClient;
    private final HttpClient httpClient;
    private final long ttl;

    private class Service {
        private volatile List<String> instances;
        private final RoundRobinRouter router;
        private volatile long lastUpdateTime;

        public Service() {
            instances = null;
            router = new RoundRobinRouter();
            lastUpdateTime = 0L;
        }

        public void setLastUpdateTime(Long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        public void setInstances(List<String> instances) {
            this.instances = instances;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastUpdateTime >= ttl;
        }

        public String getInstance() {
            return router.getNextService(instances);
        }
    }

    public LoadBalancer(String discoveryAddress, int port, int poolSize, long ttl) throws IOException {
        this.cache = new ConcurrentHashMap<>();
        this.server = new HttpServer(port, poolSize);
        this.registryClient = new RegistryClient(discoveryAddress);
        this.httpClient = HttpClient.newHttpClient();
        this.ttl = ttl;
    }

    private void refreshCache(String serviceName) {
        if(cache.containsKey(serviceName) && !cache.get(serviceName).isExpired()) return;

        cache.compute(serviceName, (key, existing) -> {
            if (existing == null) {
                List<String> healthyInstances = registryClient.getInstances(serviceName);
                Service service = new Service();
                service.setInstances(healthyInstances);
                return service;
            } else if (existing.isExpired()) {
                List<String> healthyInstances = registryClient.getInstances(serviceName);
                existing.setInstances(healthyInstances);
                existing.setLastUpdateTime(System.currentTimeMillis());
            }

            return existing;
        });
    }

    private String buildUrl(String suffix) {
        return "http://" + suffix;
    }

    private java.net.http.HttpResponse<String> callNode(java.net.http.HttpRequest request) throws IOException, InterruptedException{
        return httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse proxy(HttpRequest request) throws URISyntaxException, IOException, InterruptedException {
        String[] parts = request.getPath().split("/");
        String serviceName = parts[1];
        refreshCache(serviceName);
        String serviceAddress = cache.get(serviceName).getInstance();
        String apiAddress = buildUrl(serviceAddress + request.getPath());

        String body = request.getBody() == null ? "" : request.getBody();
        java.net.http.HttpRequest proxyRequest = java.net.http.HttpRequest.newBuilder().uri(new URI(apiAddress))
                .method(request.getMethod(), java.net.http.HttpRequest.BodyPublishers.ofString(body))
                .build();
        
        java.net.http.HttpResponse<String> proxyResponse = callNode(proxyRequest);
        HttpResponse response = new HttpResponse();
        response.setBody(proxyResponse.body());
        response.setStatusCode(proxyResponse.statusCode());
        Map<String, List<String>> headers = proxyResponse.headers().map();
        for(Map.Entry<String, List<String>> entry: headers.entrySet()){
            String key = entry.getKey();
            for(String value: entry.getValue()){
                response.setHeader(key, value);
            }
        }

        return response;
    }

    public void start() {
        server.any((request) -> {
            try {
                return proxy(request);
            } catch (IOException | InterruptedException | URISyntaxException e) {
                return HttpResponse.serverError("{\"message\": \"Server Error: " + e.getMessage() + "\"}");
            } catch (IllegalStateException e){
                return HttpResponse.unavailable("{\"message\": \"No instances Available: " + e.getMessage() + "\"}");
            } catch (Exception e){
                return HttpResponse.serverError("{\"message\": \"Proxy error: " + e.getMessage() + "\"}");
            }
        });

        Thread runner = new Thread(() -> {
            try{
                server.start();
            }
            catch(IOException e){
                throw new RuntimeException("Unable to start balancer", e);
            }
        }, "Server Thread");

        runner.start();
    }

    public void stop() throws IOException{
        server.stop();
    }
}
