package bluedot.electrochemistry.cache.local;


import bluedot.electrochemistry.cache.entity.FileData;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Senn
 * @create 2022/2/4 20:44
 */
public class FileDataCache {

    private static volatile FileDataCache fileDataCache;

    private static LoadingCache<String, FileData> CACHE;

    public static FileDataCache getInstance() {
        if (fileDataCache == null) {
           throw new RuntimeException("file cache not init...");
        }
        return fileDataCache;
    }

    public static void init(CacheLoader<String, FileData> cacheLoader) {
        CACHE = CacheBuilder
                .newBuilder()
                .softValues()
                .initialCapacity(100)
                .maximumSize(500)
                .recordStats()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build(cacheLoader);
        fileDataCache = new FileDataCache();
    }


    private FileDataCache() {
    }

    public FileData get(String key) throws ExecutionException {
        return CACHE.get(key);
    }

    public FileData getIfPresent(String key) {
        return CACHE.getIfPresent(key);
    }

    public void put(String key , FileData fileData) {
        CACHE.put(key, fileData);
    }

    public long size() {
        return CACHE.size();
    }

    public void invalidate(String key) {
        CACHE.invalidate(key);
    }

    public void invalidateAll() {
        CACHE.invalidateAll();
    }

    public void stats() {
        CACHE.stats();
    }
}
