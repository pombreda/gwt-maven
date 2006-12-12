package com.totsp.sample.client.exception;

import com.google.gwt.user.client.rpc.SerializableException;


/**
 * Exception that can be thrown across the wire.
 *
 * @author ccollins
 *
 */
public class DataException extends SerializableException {
    public DataException() {
    }

    public DataException(String message) {
        super(message);
    }
}
