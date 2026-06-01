package resilience.states;

import java.util.concurrent.Callable;
import resilience.CircuitBreaker;
import resilience.CircuitOpenException;

public class Open implements CircuitState{
    private static CircuitState instance;

    @Override
    public void transition(CircuitBreaker breaker) {
        if(System.currentTimeMillis() - breaker.getLastFailedRequestTime() >= breaker.getTimeout() * 1000){
            breaker.setState(HalfOpen.getInstance());
            breaker.setSuccessCount(0);
        }
    }

    @Override
    public <T> T execute(Callable<T> task, CircuitBreaker breaker) throws CircuitOpenException {
        CircuitState currentState;
        breaker.getLock().lock();
        try {
            if(breaker.getState() instanceof Open){
                transition(breaker);
            }
            currentState = breaker.getState();
        } finally {
            breaker.getLock().unlock();
        }

        if(currentState instanceof Open){
            throw new CircuitOpenException("Circuit is in OPEN state");
        }
        return currentState.execute(task, breaker);
    }

    public static CircuitState getInstance() {
        if(instance == null){
            synchronized (Open.class) {
                if(instance == null){
                    instance = new Open();
                }
            }
        }
        
        return instance;
    }
}
