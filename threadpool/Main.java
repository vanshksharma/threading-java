package threadpool;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static int MIN = 3000;
    static int MAX = 6000;
    static int numTasks = 10;
    static List<MyFuture<Integer>> callableFutures = new ArrayList<>();
    static List<MyFuture<?>> runnablFutures = new ArrayList<>();

    static void submitCallables(FixedThreadPool executor) {
        System.out.println("Submitting Callables");
        for (int i = 0; i < numTasks; i++) {
            final int finalI = i;
            callableFutures.add(executor.submit(() -> {
                System.out.println("Callable " + finalI + " Started");
                try {
                    int sleep = (int) (Math.random() * (MAX - MIN) + MIN);
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                }
                System.out.println("Callable " + finalI + " Ended");
                return (int) (Math.random() * (MAX - MIN) + MIN);
            }));
        }

        System.out.println("Callables Submitted");

        System.out.println("Calling All futures for callables, this should suspend the main thread");
        List<Integer> results = callableFutures.stream().map(MyFuture::get).toList();
        System.out.println("Main got released from Futures. Th result is " + results);
        executor.shutdown();
    }

    static void submitRunnables(FixedThreadPool executor) {
        System.out.println("Submitting Runnables");
        for (int i = 0; i < numTasks; i++) {
            final int finalI = i;
            runnablFutures.add(executor.submit(() -> {
                System.out.println("Runnable " + finalI + " Started");
                try {
                    int sleep = (int) (Math.random() * (MAX - MIN) + MIN);
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                }
                System.out.println("Runnable " + finalI + " Ended");
            }));
        }

        System.out.println("Runnables Submitted");

        System.out.println("Calling All futures for runnables, this should suspend the main thread");
        List<?> results = runnablFutures.stream().map(MyFuture::get).toList();
        System.out.println("Main got released from Futures. Th result is " + results);
        executor.shutdown();
    }

    public static void main(String[] args) {
        System.out.println("Solver Started");
        FixedThreadPool executor = new FixedThreadPool(5);
        submitCallables(executor);
        // submitRunnables(executor);
        System.out.println("Solver Ended");
    }
}
