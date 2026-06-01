package resilience.states;

import java.util.concurrent.Callable;
import resilience.CircuitBreaker;
import resilience.CircuitOpenException;

public interface CircuitStates {
    void transition(CircuitBreaker breaker);
    <T> T execute(Callable<T> task, CircuitBreaker breaker) throws CircuitOpenException; 
}
