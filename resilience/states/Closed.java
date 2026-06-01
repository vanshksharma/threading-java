package resilience.states;

import java.util.concurrent.Callable;
import resilience.CircuitBreaker;

public class Closed implements CircuitState{
    private static CircuitState instance = null;

    @Override
    public void transition(CircuitBreaker breaker) {
        if(breaker.getFailureCount() >= breaker.getFailureThreshold()){
            breaker.setState(Open.getInstance());
            breaker.setFailureCount(0);
            breaker.setSuccessCount(0);
            breaker.setLastFailedRequestTime(System.currentTimeMillis());
        }
    }

    @Override
    public <T> T execute(Callable<T> task, CircuitBreaker breaker) {
        T value = null;
        boolean exceptionThrown = false;

        try {
            value = task.call();
        } catch (Exception e) {
            exceptionThrown = true;
        }

        breaker.getLock().lock();
        try {
            if(exceptionThrown){
                breaker.setFailureCount(breaker.getFailureCount() + 1);
            }
            else{
                breaker.setFailureCount(0);
                breaker.setLastFailedRequestTime(null);
            }
            transition(breaker);
        } catch (Exception ignore) {}
        finally{
            breaker.getLock().unlock();
        }

        return value;
    }

    public static CircuitState getInstance() {
        if(instance == null){
            synchronized (Closed.class) {
                if(instance == null){
                    instance = new Closed();
                }
            }
        }
        
        return instance;
    }
}
