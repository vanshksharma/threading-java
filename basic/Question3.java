package basic;

import java.util.concurrent.Semaphore;

public class Question3 {
    int counter = 1;
    final int LIMIT = 100;
    Semaphore pingSemaphore = new Semaphore(1);
    Semaphore pongSemaphore = new Semaphore(0);

    void printPing(){
        while(true){
            try {
                pingSemaphore.acquire();
                if(counter > LIMIT) break;
                System.out.println("PING; " + "Counter is: " + counter + "; printed by " + Thread.currentThread().getName());
                counter++;
            } catch (InterruptedException e) {
            }
            finally{
                pongSemaphore.release();
            }
        }
    }

    void printPong(){
        while(true){
            try {
                pongSemaphore.acquire();
                if(counter > LIMIT) break;
                System.out.println("PONG; " + "Counter is: " + counter + "; printed by " + Thread.currentThread().getName());
                counter++;
            } catch (InterruptedException e) {
            }
            finally{
                pingSemaphore.release();
            }
        }
    }

    void solve() throws InterruptedException{
        System.out.println("Solver Started");
        Thread pingThread = new Thread(this::printPing, "Ping Thread");
        Thread pongThread = new Thread(this::printPong, "Pong Thread");
        
        pingThread.start();
        pongThread.start();

        pingThread.join();
        pongThread.join();

        System.out.println("Solver Ended");
    }
}