package limiter.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import limiter.data.Window;

public class FixedWindowStrategy implements RateLimitStrategy{
    private final Map<String, Window> windows;
    private final long duration;
    private final int limit;

    public FixedWindowStrategy(long duration, int limit){
        this.windows = new ConcurrentHashMap<>();
        this.duration = duration;
        this.limit = limit;

        Thread sweeper = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()){
                windows.values().removeIf(Window::isExpired);
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Sweeper Thread");

        sweeper.setDaemon(true);
        sweeper.start();
    }

    @Override
    public boolean isAllowed(String ip) {
        Window window = windows.computeIfAbsent(ip, (key) -> {
            return new Window(duration, limit);
        });

        return window.canMakeRequest();
    }
}
