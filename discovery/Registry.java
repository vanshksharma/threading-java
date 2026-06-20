package discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Registry {
    private final Map<String, List<ServiceInstance>> services;
    private final long ttl;

    public Registry(long ttl) {
        this.services = new ConcurrentHashMap<>();
        this.ttl = ttl;
        Thread runner = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()){
                services.forEach((name, instances) -> {
                    services.compute(name, (key, existing) -> {
                        if(existing == null) return null;
                        existing.removeIf((service) -> !service.isAlive(ttl));
                        if(existing.isEmpty()) return null;
                        return existing;
                    });
                });

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Sweeper Thread");
        
        runner.setDaemon(true);
        runner.start();
    }

    public void register(String name, String host, int port){
        services.compute(name, (key, existing) -> {
            ServiceInstance service = new ServiceInstance(name, host, port);
            if(existing == null){
                existing = new ArrayList<>();
                existing.add(service);
            }
            else{
                if(existing.contains(service)) throw new IllegalStateException("Service already exists");
                existing.add(service);
            }

            return existing;
        });
    }

    public void heartbeat(String name, String host, int port){
        services.compute(name, (key, existing) -> {
            if(existing == null) throw new IllegalStateException("Service not found");
            ServiceInstance service = new ServiceInstance(name, host, port);
            int index = existing.indexOf(service);
            if(index == -1) throw new IllegalStateException("Service not found");
            existing.get(index).setLastHeatbeat(System.currentTimeMillis());
            return existing;
        });
    }

    public void deRegister(String name, String host, int port){
        services.compute(name, (key, existing) -> {
            if(existing == null) throw new IllegalStateException("Service not found");
            ServiceInstance service = new ServiceInstance(name, host, port);
            int index = existing.indexOf(service);
            if(index == -1) throw new IllegalStateException("Service not found");
            existing.remove(index);
            if(existing.isEmpty()){
                return null;
            }
            return existing;
        });
    }

    public List<ServiceInstance> getInstances(String name){
        List<ServiceInstance> l = services.get(name);
        if(l == null) return Collections.emptyList();
        return l.stream().filter((service) -> service.isAlive(ttl)).toList();
    }
}
