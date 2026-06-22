package balancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinRouter {
    private final AtomicInteger index;

    public RoundRobinRouter() {
        this.index = new AtomicInteger(0);
    }

    public String getNextService(List<String> services){
        if(services.isEmpty()) throw new IllegalStateException("No services found");
        int nextIndex = index.getAndUpdate((current) -> {
            if (current == Integer.MAX_VALUE) return 0;
            return current + 1;
        });
        return services.get(nextIndex % services.size());
    }
}
