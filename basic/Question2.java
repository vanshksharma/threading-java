package basic;

import java.util.ArrayList;
import java.util.List;

public class Question2 {
    int counter = 1;
    final int LIMIT = 500;
    final int THREADS = 50;
    int chance = 0;

    synchronized void print(){
        while(true){
            while(counter <= LIMIT && !Thread.currentThread().getName().equals("Thread " + chance)){
                try {
                    wait();
                } catch (InterruptedException ignore) {
                }
            }

            if(counter > LIMIT) break;

            System.out.println("Counter is: " + counter + "; printed by " + Thread.currentThread().getName());
            counter++;
            chance = (chance + 1) % THREADS;
            notifyAll();
        }
    }

    void solve() throws InterruptedException{
        System.out.println("Solver Stared");
        List<Thread> l = new ArrayList<>();

        for(int i=0; i < THREADS; i++){
            Thread t = new Thread(this::print, "Thread " + i);
            l.add(t);
            t.start();
        }

        for (Thread t : l) {
            t.join();
        }

        System.out.println("Solver Ended");
    }
}
