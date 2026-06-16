package messaging;

public class Message {
    private final String content;
    private final String topic;
    private final long timestamp;

    public Message(String content, String topic) {
        this.content = content;
        this.topic = topic;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public String getTopic() {
        return topic;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
