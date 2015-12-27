package com.fib.upc.albertsegarraroca.parquing.Model;

import java.util.Date;

/**
 * Created by albert on 26/12/15.
 */
public abstract class VehicleActivity {
    private final Date date;

    public VehicleActivity(Date date) {
        if (date == null) throw new NullPointerException();

        this.date = date;
    }

    public Date getDate() {
        return this.date;
    }

    public abstract Vehicle getVehicle();

    public abstract ParkingPlace getPlace();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
