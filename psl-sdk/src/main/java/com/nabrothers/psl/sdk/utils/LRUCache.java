package com.nabrothers.psl.sdk.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private final int maxNum;

    public LRUCache(final int maxNum) {
        super();
        this.maxNum = maxNum;
    }

    public LRUCache(int initial, float loadFactor, final int maxNum, boolean accessMode) {
        super(initial, loadFactor, accessMode);
        this.maxNum = maxNum;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxNum;
    }

    public static <K, V> LRUCache<K, V> createDefault(int max) {
        return new LRUCache<>(max >> 1, 0.8f, max, true);
    }
}
