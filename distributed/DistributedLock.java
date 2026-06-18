package distributed;

import java.util.UUID;

public class DistributedLock {
    private final String name;
    private final UUID token;
    private final long ttl;
    private final long acquiringTime;

    public DistributedLock(String name, long ttl){
        this.name = name;
        token = UUID.randomUUID();
        this.ttl = ttl;
        acquiringTime = System.currentTimeMillis();
    }

    public String getName(){
        return name;
    }

    public UUID getToken(){
        return token;
    }

    public long getTtl(){
        return ttl;
    }

    public long getAcquiringTime(){
        return acquiringTime;
    }

    public boolean isExpired(){
        return System.currentTimeMillis() >= acquiringTime + ttl;
    }
}