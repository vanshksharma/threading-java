package basic;

public class Question5 {
    int counter = 1;
    final int LIMIT = 100;

    synchronized void printFizz(){
        while(true){
            while(counter <= LIMIT && (counter % 3 != 0 || counter % 5 == 0)){
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            if(counter > LIMIT) break;

            System.out.println("Fizz " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
            counter++;
            notifyAll();
        }
    }

    synchronized void printBuzz(){
        while(true){
            while(counter <= LIMIT && (counter % 3 == 0 || counter % 5 != 0)){
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            if(counter > LIMIT) break;

            System.out.println("Buzz; " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
            counter++;
            notifyAll();
        }
    }

    synchronized void printFizzBuzz(){
        while(true){
            while(counter <= LIMIT && (counter % 3 != 0 || counter % 5 != 0)){
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            if(counter > LIMIT) break;

            System.out.println("FizzBuzz " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
            counter++;
            notifyAll();
        }
    }

    synchronized void printNumber(){
        while(true){
            while(counter <= LIMIT && (counter % 3 == 0 || counter % 5 == 0)){
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            if(counter > LIMIT) break;

            System.out.println("Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
            counter++;
            notifyAll();
        }
    }

    void solve() throws InterruptedException{
        System.out.println("Solver Started");

        Thread t1 = new Thread(this::printFizz, "Fizz Thread");
        Thread t2 = new Thread(this::printBuzz, "Buzz Thread");
        Thread t3 = new Thread(this::printFizzBuzz, "Fizz Buzz Thread");
        Thread t4 = new Thread(this::printNumber, "Number Thread");

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        System.out.println("Solver Ended");
    }
}
