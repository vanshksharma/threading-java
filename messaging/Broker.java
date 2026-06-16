package messaging;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Broker {
    private final Map<String, Queue<Message>> messages;
    private final Lock lock;

    public Broker() {
        messages = new HashMap<>();
        lock = new ReentrantLock();
    }

    public void createTopic(String name){
        lock.lock();
        try {
            if(messages.containsKey(name)) throw new IllegalStateException("Topic already exists");
            messages.put(name, new LinkedList<>());
        }
        finally{
            lock.unlock();
        }
    }

    public void publish(String topic, Message message){
        lock.lock();
        try {
            if(!messages.containsKey(topic)) throw new IllegalStateException("Topic does not exist");
            messages.get(topic).add(message);
        }
        finally{
            lock.unlock();
        }
    }

    public Message consume(String topic){
        lock.lock();
        try {
            if(!messages.containsKey(topic)) throw new IllegalStateException("Topic does not exist");
            return messages.get(topic).poll();
        }
        finally{
            lock.unlock();
        }
    }
}
