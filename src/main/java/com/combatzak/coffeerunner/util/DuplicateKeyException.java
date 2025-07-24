package com.combatzak.coffeerunner.util;

/**
 * Thrown when attempting to an element to a collection would violate a unique key constraint
 */
public class DuplicateKeyException extends Exception {
    /**
     * Name of collection which had a constraint violation
     */
    private final String collectionName;

    /**
     * Reference to collection that had a constraint violation
     */
    private final Object collection;

    /**
     * Creates a new DuplicateKeyException object
     *
     * @param message Human readable message describing error
     * @param collectionName name of collection violating constraint
     * @param collection reference to collection violating constraint
     */
    public DuplicateKeyException(String message, String collectionName, Object collection) {
        super(message);

        this.collectionName = collectionName;
        this.collection = collection;
    }

    /**
     * Creates a new DuplicateKeyException object
     *
     * @param message Human readable message describing error
     */
    public DuplicateKeyException(String message) {
        this(message, null, null);
    }

    /**
     * Creates a new DuplicateKeyException object
     *
     * @param collectionName name of collection violating constraint
     * @param collection reference to collection violating constraint
     */
    public DuplicateKeyException(String collectionName, Object collection) {
        super();

        this.collectionName = collectionName;
        this.collection = collection;
    }

    /**
     * Default constructor
     */
    public DuplicateKeyException() {
        this(null, null);
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Object getCollection() {
        return collection;
    }
}
