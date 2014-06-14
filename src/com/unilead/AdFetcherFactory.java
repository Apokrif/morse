package com.unilead;


public class AdFetcherFactory {
    protected static AdFetcherFactory instance = new AdFetcherFactory();

    @Deprecated // for testing
    public static void setInstance(AdFetcherFactory factory) {
        instance = factory;
    }

    public static AdFetcher create(Engine adViewController, String userAgent) {
        return instance.internalCreate(adViewController, userAgent);
    }

    protected AdFetcher internalCreate(Engine adViewController, String userAgent) {
        return new AdFetcher(adViewController, userAgent);
    }
}
