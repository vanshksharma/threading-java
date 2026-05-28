package cache;

public class Main {
    public static void main(String[] args) {
        LRUCache<Integer, String> cache = new LRUCache<>(5);
        System.out.println(cache.get(1));
        cache.put(1, "ABC");
        cache.put(2, "EFG");
        cache.put(3, "PQR");
        cache.put(4, "XYZ");
        cache.put(5, "DEF");
        System.out.println(cache.get(4));
        System.out.println(cache.get(1));
        cache.put(6, "MNO");
        System.out.println(cache.get(2));
        System.out.println(cache.get(6));

    }
}
