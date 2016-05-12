package com.blanclink.geoindex;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiConsumer;

public class ExpiringMap<K, V> extends HashMap<K, V> {

    private final Queue<TimestampedValue> insertionOrder = new LinkedList<>();
    private final Duration expiration; // minutes
    private BiConsumer<K, V> onExpire;
    private final Map<K, Instant> lastInserted = new HashMap<>();

    public ExpiringMap(Duration expiration) {
        this.expiration = expiration;
    }

    private boolean hasExpired(Instant instant) {
        return instant.until(Instant.now(), ChronoUnit.MILLIS) > expiration.toMillis();
    }

    private void expire() {
        while (!insertionOrder.isEmpty()) {
            TimestampedValue<K, V> lastInserted = insertionOrder.peek();

            if (hasExpired(lastInserted.getTimestamp())) {
                insertionOrder.remove();

                if (hasExpired(this.lastInserted.get(lastInserted.getId()))) {
                    super.remove(lastInserted.getId());

                    if (onExpire != null) {
                        onExpire.accept(lastInserted.getId(), lastInserted.getValue());
                    }
                }
            } else {
                break;
            }
        }
    }

    @Override
    public V put(K key, V value) {
        expire();
        Instant insertionTime = Instant.now();
        lastInserted.put(key, insertionTime);
        insertionOrder.add(new TimestampedValue<>(key, value, insertionTime));
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        expire();
        lastInserted.remove(key);
        return super.remove(key);
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public V get(Object key) {
        expire();
        return super.get(key);
    }

    @Override
    public int size() {
        expire();
        return super.size();
    }

    public void setOnExpire(BiConsumer<K, V> onExpire) {
        this.onExpire = onExpire;
    }

}