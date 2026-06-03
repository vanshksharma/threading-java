package map;

public class MyHashMap<K,V>{
    private int capacity;
    private final float loadFactor;
    private int size;
    private MapNode[] buckets;

    private class MapNode{
        final K key;
        V value;
        MapNode next;

        MapNode(K key, V value){
            this.key = key;
            this.value = value;
            this.next = null;
        }

        K getKey(){
            return key;
        }

        V getValue(){
            return value;
        }

        MapNode getNext(){
            return next;
        }

        void setValue(V value){
            this.value = value;
        }

        void setNext(MapNode next){
            this.next = next;
        }
    }

    MyHashMap(){
        this(16);
    }

    MyHashMap(int capacity){
        this(capacity, 0.75F);
    }

    @SuppressWarnings("unchecked")
    MyHashMap(int capacity, float loadFactor){
        this.capacity = nextPowerOfTwo(capacity);
        this.loadFactor = loadFactor;
        buckets = (MapNode[]) new Object[this.capacity];
        size = 0;
    }

    private int nextPowerOfTwo(int capacity){
        int n = 1;
        while(n < capacity){
            n <<= 1;
        }

        return n;
    }

    private int getIndex(K key){
        return (key.hashCode() & 0x7fffffff) % capacity;
    }

    public void put(K key, V value){
        int index = getIndex(key);
        MapNode head = buckets[index];
        MapNode prev = null;
        if(head == null){
            MapNode node = new MapNode(key, value);
            buckets[index] = node;
            size++;
            if((float) size / capacity >= loadFactor) {
                reHash();
            }
        }
        else{
            while(head != null){
                if(head.getKey().equals(key)){
                    head.setValue(value);
                    break;
                }
                
                prev = head;
                head = head.getNext();
            }
            
            if(head == null){
                MapNode node = new MapNode(key, value);
                prev.setNext(node);
                size++;
                if((float) size / capacity >= loadFactor) {
                    reHash();
                }
            }
        }
    }

    public V get(K key){
        V value = null;
        int index = getIndex(key);
        MapNode head = buckets[index];
        while(head != null){
            if(head.getKey().equals(key)){
                value = head.getValue();
                break;
            }

            head = head.getNext();
        }

        return value;
    }

    public void remove(K key){
        int index = getIndex(key);
        MapNode head = buckets[index];
        if(head != null && head.getKey().equals(key)){
            buckets[index] = head.getNext();
            size--;
        }
        else{
            MapNode prev = head;
            head = head.getNext();
            while(head != null){
                if(head.getKey().equals(key)){
                    prev.setNext(head.getNext());
                    head.setNext(null);
                    size--;
                    break;
                }

                prev = head;
                head = head.getNext();
            }
        }
    }

    public int size(){
        return size;
    }

    public boolean containsKey(K key){
        int index = getIndex(key);
        MapNode head = buckets[index];
        while(head != null){
            if(head.getKey().equals(key)){
                return true;
            }

            head = head.getNext();
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void reHash(){
        capacity *= 2;
        MapNode[] temp = buckets;
        buckets = (MapNode[]) new Object[capacity];
        for (MapNode mapNode : temp) {
            MapNode head = mapNode;
            while (head != null) {
                MapNode next = head.getNext();
                put(head.getKey(), head.getValue());
                head = next;
            }
        }
    }
}