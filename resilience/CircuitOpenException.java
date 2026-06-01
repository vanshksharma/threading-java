package resilience;

public class CircuitOpenException extends Exception{
    
    public CircuitOpenException(String message) {
        super(message);
    }
}
