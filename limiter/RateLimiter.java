package limiter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RateLimiter {
    private final Map<String, LimiterAlgorithm> limits;
    private final LimiterAlgorithm algorithm;
    private final ReadWriteLock lock;

    public RateLimiter(LimiterAlgorithm algorithm) {
        this.algorithm = algorithm;
        limits = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    public void registerClient(String key){
        try {
            lock.writeLock().lock();
            if(!limits.containsKey(key)) limits.put(key, algorithm.copy());
        }
        catch (Exception ignore) {}
        finally{
            lock.writeLock().unlock();
        }
    }

    public boolean allowRequest(String key) throws IllegalAccessException{
        return getBucket(key).allowRequest();
    }

    public boolean allowRequest(String key, int tokens) throws IllegalAccessException{
        return getBucket(key).allowRequest(tokens);
    }

    public void deRegisterClient(String key){
        try {
            lock.writeLock().lock();
            limits.remove(key);
        }
        catch (Exception ignore) {}
        finally{
            lock.writeLock().unlock();
        }
    }

    private LimiterAlgorithm getBucket(String key) throws IllegalAccessException{
        LimiterAlgorithm algo = null;
        try {
            lock.readLock().lock();
            if(!limits.containsKey(key)) throw new IllegalAccessException("Key not Found");
            algo = limits.get(key);
        } catch (IllegalAccessException e) { throw e;}
        catch (Exception ignore) {}
        finally{
            lock.readLock().unlock();
        }

        return algo;
    }
}
