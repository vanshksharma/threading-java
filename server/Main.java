package server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = new HttpServer(8000, 10);
            server.get("/hello", (request) -> {
                HttpResponse response = new HttpResponse();
                response.setStatusCode(200);
                response.setBody("Hello, World!");
                return response;
            });

            server.get("/user/:id", (request) -> {
                HttpResponse response = new HttpResponse();
                response.setStatusCode(200);
                response.setBody("User id is: " + request.getPathParams().get("id"));
                return response;
            });

            server.get("/user/:id/department/:department", (request) -> {
                HttpResponse response = new HttpResponse();
                response.setStatusCode(200);
                response.setBody("User id is: " + request.getPathParams().get("id") + ", Department is: " + request.getPathParams().get("department"));
                return response;
            });

            server.get("/throwError", (request) -> {
                throw new Exception("Exception is thrown");
            });

            server.start();
        } catch (IOException e) {
        }
    }
}
