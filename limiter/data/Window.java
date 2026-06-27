package limiter.data;

public class Window {
    private final long duration;
    private final int limit;
    private int requestCount;
    private long startTime;

    public Window(long duration, int limit) {
        this.duration = duration;
        this.limit = limit;
        this.requestCount = 0;
        this.startTime = System.currentTimeMillis();
    }

    public synchronized boolean canMakeRequest(){
        if(System.currentTimeMillis() - startTime < duration){
            if(requestCount + 1 <= limit){
                requestCount++;
                return true;
            }

            return false;
        }
        else{
            requestCount = 1;
            startTime = System.currentTimeMillis();
            return true;
        }
    }

    public synchronized boolean isExpired(){
        return System.currentTimeMillis() - startTime >= duration;
    }
}
