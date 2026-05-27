package threadpool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FixedThreadPool {
    private final Queue<Runnable> tasks;
    private boolean shutdown;
    private final Lock lock;
    private final Condition emptyQueue;

    public FixedThreadPool(int threads) {
        tasks = new LinkedList<>();
        shutdown = false;
        lock = new ReentrantLock();
        emptyQueue = lock.newCondition();

        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(this::queryTasks, "Worked Thread " + i);
            t.start();
        }
    }

    private void queryTasks() {
        while (true) {
            lock.lock();
            Runnable r = null;
            try {
                while (!shutdown && tasks.isEmpty()) {
                    emptyQueue.await();
                }

                if (shutdown && tasks.isEmpty())
                    break;
                r = tasks.poll();

            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }

            try {
                if (r != null)
                    r.run();
            } catch (Exception ignore) {
            }
        }
    }

    public MyFuture<?> submit(Runnable task) {
        lock.lock();
        MyFuture<?> future = new MyFuture<>();
        try {
            if (shutdown)
                throw new IllegalStateException("Pool is already Shutdown");
            tasks.add(() -> {
                try {
                    task.run();
                    future.setValue(null);
                } catch (Exception e) {
                    future.setException(e);
                }
            });

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
        } finally {
            emptyQueue.signal();
            lock.unlock();
        }

        return future;
    }

    public <T> MyFuture<T> submit(Callable<T> task) {
        lock.lock();
        MyFuture<T> future = new MyFuture<>();
        try {
            if (shutdown)
                throw new IllegalStateException("Pool is already Shutdown");
            tasks.add(() -> {
                try {
                    T value = task.call();
                    future.setValue(value);
                } catch (Exception e) {
                    future.setException(e);
                }
            });
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
        } finally {
            emptyQueue.signal();
            lock.unlock();
        }

        return future;
    }

    public void shutdown() {
        try {
            lock.lock();
            shutdown = true;
        } catch (Exception e) {
        } finally {
            emptyQueue.signalAll();
            lock.unlock();
        }
    }
}
