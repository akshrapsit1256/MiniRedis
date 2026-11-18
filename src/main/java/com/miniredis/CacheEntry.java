package com.miniredis;

public final class CacheEntry {

    private final String value;
    private final Long expirationTime;

    public CacheEntry(String value, Long expirationTime) {
        this.value = value;
        this.expirationTime = expirationTime;
    }

    public String getValue() {
        return value;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public boolean isExpired() {
        return expirationTime != null
                && System.currentTimeMillis() >= expirationTime;
    }

    public long remainingTTL() {
        if (expirationTime == null) {
            return -1;
        }

        long remaining = expirationTime - System.currentTimeMillis();

        if (remaining <= 0) {
            return -2;
        }

        return (remaining + 999) / 1000;
    }
    
}