package resilience.states;

import java.util.concurrent.Callable;

import resilience.CircuitBreaker;
import resilience.CircuitOpenException;

public class HalfOpen implements CircuitStates{

    @Override
    public void transition(CircuitBreaker breaker) {
        if(breaker.getSuccessCount() >= breaker.getSuccessThreshold()){
            breaker.setState(new Closed());
            breaker.setFailureCount(0);
            breaker.setLastFailedRequestTime(null);
        }
    }

    @Override
    public <T> T execute(Callable<T> task, CircuitBreaker breaker) throws CircuitOpenException {
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
                breaker.setState(new Open());
                breaker.setSuccessCount(0);
                breaker.setFailureCount(0);
                breaker.setLastFailedRequestTime(System.currentTimeMillis());
            }
            else{
                breaker.setSuccessCount(breaker.getSuccessCount() + 1);
                transition(breaker);
            }
        } catch (Exception ignore) {}
        finally{
            breaker.getLock().unlock();
        }

        return value;
    }
}
