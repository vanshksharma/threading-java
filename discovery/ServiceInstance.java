package discovery;

import java.util.Objects;

public class ServiceInstance {
    private final String name;
    private final String host;
    private final int port;
    private volatile long lastHeartbeat;

    public ServiceInstance(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public String getName(){
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeatbeat(long heartBeatTime){
        lastHeartbeat = heartBeatTime;
    }

    public boolean isAlive(long ttl){
        return System.currentTimeMillis() - lastHeartbeat < ttl;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof ServiceInstance)) return false;
        ServiceInstance other = (ServiceInstance) o;
        return other.getPort() == port && other.getName().equals(name) && other.getHost().equals(host);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name, host, port);
    }
}
