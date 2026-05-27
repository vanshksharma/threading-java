package basic;

import java.util.concurrent.Semaphore;

public class Question4 {
    Semaphore aSem = new Semaphore(1);
    Semaphore bSem = new Semaphore(0);
    Semaphore cSem = new Semaphore(0);
    int counter = 1;
    final int LIMIT = 50;
    
    void printA(){
        while (true) { 
            try {
                aSem.acquire();
                if(counter > LIMIT) break;
                System.out.println("A; " + "Counter is: " + counter + "; printed by " + Thread.currentThread().getName());
            } catch (InterruptedException e) {
            } 
            finally {
                bSem.release();
            }
        }
    }

    void printB(){
        while (true) { 
            try {
                bSem.acquire();
                if(counter > LIMIT) break;
                System.out.println("B; " + "Counter is: " + counter + "; printed by " + Thread.currentThread().getName());
            } catch (InterruptedException e) {
            } 
            finally {
                cSem.release();
            }
        }
    }

    void printC(){
        while (true) { 
            try {
                cSem.acquire();
                if(counter > LIMIT) break;
                System.out.println("C; " + "Counter is: " + counter + "; printed by " + Thread.currentThread().getName());
                counter++;
            } catch (InterruptedException e) {
            } 
            finally {
                aSem.release();
            }
        }
    }

    void solve() throws InterruptedException {
        System.out.println("Solver Started");
        Thread aThread = new Thread(this::printA, "A Thread");
        Thread bThread = new Thread(this::printB, "B Thread");
        Thread cThread = new Thread(this::printC, "C Thread");

        aThread.start();
        bThread.start();
        cThread.start();

        aThread.join();
        bThread.join();
        cThread.join();

        System.out.println("Solver Ended");
    }
}
