package cache;

import java.util.HashMap;
import java.util.Map;

import hash.ConsistentHashing;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheCluster {
    private final ConsistentHashing ring;
    private final Map<Integer, String> nodes;
    private final ReadWriteLock lock;

    public CacheCluster(int virtualNodes) {
        ring = new ConsistentHashing(virtualNodes);
        nodes = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    private String getServerString(int nodeNumber){
        return "Server:" + nodeNumber;
    }

    public void addNode(int nodeNumber, String address){
        lock.writeLock().lock();
        try {
            if(nodes.containsKey(nodeNumber)){
                throw new IllegalStateException("Node already exists");
            }
            
            nodes.put(nodeNumber, address);
            ring.addServer(getServerString(nodeNumber));
        }
        finally{
            lock.writeLock().unlock();
        }
    }

    public void removeNode(int nodeNumber){
        lock.writeLock().lock();
        try {
            if(!nodes.containsKey(nodeNumber)){
                throw new IllegalStateException("Node does not exist on the ring");
            }
            
            nodes.remove(nodeNumber);
            ring.removeServer(getServerString(nodeNumber));
        }
        finally{
            lock.writeLock().unlock();
        }
    }

    public String getAddress(String key){
        lock.readLock().lock();
        try {
            String server = ring.getServer(key);
            int nodeNumber = Integer.parseInt(server.substring(server.lastIndexOf(":") + 1));
            return nodes.get(nodeNumber);
        } finally {
            lock.readLock().unlock();
        }
    }
}
