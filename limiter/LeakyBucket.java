package limiter;

public class LeakyBucket implements LimiterAlgorithm{
    private final int capacity;
    private final int leakRate;
    private int queueSize;
    private long lastRequestTime;

    public LeakyBucket(int capacity, int leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;
        queueSize = 0;
        lastRequestTime = System.currentTimeMillis();
    }

    private void emptyBucket(){
        long currentTime = System.currentTimeMillis();
        int leakedRequests = (int)((currentTime - lastRequestTime) / 1000) * leakRate;
        queueSize = Math.max(queueSize - leakedRequests, 0);
        lastRequestTime = currentTime;
    }

    @Override
    public synchronized boolean allowRequest() {
        emptyBucket();
        
        if(queueSize < capacity){
            queueSize++;
            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean allowRequest(int tokens) {
        emptyBucket();
        
        if(capacity - queueSize >= tokens){
            queueSize += tokens;
            return true;
        }

        return false;
    }

    @Override
    public LimiterAlgorithm copy() {
        return new LeakyBucket(this.capacity, this.leakRate);
    }
}
