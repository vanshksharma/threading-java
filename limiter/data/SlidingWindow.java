package limiter.data;

import java.util.LinkedList;
import java.util.Queue;

public class SlidingWindow {
    private final long duration;
    private final int limit;
    private final Queue<Long> requests;

    public SlidingWindow(long duration, int limit) {
        this.duration = duration;
        this.limit = limit;
        this.requests = new LinkedList<>();
    }

    private void evictExpired(long currentTime){
        long lastAllowedTime = currentTime - duration;
        while(!requests.isEmpty() && requests.peek() < lastAllowedTime){
            requests.poll();
        }
    }

    public synchronized boolean canMakeRequest(){
        long currentTime = System.currentTimeMillis();
        evictExpired(currentTime);
        if(requests.size() < limit){
            requests.add(currentTime);
            return true;
        }
        return false;
    }

    public synchronized boolean isExpired(){
        long currentTime = System.currentTimeMillis();
        evictExpired(currentTime);
        return requests.isEmpty();
    }
}
