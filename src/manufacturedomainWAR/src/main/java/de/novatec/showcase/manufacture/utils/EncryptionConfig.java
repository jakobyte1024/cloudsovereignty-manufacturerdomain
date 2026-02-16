package de.novatec.showcase.manufacture.utils;

public final class EncryptionConfig {

    private EncryptionConfig() {}

    public static String getKekProvider(Class<?> entityClass) {
        String env = System.getenv("KEK_PROVIDER_" + entityClass.getSimpleName().toUpperCase());
        if (env != null) {
            return env;
        }
        throw new IllegalStateException("KEK provider not configured for " + entityClass.getSimpleName());
    }

    public static String getKekKeyName(Class<?> entityClass) {
        String env = System.getenv("KEK_KEYNAME_" + entityClass.getSimpleName().toUpperCase());
        if (env != null) {
            return env;
        }
        throw new IllegalStateException("KEK key name not configured for " + entityClass.getSimpleName());
    }
}