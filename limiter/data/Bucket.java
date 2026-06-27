package limiter.data;

public class Bucket {
    private final int capacity;
    private final double refillRate;
    private double tokens;
    private long lastRequestTime;

    public Bucket(int capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate / 1000;
        this.tokens = capacity;
        this.lastRequestTime = System.currentTimeMillis();
    }

    private void refillTokens(){
        long timeElapsed = System.currentTimeMillis() - lastRequestTime;
        double newTokens = refillRate * timeElapsed;
        tokens = Math.min(capacity, tokens + newTokens);
        lastRequestTime = System.currentTimeMillis();
    }

    public synchronized boolean canMakeRequest(){
        refillTokens();
        if(tokens >= 1){
            tokens--;
            return true;
        }
        return false; 
    }
}
