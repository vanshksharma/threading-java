package limiter;

public interface LimiterAlgorithm {
    boolean allowRequest();
    boolean allowRequest(int tokens);
    LimiterAlgorithm copy();
}
