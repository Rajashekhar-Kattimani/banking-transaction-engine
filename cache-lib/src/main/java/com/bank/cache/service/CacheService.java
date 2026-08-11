package com.bank.cache.service;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {

    <T> void put(String key, T value);

    <T> void put(String key, T value, Duration ttl);

    <T> Optional<T> get(String key, Class<T> type);

    void evict(String key);

    boolean exists(String key);

    void expire(String key, Duration ttl);
}