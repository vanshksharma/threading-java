package limiter.strategy;

public interface RateLimitStrategy {
    boolean isAllowed(String ip);
}
