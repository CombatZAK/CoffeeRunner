package com.combatzak.coffeerunner.util;

/**
 * Thrown when an object with an expected key is expected to be present in a collection and is not present.
 */
public class MissingKeyException extends Exception {
    private final Object key;
    private final Object collection;

    /**
     * Creates a new MissingKeyException instance
     *
     * @param message human-readable problem description
     * @param key key expected to be in collection
     * @param collection collection expected to contain key
     */
    public MissingKeyException(String message, Object key, Object collection) {
        super(message);

        this.key = key;
        this.collection = collection;
    }

    /**
     * Creates a new MissingKeyException instance
     *
     * @param message human-readable problem description
     */
    public MissingKeyException(String message) {
        this(message, null, null);
    }

    /**
     * Creates a new MissingKeyException instance
     *
     * @param key key expected to be in collection
     * @param collection collection expected to contain key
     */
    public MissingKeyException(Object key, Object collection) {
        super();

        this.key = key;
        this.collection = collection;
    }

    /**
     * Default constructor
     */
    public MissingKeyException() {
        this(null, null);
    }

    /**
     * Key that expected to be present in collection
     */
    public Object getKey() {
        return key;
    }

    /**
     * Collection that is expected to contain key
     */
    public Object getCollection() {
        return collection;
    }
}
