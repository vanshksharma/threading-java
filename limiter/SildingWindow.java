package limiter;

import java.util.LinkedList;
import java.util.Queue;

public class SildingWindow implements LimiterAlgorithm{
    private final int maxRequests;
    private final int windowSize;
    private final Queue<Long> requests;

    public SildingWindow(int maxRequests, int windowSize) {
        this.maxRequests = maxRequests;
        this.windowSize = windowSize;
        this.requests = new LinkedList<>();
    }

    private void evictExpired(long currentTime){
        long lastAllowedTime = currentTime - windowSize * 1000;
        while(!requests.isEmpty() && requests.peek() < lastAllowedTime){
            requests.poll();
        }
    }

    @Override
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        evictExpired(currentTime);

        if(requests.size() < maxRequests){
            requests.add(currentTime);
            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean allowRequest(int tokens) {
        long currentTime = System.currentTimeMillis();
        evictExpired(currentTime);

        if(maxRequests - requests.size() >= tokens){
            for(int i=0; i<tokens; i++){
                requests.add(currentTime);
            }
            return true;
        }

        return false;
    }

    @Override
    public LimiterAlgorithm copy() {
        return new SildingWindow(this.maxRequests, this.windowSize);
    }
}
