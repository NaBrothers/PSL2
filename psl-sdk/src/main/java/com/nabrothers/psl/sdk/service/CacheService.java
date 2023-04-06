package com.nabrothers.psl.sdk.service;

import java.util.Map;

public interface CacheService {
    void put(String category, String key, String value);

    String get(String category, String key);

    String get(String category, String key, String defaultValue);

    void delete(String category, String key);

    Map<String, String> getAll(String category);
}
