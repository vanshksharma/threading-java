package basic;

public class Main {
    public static void main(String[] args) throws InterruptedException {
    Question1 q1 = new Question1(); // Print Even Odd using 2 threads
    Question2 q2 = new Question2(); // Print 1 to N using M threads
    Question3 q3 = new Question3(); // Print Ping Pong using Semaphores
    Question4 q4 = new Question4(); // Print A-B-C in order using Semaphores    
    Question5 q5 = new Question5(); // Print Fizz Buzz using 4 threads
    Question6 q6 = new Question6(); // Print Zero Even Odd using Reentrant Lock and Condition
    Question7 q7 = new Question7(); // Traffic Light controller using 4 threads
    
    // q1.solve();
    // q2.solve();
    // q3.solve();
    // q4.solve();
    // q5.solve();
    // q6.solve();
    q7.solve();

    }
}