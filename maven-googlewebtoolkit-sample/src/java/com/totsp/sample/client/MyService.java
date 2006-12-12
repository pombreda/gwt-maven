package com.totsp.sample.client;

import com.google.gwt.user.client.rpc.RemoteService;

import com.totsp.sample.client.exception.DataException;

import java.util.List;


/**
 * Simple GWT CLIENT side RPC service interface.
 *
 * Indicate that the return List is of type Entry using the gwt typeargs construct (no name means its the return type, not a param, we are denoting).
 * http://code.google.com/webtoolkit/documentation/com.google.gwt.doc.DeveloperGuide.RemoteProcedureCalls.SerializableTypes.html
 *
 * @gwt.typeArgs <com.totsp.sample.client.model.Entry>
 */
public interface MyService extends RemoteService {
    public List myMethod(String s) throws DataException;
}
