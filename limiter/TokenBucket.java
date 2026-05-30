package limiter;

public class TokenBucket implements LimiterAlgorithm {
    final int capacity;
    final int refillRate;
    int remaining;
    long lastRequestTime;

    public TokenBucket(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        remaining = capacity;
        lastRequestTime = System.currentTimeMillis();
    }

    private void refillTokens(){
        int secondsElapsed = (int)((System.currentTimeMillis() - lastRequestTime) / 1000);
        remaining = Math.min(remaining + secondsElapsed * refillRate, capacity);
        lastRequestTime = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean allowRequest() {
        refillTokens();

        if(remaining > 0){
            remaining --;
            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean allowRequest(int tokens) {
        refillTokens();

        if(remaining >= tokens){
            remaining -= tokens;
            return true;
        }

        return false;
    }

    @Override
    public LimiterAlgorithm copy() {
        return new TokenBucket(this.capacity, this.refillRate);
    }
}
