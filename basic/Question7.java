package basic;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Question7 {
    int counter = 1;
    final int LIMIT = 10;
    char chance = 'A';
    Lock lock = new ReentrantLock();
    Condition roadA = lock.newCondition();
    Condition roadB = lock.newCondition();
    Condition roadC = lock.newCondition();
    Condition roadD = lock.newCondition();

    void turnA(){
        while(true){
            try {
                lock.lock();
                while(counter <= LIMIT && chance != 'A'){
                    roadA.await();
                }

                if(counter > LIMIT) break;

                System.out.println("Chance of Road A; " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
                Thread.sleep(500);
                chance = 'B';
            } catch (InterruptedException e) {
            } finally {
                roadB.signalAll();
                lock.unlock();
            }
        }
    }

    void turnB(){
        while(true){
            try {
                lock.lock();
                while(counter <= LIMIT && chance != 'B'){
                    roadB.await();
                }

                if(counter > LIMIT) break;

                System.out.println("Chance of Road B; " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
                Thread.sleep(500);
                chance = 'C';
            } catch (InterruptedException e) {
            } finally {
                roadC.signalAll();
                lock.unlock();
            }
        }
    }

    void turnC(){
        while(true){
            try {
                lock.lock();
                while(counter <= LIMIT && chance != 'C'){
                    roadC.await();
                }

                if(counter > LIMIT) break;

                System.out.println("Chance of Road C; " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
                Thread.sleep(500);
                chance = 'D';
            } catch (InterruptedException e) {
            } finally {
                roadD.signalAll();
                lock.unlock();
            }
        }
    }

    void turnD(){
        while(true){
            try {
                lock.lock();
                while(counter <= LIMIT && chance != 'D'){
                    roadD.await();
                }

                if(counter > LIMIT) break;

                System.out.println("Chance of Road D; " + "Counter: " + counter + " ;printed by " + Thread.currentThread().getName());
                Thread.sleep(500);
                chance = 'A';
                counter++;
            } catch (InterruptedException e) {
            } finally {
                roadA.signalAll();
                lock.unlock();
            }
        }
    }
    void solve() throws InterruptedException{
        System.out.println("Solver Started");
        Thread t1 = new Thread(this::turnA, "A Thread");
        Thread t2 = new Thread(this::turnB, "B Thread");
        Thread t3 = new Thread(this::turnC, "C Thread");
        Thread t4 = new Thread(this::turnD, "D Thread");

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t1.join();
        t1.join();
        t1.join();
        System.out.println("Solver Ended");
    }
}
