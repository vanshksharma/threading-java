package limiter.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import limiter.data.Bucket;
import limiter.data.Window;

public class TokenBucketStrategy implements RateLimitStrategy{
    private final Map<String, Bucket> buckets;
    private final int capacity;
    private final double refillRate;

    public TokenBucketStrategy(int capacity, double refillRate) {
        this.buckets = new ConcurrentHashMap<>();
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    @Override
    public boolean isAllowed(String ip) {
        Bucket bucket = buckets.computeIfAbsent(ip, (key) -> {
            return new Bucket(capacity, refillRate);
        });

        return bucket.canMakeRequest();
    }
}
