package messaging;

import java.io.IOException;

public class MessageTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("[Broker] Starting broker node on port 8080");
        BrokerNode brokerNode = new BrokerNode(8080, 10);
        brokerNode.start();

        Thread.sleep(500);

        String brokerUrl = "localhost:8080";
        Producer producer = new Producer(brokerUrl);
        Consumer consumer = new Consumer(brokerUrl);

        System.out.println("[Producer] Creating topic 'events'");
        producer.createTopic("events");

        Thread consumerThread = new Thread(() -> {
            System.out.println("[Consumer] Starting to consume from 'events'");
            while (true) {
                try {
                    String message = consumer.consume("events", 2000);
                    if (message != null) {
                        System.out.println("[Consumer] Received: " + message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Consumer Thread");

        consumerThread.setDaemon(true);
        consumerThread.start();

        Thread producerThread = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000);
                    String message = "Message " + i;
                    System.out.println("[Producer] Publishing: " + message);
                    producer.publish("events", message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("[Producer] Done publishing");
        }, "Producer Thread");

        producerThread.start();
        producerThread.join();

        Thread.sleep(3000); // give consumer time to drain remaining messages

        System.out.println("[Broker] Shutting down");
        brokerNode.stop();
    }
}
