package io.github.upinmcse.common.cache;

public final class CacheKeys {

    /** Prefix used for every cache entry; bumping it is a cheap way to flush all keys. */
    public static final String NAMESPACE = "v1";

    private CacheKeys(){}

    public static String itemById(long id){
        return NAMESPACE + ":item" + id;
    }
}
