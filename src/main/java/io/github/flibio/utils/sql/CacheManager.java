/*
 * This file is part of Utils, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */

package io.github.flibio.utils.sql;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class CacheManager<T, K> {

    private Logger logger;
    private Cache<T, K> cache;

    private CacheManager(Logger logger, long maxSize, long minutes) {
        this.logger = logger;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(minutes, TimeUnit.MINUTES)
                .build();
    }

    public static CacheManager create(Logger logger, long maxSize, long minutes) {
        return new CacheManager(logger, maxSize, minutes);
    }

    public void update(T key, K entry) {
        cache.put(key, entry);
    }

    public K getIfPresent(T key) {
        return cache.getIfPresent(key);
    }

    public void clear() {
        cache.invalidateAll();
    }
}
