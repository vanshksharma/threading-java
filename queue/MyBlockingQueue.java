package queue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<T> {
    private final T[] queue;
    private final int capacity;
    private final Lock lock;
    private final Condition emptyQueue;
    private final Condition fullQueue;
    private int head, tail, size;

    @SuppressWarnings("unchecked")
    public MyBlockingQueue(int capacity){
        this.queue = (T[]) new Object[capacity];
        this.capacity = capacity;
        lock = new ReentrantLock();
        emptyQueue = lock.newCondition();
        fullQueue = lock.newCondition();
        head = tail = size = 0;
    }

    public int size(){
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty(){
        lock.lock();
        try {
            return size == 0;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull(){
        lock.lock();
        try {
            return size == capacity;
        } finally {
            lock.unlock();
        }
    }

    public void put(T item){
        lock.lock();
        try {
            while(size == capacity){
                fullQueue.await();
            }

            queue[tail] = item;
            tail = (tail + 1) % capacity;
            size++;
            emptyQueue.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error in adding item to queue", e);
        } finally{
            lock.unlock();
        }
    }

    public T take(){
        lock.lock();
        try {
            while(size == 0){
                emptyQueue.await();
            }

            T item = queue[head];
            queue[head] = null;
            head = (head + 1) % capacity;
            size--;
            fullQueue.signal();
            return item;
        } catch (InterruptedException e) {
            throw new RuntimeException("Error in taking item from the queue", e);
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(T item, int timeout){
        lock.lock();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeout);
        try {
            while(size == capacity){
                long remaining = deadline - System.nanoTime();
                if(remaining <= 0) return false;
                fullQueue.await(remaining, TimeUnit.NANOSECONDS);
            }

            queue[tail] = item;
            tail = (tail + 1) % capacity;
            size++;
            emptyQueue.signal();

            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException("Error in adding item to queue", e);
        } finally {
            lock.unlock();
        }
    }

    public T poll(int timeout){
        lock.lock();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeout);
        try {
            while(size == 0){
                long remaining = deadline - System.nanoTime();
                if(remaining <= 0) return null;
                emptyQueue.await(remaining, TimeUnit.NANOSECONDS);
            }

            T item = queue[head];
            queue[head] = null;
            head = (head + 1) % capacity;
            size--;
            fullQueue.signal();
            return item;
        } catch (InterruptedException e) {
            throw new RuntimeException("Error in taking item from the queue", e);
        } finally {
            lock.unlock();
        }
    }
}
