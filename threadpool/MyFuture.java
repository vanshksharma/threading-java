package threadpool;

public class MyFuture<T> {
    private T value;
    private boolean isCompleted;
    private boolean isExceptionRaised;
    private Exception exception;

    MyFuture() {
        value = null;
        isCompleted = false;
        isExceptionRaised = false;
        exception = null;
    }

    public synchronized T get() {
        while (!isCompleted) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        if (isExceptionRaised) {
            throw new CustomException(exception.getMessage(), exception);
        }

        return value;
    }

    public synchronized void setValue(T value) {
        isCompleted = true;
        this.value = value;
        notifyAll();
    }

    public synchronized void setException(Exception exception) {
        value = null;
        isCompleted = true;
        isExceptionRaised = true;
        this.exception = exception;
        notifyAll();
    }
}
