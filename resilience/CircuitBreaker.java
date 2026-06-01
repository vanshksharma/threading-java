package resilience;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import resilience.states.CircuitState;
import resilience.states.Closed;

public class CircuitBreaker {
    private CircuitState state;
    private final int failureThreshold;
    private final int successThreshold;
    private final int timeout;
    private int successCount;
    private int failureCount;
    private Long lastFailedRequestTime;
    private final Lock lock;

    public CircuitBreaker(int failureThreshold, int successThreshold, int timeout) {
        this.state = new Closed();
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.timeout = timeout;
        successCount = 0;
        failureCount = 0;
        lastFailedRequestTime = null;
        lock = new ReentrantLock();
    }

    public <T> T execute(Callable<T> task) throws CircuitOpenException{
        return state.execute(task, this);
    }

    public CircuitState getState() {
        return state;
    }

    public void setState(CircuitState state) {
        this.state = state;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public int getSuccessThreshold() {
        return successThreshold;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public Long getLastFailedRequestTime() {
        return lastFailedRequestTime;
    }

    public void setLastFailedRequestTime(Long lastFailedRequestTime) {
        this.lastFailedRequestTime = lastFailedRequestTime;
    }

    public Lock getLock() {
        return lock;
    }
}
