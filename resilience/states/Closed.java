package resilience.states;

import java.util.concurrent.Callable;
import resilience.CircuitBreaker;
import resilience.CircuitOpenException;

public class Closed implements CircuitState{
    private volatile static CircuitState instance = null;

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
    public <T> T execute(Callable<T> task, CircuitBreaker breaker) throws CircuitOpenException{
        T value = null;
        boolean exceptionThrown = false;
        CircuitState currentState;

        breaker.getLock().lock();
        try {
            currentState = breaker.getState();
            if(!(currentState instanceof Closed)){
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
            if(breaker.getState() instanceof Closed){
                if(exceptionThrown){
                    breaker.setFailureCount(breaker.getFailureCount() + 1);
                }
                else{
                    breaker.setFailureCount(0);
                    breaker.setLastFailedRequestTime(null);
                }
                transition(breaker);
            }
        }
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
