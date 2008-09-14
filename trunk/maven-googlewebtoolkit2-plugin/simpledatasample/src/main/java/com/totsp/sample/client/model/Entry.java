package com.totsp.sample.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * Super simple Entry object with two String members, just to demonstrate "IsSerializable."
 *
 * @author ccollins
 *
 */
public class Entry implements IsSerializable {
    public String name;
    public String time;
}
