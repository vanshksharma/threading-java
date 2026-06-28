package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

public class ChatClient {
    private final Socket socket;
    private final Scanner sc;
    private final PrintWriter writer;
    private final BufferedReader reader;

    public ChatClient(String host, int port, String username) throws UnknownHostException, IOException {
        this.socket = new Socket(host, port);
        this.sc = new Scanner(System.in);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer.println(username);
    }

    private void startWriting(){
        while(sc.hasNextLine()){
            writer.println(sc.nextLine());
        }
    }

    private void startWritingForTest(List<String> messages) throws InterruptedException{
        for (String message : messages) {
            writer.println(message);
            Thread.sleep(500);
        }
    }

    private void startReading() throws IOException{
        String line;
        while((line = reader.readLine()) != null){
            System.out.println(line);
        }
    }

    public void start(List<String> messages){
        Thread.ofVirtual().name("Reader Thread").start(() -> {
            try {
                startReading();
            } catch (IOException e) {
                System.out.println("Failed to read from Socket");
            } finally {
                try { stop(); } catch (IOException ignored) {}
            }
        });

        Thread.ofVirtual().name("Writer Thread").start(() -> {
            try {
                startWritingForTest(messages);
            } catch (InterruptedException e) {
                System.out.println("Failed to read from Socket");
            } finally{
                try { stop(); } catch (IOException ignored) {}
            }
        });
    }

    public void stop() throws IOException{
        socket.close();
    }
}
