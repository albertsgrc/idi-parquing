package com.fib.upc.albertsegarraroca.parquing.Model;

/**
 * Created by albert on 26/12/15.
 */
public class Parking {
    private static Parking ourInstance = new Parking();

    public static Parking getInstance() {
        return ourInstance;
    }

    private Parking() {
    }
}