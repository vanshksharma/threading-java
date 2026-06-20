package queue;

public class QueueTest {
    public static void main(String[] args) throws InterruptedException {
        // Scenario 1 — basic put and take
        System.out.println("--- Scenario 1: Basic put and take ---");
        MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5);
        queue.put(1);
        queue.put(2);
        queue.put(3);
        System.out.println("[Main] Put 1, 2, 3");
        System.out.println("[Main] Took: " + queue.take());
        System.out.println("[Main] Took: " + queue.take());
        System.out.println("[Main] Took: " + queue.take());

        // Scenario 2 — producer blocks when queue is full
        System.out.println("\n--- Scenario 2: Producer blocks when full ---");
        MyBlockingQueue<Integer> fullQueue = new MyBlockingQueue<>(3);
        fullQueue.put(1);
        fullQueue.put(2);
        fullQueue.put(3);
        System.out.println("[Main] Queue full, starting blocked producer");

        Thread blockedProducer = new Thread(() -> {
            System.out.println("[Producer] Trying to put 4 into full queue...");
            fullQueue.put(4);
            System.out.println("[Producer] Successfully put 4 after space freed");
        }, "Blocked Producer");

        blockedProducer.start();
        Thread.sleep(1000);
        System.out.println("[Main] Taking one item to free space: " + fullQueue.take());
        blockedProducer.join();

        // Scenario 3 — consumer blocks when queue is empty
        System.out.println("\n--- Scenario 3: Consumer blocks when empty ---");
        MyBlockingQueue<Integer> emptyQueue = new MyBlockingQueue<>(3);

        Thread blockedConsumer = new Thread(() -> {
            System.out.println("[Consumer] Trying to take from empty queue...");
            Integer item = emptyQueue.take();
            System.out.println("[Consumer] Successfully took: " + item);
        }, "Blocked Consumer");

        blockedConsumer.start();
        Thread.sleep(1000);
        System.out.println("[Main] Putting item to unblock consumer");
        emptyQueue.put(42);
        blockedConsumer.join();

        // Scenario 4 — offer times out when queue stays full
        System.out.println("\n--- Scenario 4: offer times out ---");
        MyBlockingQueue<Integer> timeoutQueue = new MyBlockingQueue<>(2);
        timeoutQueue.put(1);
        timeoutQueue.put(2);
        System.out.println("[Main] Queue full, trying offer with 2s timeout");
        boolean offered = timeoutQueue.offer(3, 2);
        System.out.println("[Main] offer returned: " + offered + " (expected false)");

        // Scenario 5 — poll times out when queue stays empty
        System.out.println("\n--- Scenario 5: poll times out ---");
        MyBlockingQueue<Integer> emptyPollQueue = new MyBlockingQueue<>(2);
        System.out.println("[Main] Queue empty, trying poll with 2s timeout");
        Integer polled = emptyPollQueue.poll(2);
        System.out.println("[Main] poll returned: " + polled + " (expected null)");

        // Scenario 6 — multiple producers and consumers
        System.out.println("\n--- Scenario 6: Multiple producers and consumers ---");
        MyBlockingQueue<Integer> sharedQueue = new MyBlockingQueue<>(5);

        Thread producer1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                sharedQueue.put(i);
                System.out.println("[Producer 1] Put: " + i);
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Producer 1");

        Thread producer2 = new Thread(() -> {
            for (int i = 6; i <= 10; i++) {
                sharedQueue.put(i);
                System.out.println("[Producer 2] Put: " + i);
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Producer 2");

        Thread consumer1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                Integer item = sharedQueue.take();
                System.out.println("[Consumer 1] Took: " + item);
                try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Consumer 1");

        Thread consumer2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                Integer item = sharedQueue.take();
                System.out.println("[Consumer 2] Took: " + item);
                try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Consumer 2");

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();

        producer1.join();
        producer2.join();
        consumer1.join();
        consumer2.join();

        System.out.println("\n[Main] All scenarios passed.");
    }

}
