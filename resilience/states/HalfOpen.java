package resilience.states;

import java.util.concurrent.Callable;
import resilience.CircuitBreaker;
import resilience.CircuitOpenException;

public class HalfOpen implements CircuitState{
    private volatile static CircuitState instance = null;

    @Override
    public void transition(CircuitBreaker breaker) {
        if(breaker.getSuccessCount() >= breaker.getSuccessThreshold()){
            breaker.setState(Closed.getInstance());
            breaker.setFailureCount(0);
            breaker.setLastFailedRequestTime(null);
        }
    }

    @Override
    public <T> T execute(Callable<T> task, CircuitBreaker breaker) throws CircuitOpenException {
        T value = null;
        boolean exceptionThrown = false;
        CircuitState currentState;

        breaker.getLock().lock();
        try {
            currentState = breaker.getState();
            if(!(currentState instanceof HalfOpen)){
                return null;
            }
        }
        finally{
            breaker.getLock().unlock();
        }

        try {
            value = task.call();
        } catch (Exception e) {
            exceptionThrown = true;
        }

        breaker.getLock().lock();
        try {
            if(breaker.getState() instanceof HalfOpen){
                if(exceptionThrown){
                    breaker.setState(Open.getInstance());
                    breaker.setSuccessCount(0);
                    breaker.setFailureCount(0);
                    breaker.setLastFailedRequestTime(System.currentTimeMillis());
                }
                else{
                    breaker.setSuccessCount(breaker.getSuccessCount() + 1);
                    transition(breaker);
                }
            }
        }
        finally{
            breaker.getLock().unlock();
        }

        return value;
    }

    public static CircuitState getInstance() {
        if(instance == null){
            synchronized (HalfOpen.class) {
                if(instance == null){
                    instance = new HalfOpen();
                }
            }
        }
        
        return instance;
    }
}
