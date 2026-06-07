package server;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private Integer statusCode;
    private final Map<String, String> headers;
    private String body;

    public HttpResponse() {
        headers = new HashMap<>();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeader(String key, String value){
        headers.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static HttpResponse ok(String body){
        HttpResponse response = new HttpResponse();
        response.setStatusCode(200);
        response.setBody(body);
        return response;
    }

    public static HttpResponse notFound(String body){
        HttpResponse response = new HttpResponse();
        response.setStatusCode(404);
        response.setBody(body);
        return response;
    }

    public static HttpResponse badRequest(String body){
        HttpResponse response = new HttpResponse();
        response.setStatusCode(400);
        response.setBody(body);
        return response;
    }

    public static HttpResponse serverError(String body){
        HttpResponse response = new HttpResponse();
        response.setStatusCode(500);
        response.setBody(body);
        return response;
    }
}
