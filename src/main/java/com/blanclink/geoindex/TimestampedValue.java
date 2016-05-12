package com.blanclink.geoindex;

import java.time.Instant;

public class TimestampedValue<K, V> {

    private final K id;
    private final V value;
    private final Instant timestamp;


    public TimestampedValue(K id, V value, Instant timestamp) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
    }

    public K getId() {
        return id;
    }

    public V getValue() {
        return value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
