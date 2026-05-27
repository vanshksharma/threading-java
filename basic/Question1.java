package basic;

public class Question1 {
    int counter = 1;
    int LIMIT = 100;

    synchronized void printOdd() {
        while (true) {
            while (counter <= LIMIT && counter % 2 == 0) {
                try {
                    wait();
                } catch (InterruptedException ignore) {
                }
            }

            if(counter > LIMIT) break;

            System.out.println("Counter is odd: " + counter + "; printed by: " + Thread.currentThread().getName());
            counter++;
            notifyAll();
        }
    }

    synchronized void printEven() {
        while (true) {
            while (counter <= LIMIT && counter % 2 == 1) {
                try {
                    wait();
                } catch (InterruptedException ignore) {
                }
            }

            if(counter > LIMIT) break;

            System.out.println("Counter is even: " + counter + "; printed by: " + Thread.currentThread().getName());
            counter++;
            notifyAll();
        }
    }

    public void solve() throws InterruptedException{
        System.out.println("Solver Started");
        Thread t1 = new Thread(this::printEven, "Even Thread");
        Thread t2 = new Thread(this::printOdd, "Odd Thread");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Solver Ended");
    }
}
