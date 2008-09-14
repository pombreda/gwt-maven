package com.totsp.sample.client.model;

import java.io.Serializable;


/**
 * Super simple Entry object with two String members, just to demonstrate "IsSerializable."
 * 
 * Uses Serializable (not IsSerializable) so needs the rpc types definition file the compiler creates. 
 *
 * @author ccollins
 *
 */
public class Entry implements Serializable {
    public String name;
    public String time;
}
