package distributed; 

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedLockManager {
    private final Map<String, DistributedLock> locks;

    public DistributedLockManager(){
        locks = new ConcurrentHashMap<>();
        Thread sweeper = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()){
                locks.values().removeIf(DistributedLock::isExpired);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }, "Lock Sweeper");

        sweeper.setDaemon(true);
        sweeper.start();
    }

    public String acquire(String lockName, long ttl){
        DistributedLock lock = locks.compute(lockName, (key, existing) -> {
            if(existing != null && !existing.isExpired()){
                throw new IllegalStateException("Lock is already held");
            }

            return new DistributedLock(lockName, ttl);
        });

        return lock.getToken().toString();
    }

    public void release(String lockName, String tokenString){
        UUID clientToken = UUID.fromString(tokenString);
        locks.compute(lockName, (key, existing) -> {
            if(existing == null){
                throw new IllegalStateException("Lock does not exist");
            }
            else if(existing.isExpired()){
                throw new IllegalStateException("Lock is expired");
            }
            else if(!existing.getToken().equals(clientToken)){
                throw new IllegalCallerException("Lock is not held by the caller");
            }
            else{
                return null;
            }
        });
    }
}
