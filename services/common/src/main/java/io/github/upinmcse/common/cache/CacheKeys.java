package io.github.upinmcse.common.cache;

public final class CacheKeys {

    public static final String NAMESPACE = "v1";

    private CacheKeys(){}

    public static String itemById(long id){
        return NAMESPACE + ":item" + id;
    }
}
