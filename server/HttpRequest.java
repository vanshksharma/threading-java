package server;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private final Map<String, String> headers;
    private String body;
    private final Map<String, String> pathParams;

    public HttpRequest(){
        headers = new HashMap<>();
        pathParams = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParam(String key, String value) {
        pathParams.put(key, value);
    }
}
