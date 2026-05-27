package basic;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Question6 {
    boolean zero = true;
    int counter = 1;
    final int LIMIT = 100;
    Lock lock = new ReentrantLock();
    Condition zeroTurn = lock.newCondition();
    Condition evenTurn = lock.newCondition();
    Condition oddTurn = lock.newCondition();

    void printZero(){
        while(true){
            try {
                lock.lock();
                while(counter <= LIMIT && !zero){
                    zeroTurn.await();
                }

                if(counter > LIMIT) break;

                System.out.println("Zero " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
                zero = false;
                
                
            } catch (InterruptedException e) {
            } finally {
                evenTurn.signalAll();
                oddTurn.signalAll();
                lock.unlock();
            }
        }
    }

    void printEven(){
        while(true){
            try {
                lock.lock();
                while(counter <= LIMIT && (zero || counter % 2 != 0)){
                    evenTurn.await();
                }

                if(counter > LIMIT) break;

                System.out.println("Even " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
                zero = true;
                counter++;
            } catch (InterruptedException e) {
            } finally {
                zeroTurn.signalAll();
                lock.unlock();
            }
        }
    }

    void printOdd(){
        while(true){
            try {
                lock.lock();
                while(counter <= LIMIT && (zero || counter % 2 == 0)){
                    oddTurn.await();
                }

                if(counter > LIMIT) break;

                System.out.println("Odd " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
                zero = true;
                counter++;
            } catch (InterruptedException e) {
            } finally {
                zeroTurn.signalAll();
                lock.unlock();
            }
        }
    }
    void solve() throws InterruptedException{
        System.out.println("Solver Started");

        Thread t1 = new Thread(this::printOdd, "Odd Thread");
        Thread t2 = new Thread(this::printEven, "Even Thread");
        Thread t3 = new Thread(this::printZero, "Zero Thread");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("Solver Ended");
    }
}
