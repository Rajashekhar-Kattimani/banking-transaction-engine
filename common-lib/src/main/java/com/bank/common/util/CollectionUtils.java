package com.bank.common.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static <T> List<T> nullSafeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public static <T> Set<T> nullSafeSet(Set<T> set) {
        return set == null ? Collections.emptySet() : set;
    }

    public static <K, V> Map<K, V> nullSafeMap(Map<K, V> map) {
        return map == null ? Collections.emptyMap() : map;
    }

    public static <T> List<T> immutableList(List<T> list) {
        return List.copyOf(nullSafeList(list));
    }

    public static <T> Set<T> immutableSet(Set<T> set) {
        return Set.copyOf(nullSafeSet(set));
    }

    public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
        return Map.copyOf(nullSafeMap(map));
    }
}