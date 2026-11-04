package org.route;

/** Thrown for every business-rule failure (empty cart, out of stock, ...). */
public class StoreException extends RuntimeException {

    public StoreException(String message) {
        super(message);
    }
}
