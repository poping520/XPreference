package com.poping520.open.xpreference.datastore;

/**
 * Created by WangKZ on 18/09/15.
 *
 * @author poping520
 * @version 1.0.0
 */
class StorageException extends RuntimeException {

    StorageException(String message) {
        super(message);
    }

    StorageException(Throwable cause) {
        super(cause);
    }
}
