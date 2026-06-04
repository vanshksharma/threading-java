package filter;

import java.util.BitSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BloomFilter {
    private final int size;
    private final int hashFunctions;
    private final BitSet bitArray;
    private final ReadWriteLock lock;

    public BloomFilter(int size, int hashFunctions) {
        this.size = size;
        this.hashFunctions = hashFunctions;
        bitArray = new BitSet(size);
        lock = new ReentrantReadWriteLock();
    }

    public <T> void add(T item){
        int firstHash = item.hashCode() & 0x7fffffff;
        int secondHash = (firstHash >>> 16) & 0x7fffffff;

        lock.writeLock().lock();
        try {
            for(int i=0; i<hashFunctions; i++){
                int index = (firstHash + i * secondHash) % size;
                bitArray.set(index);
            }
        }
        finally{
            lock.writeLock().unlock();
        }
    }

    public <T> boolean contains(T item){
        int firstHash = item.hashCode() & 0x7fffffff;
        int secondHash = (firstHash >>> 16) & 0x7fffffff;

        lock.readLock().lock();
        try {
            for(int i=0; i<hashFunctions; i++){
                int index = (firstHash + i * secondHash) % size;
                if(!bitArray.get(index)) return false;
            }
        }
        finally{
            lock.readLock().unlock();
        }

        return true;
    }
}
