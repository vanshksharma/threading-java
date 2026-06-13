package hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        return virtualServer.substring(0, virtualServer.lastIndexOf('_'));
    }

    private int hash(String key) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(key.getBytes(StandardCharsets.UTF_8));
        return ((bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8  | (bytes[3] & 0xFF)) & 0x7fffffff;
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
    }
}

    public void addServer(String server){
        lock.writeLock().lock();
        try {
            for(int i=0; i<virtualNodes; i++){
                String serverId = server + "_" + i;
                int serverHash = hash(serverId);
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
            if (ring.isEmpty()) throw new IllegalStateException("No servers in ring");
            int keyHash = hash(key);
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
                int serverHash = hash(serverId);
                ring.remove(serverHash);
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
