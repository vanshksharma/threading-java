package limiter.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import limiter.data.SlidingWindow;

public class SlidingWindowStrategy implements RateLimitStrategy{
    private final Map<String, SlidingWindow> windows;
    private final long duration;
    private final int limit;

    public SlidingWindowStrategy(int limit, long duration) {
        this.windows = new ConcurrentHashMap<>();
        this.duration = duration;
        this.limit = limit;

        Thread sweeper = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()){
                windows.values().removeIf(SlidingWindow::isExpired);
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
        SlidingWindow window = windows.computeIfAbsent(ip, (key) -> {
            return new SlidingWindow(duration, limit);
        });

        return window.canMakeRequest();
    }
}
