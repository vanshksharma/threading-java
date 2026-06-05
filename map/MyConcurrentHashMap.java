package map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyConcurrentHashMap<K,V>{
    private int capacity;
    private final float loadFactor;
    private AtomicInteger size;
    private volatile MapNode[] buckets;
    private List<Lock> stripes;

    private class MapNode{
        final K key;
        volatile V value;
        volatile MapNode next;

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

    public MyConcurrentHashMap(){
        this(16);
    }

    public MyConcurrentHashMap(int capacity){
        this(capacity, 0.75F);
    }

    @SuppressWarnings("unchecked")
    public MyConcurrentHashMap(int capacity, float loadFactor){
        this.capacity = nextPowerOfTwo(capacity);
        this.loadFactor = loadFactor;
        buckets = (MapNode[]) new Object[this.capacity];
        size = new AtomicInteger(0);
        stripes = new ArrayList<>();
        for(int i=0; i<this.capacity; i++){
            stripes.add(new ReentrantLock());
        }
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

    private void putNode(K key, V value, boolean hashCheck){
        int index = getIndex(key);
        Lock stripeLock = stripes.get(index);
        stripeLock.lock();
        try{
            MapNode head = buckets[index];
            MapNode prev = null;
            if(head == null){
                MapNode node = new MapNode(key, value);
                buckets[index] = node;
                size.incrementAndGet();
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
                    size.incrementAndGet();
                }
            }
        }
        finally {
            stripeLock.unlock();
        }

        if(hashCheck && (float) size.get() / capacity >= loadFactor) {
            reHash();
        }
    }

    public void put(K key, V value){
        putNode(key, value, true);
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
        Lock stripeLock = stripes.get(index);
        stripeLock.lock();
        try{
            MapNode head = buckets[index];
            if(head != null && head.getKey().equals(key)){
                buckets[index] = head.getNext();
                size.decrementAndGet();
            }
            else{
                MapNode prev = head;
                head = head.getNext();
                while(head != null){
                    if(head.getKey().equals(key)){
                        prev.setNext(head.getNext());
                        head.setNext(null);
                        size.decrementAndGet();
                        break;
                    }

                    prev = head;
                    head = head.getNext();
                }
            }
        }
        finally {
            stripeLock.unlock();
        }
    }

    public int size(){
        return size.get();
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
        for(Lock stripeLock: stripes){
            stripeLock.lock();
        }
        try{
            if((float) size.get() / capacity >= loadFactor) {
                for(int i=0; i<capacity; i++){
                    stripes.add(new ReentrantLock());
                }
                capacity *= 2;
                MapNode[] temp = buckets;
                buckets = (MapNode[]) new Object[capacity];
                for (MapNode mapNode : temp) {
                    MapNode head = mapNode;
                    while (head != null) {
                        MapNode next = head.getNext();
                        putNode(head.getKey(), head.getValue(), false);
                        head = next;
                    }
                }
            }
        }
        finally {
            for(int i=0; i<capacity/2; i++){
                stripes.get(i).unlock();
            }
        }
    }
}
