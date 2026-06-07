package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import threadpool.FixedThreadPool;

public class HttpServer {
    private final Map<String, HttpHandler> routes;
    private final static Map<Integer, String> codes = Map.of(200, "OK", 404, "Not Found", 500, "Internal Sever Error",
        400, "Bad Request"
    );
    private ServerSocket serverSocket;
    private int poolSize;
    private FixedThreadPool executor;

    public HttpServer() throws IOException{
        this(8000, 10);
    }

    public HttpServer(int port, int poolSize) throws IOException {
        this.routes = new HashMap<>();
        this.poolSize = poolSize;
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException{
        executor = new FixedThreadPool(poolSize);
        while(!serverSocket.isClosed()){
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(createRunnableTask(clientSocket));
            } catch (IOException e) {
                break;
            }
        }
    }

    public void stop() throws IOException{
        executor.shutdown();
        serverSocket.close();
    }

    public void get(String route, HttpHandler handler){
        routes.put("GET" + "@@" + route, handler);
    }

    public void post(String route, HttpHandler handler){
        routes.put("POST" + "@@" + route, handler);
    }

    public void put(String route, HttpHandler handler){
        routes.put("PUT" + "@@" + route, handler);
    }

    public void delete(String route, HttpHandler handler){
        routes.put("DELETE" + "@@" + route, handler);
    }

    public HttpHandler matchRoute(HttpRequest request) throws NoRouteFoundException{
        for (Map.Entry<String, HttpHandler> entry : routes.entrySet()) {
            Map<String, String> tempParams = new HashMap<>();
            boolean isMatching = true;

            String[] brokenRoute = entry.getKey().split("@@");
            if(!brokenRoute[0].equals(request.getMethod())) continue;

            String[] registeredPattern = brokenRoute[1].split("/");
            String[] incomingPattern = request.getPath().split("/");

            if(registeredPattern.length != incomingPattern.length) continue;

            for(int i=0; i<registeredPattern.length; i++){
                if(!registeredPattern[i].startsWith(":")){
                    if(!registeredPattern[i].equals(incomingPattern[i])){
                        isMatching = false;
                        break;
                    }
                }
                else{
                    tempParams.put(registeredPattern[i].substring(1), incomingPattern[i]);
                }
            }

            if(isMatching){
                tempParams.forEach(request::setPathParam);
                return entry.getValue();
            }
        }

        throw new NoRouteFoundException("No Matching Routes Found");
    }

    private HttpRequest parseRequest(InputStream stream) throws IOException{
        HttpRequest request = new HttpRequest();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        try{
            String line;
            int contentLength = 0;
            String firstLine = reader.readLine();
            String[] brokenLine = firstLine.split(" ");
            request.setMethod(brokenLine[0]);
            request.setPath(brokenLine[1]);
            
            while(!(line = reader.readLine()).trim().isEmpty()){
                String[] pair = line.split(":", 2);
                request.setHeader(pair[0].trim(), pair[1].trim());
                if(pair[0].trim().equals("Content-Length")){
                    contentLength = Integer.parseInt(pair[1].trim());
                }
            }

            if(contentLength > 0){
                char[] buffer = new char[contentLength];
                reader.read(buffer);
                request.setBody(String.valueOf(buffer));
            }

        } catch (IOException | NumberFormatException e) {
            throw new IOException("Failed to parse request");
        }

        return request;
    }

    public String serializeResponse(HttpResponse response){
        StringBuilder builder = new StringBuilder("");
        builder.append("HTTP/1.1 ").append(response.getStatusCode()).append(" ").append(codes.get(response.getStatusCode())).append("\r\n");
        for(Map.Entry<String, String> entry: response.getHeaders().entrySet()){
            builder.append(entry.getKey().trim()).append(": ").append(entry.getValue().trim()).append("\r\n");
        }

        if(!response.getHeaders().containsKey("Content-Length")){
            builder.append("Content-Length: ");
            if(response.getBody() != null){
                builder.append(response.getBody().length());
            }
            else{
                builder.append(0);
            }
            builder.append("\r\n");
        }

        builder.append("\r\n");

        if(response.getBody() != null){
            builder.append(response.getBody());
        }

        return builder.toString();
    }

    private Runnable createRunnableTask(Socket socket){
        return () -> {
            PrintWriter out;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Failed to create socket connection");
                try {
                    socket.close();
                } catch (IOException exc) {
                    System.out.println("Failed to close connection");
                }
                return;
            }

            try {
                HttpRequest httpRequest = parseRequest(socket.getInputStream());
                HttpHandler handler = matchRoute(httpRequest);
                HttpResponse httpResponse = handler.handle(httpRequest);
                String response = serializeResponse(httpResponse);
                out.append(response);
                out.flush();
            }
            catch (NoRouteFoundException e){
                HttpResponse httpResponse = HttpResponse.notFound(e.getMessage());
                String response = serializeResponse(httpResponse);
                out.append(response);
                out.flush();
            } 
            catch (IOException e) {
                HttpResponse httpResponse = HttpResponse.badRequest(e.getMessage());
                String response = serializeResponse(httpResponse);
                out.append(response);
                out.flush();
            }
            catch (Exception e){
                HttpResponse httpResponse = HttpResponse.serverError(e.getMessage());
                String response = serializeResponse(httpResponse);
                out.append(response);
                out.flush();
            }
            finally{
                try{
                    socket.close();
                    out.close();
                }
                catch(IOException e){
                    System.out.println("Failed to close connection");
                }
                
            }
        };
    }
}
