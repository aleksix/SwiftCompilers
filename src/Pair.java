public class Pair<K, V> {
    public K first;
    public V second;

    Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    Pair() {
        this(null, null);
    }
}
