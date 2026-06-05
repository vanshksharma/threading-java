package hash;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConsistentHashing {
    private final int virtualNodes;
    private final NavigableMap<Integer, String> ring;
    private final ReadWriteLock lock;

    public ConsistentHashing(int virtualNodes) {
        this.virtualNodes = virtualNodes;
        ring = new TreeMap<>();
        lock = new ReentrantReadWriteLock();
    }

    private String getActualServer(String virtualServer){
        return virtualServer.split("_")[0];
    }

    public void addServer(String server){
        lock.writeLock().lock();
        try {
            for(int i=0; i<virtualNodes; i++){
                String serverId = server + "_" + i;
                int serverHash = serverId.hashCode() & 0x7fffffff;
                ring.put(serverHash, serverId);
            }
        }
        finally{
            lock.writeLock().unlock();
        }
    }

    public String getServer(String key){
        lock.readLock().lock();
        try {
            int keyHash = key.hashCode() & 0x7fffffff;
            Integer virtualServerHash = ring.ceilingKey(keyHash);
            if(virtualServerHash == null){
                return getActualServer(ring.get(ring.firstKey()));
            }

            return getActualServer(ring.get(virtualServerHash));
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void removeServer(String server){
        lock.writeLock().lock();
        try {
            for(int i=0; i<virtualNodes; i++){
                String serverId = server + "_" + i;
                int serverHash = serverId.hashCode() & 0x7fffffff;
                ring.remove(serverHash);
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
