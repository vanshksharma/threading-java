package cache;

import java.util.HashMap;
import java.util.Map;

public class LRUCache<K,V> {
    private final int capacity;
    private ListNode head;
    private ListNode tail;
    private Map<K, ListNode> map;

    private class ListNode{
        private final K key;
        private V value;
        private ListNode next;
        private ListNode prev;

        public ListNode(K key, V value) {
            this.key = key;
            this.value = value;
            next = null;
            prev = null;
        }

        public ListNode getNext() {
            return next;
        }

        public void setNext(ListNode node) {
            next = node;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public ListNode getPrev() {
            return prev;
        }

        public void setPrev(ListNode prev) {
            this.prev = prev;
        }

        public K getKey() {
            return key;
        }
    }

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head = null;
        tail = null;
        map = new HashMap<>();
    }

    synchronized public V get(K key){
        if(!map.containsKey(key)) return null;
        markRecentlyUsed(map.get(key));
        return map.get(key).getValue();
    }

    synchronized public void put(K key, V value){
        if(map.containsKey(key)){
            map.get(key).setValue(value);
            markRecentlyUsed(map.get(key));
        }
        else{
            ListNode node = new ListNode(key, value);
            if(map.size() == capacity){
                removeLeastRecent();
            }
            map.put(key, node);
            addToList(node);
        }
    }

    synchronized public void emptyCache(){
        head = null;
        tail = null;
        map = new HashMap<>();
    }

    private void addToList(ListNode node){
        if(head == null){
            head = node;
            tail = node;
        }
        else{
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
    }

    private void removeLeastRecent(){
        if(head == null) return;
        else if(head == tail){
            map.remove(head.getKey());
            head = null;
            tail = null;
        }
        else{
            ListNode temp = head.getNext();
            head.setNext(null);
            temp.setPrev(null);
            map.remove(head.getKey());
            head = temp;
        }
    }

    private void markRecentlyUsed(ListNode node){
        if(node == tail) return;
        else if(node == head){
            ListNode temp = head.getNext();
            head.setNext(null);
            temp.setPrev(null);
            head = temp;
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
        else{
            ListNode prev = node.getPrev();
            ListNode next = node.getNext();
            node.setNext(null);
            node.setPrev(null);
            prev.setNext(next);
            next.setPrev(prev);
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
    }
}
