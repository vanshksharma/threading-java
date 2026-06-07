package server;

@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request);
}
