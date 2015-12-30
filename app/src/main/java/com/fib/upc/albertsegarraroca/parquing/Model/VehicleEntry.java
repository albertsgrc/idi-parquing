package com.fib.upc.albertsegarraroca.parquing.Model;

import java.util.Date;

/**
 * Created by albert on 26/12/15.
 */
public class VehicleEntry extends VehicleActivity {
    private final Vehicle vehicle;
    private final String place;
    private VehicleExit associatedExit;

    public VehicleEntry(Date date, Vehicle vehicle, String place, VehicleExit associatedExit) {
        super(date);

        if (vehicle == null || place == null) throw new NullPointerException();
        if (associatedExit != null && associatedExit.getAssociatedEntry() != this)
            throw new IllegalArgumentException();

        this.vehicle = vehicle;
        this.place = place;
        this.associatedExit = associatedExit;
    }

    public VehicleEntry(Date date, Vehicle vehicle, String place) {
        this(date, vehicle, place, null);
    }

    @Override
    public Vehicle getVehicle() {
        return vehicle;
    }

    @Override
    public String getPlace() {
        return place;
    }

    public void setAssociatedExit(VehicleExit v) {
        this.associatedExit = v;
    }

    public Date getExitDate() {
        return this.associatedExit.getDate();
    }

    public VehicleExit getAssociatedExit() {
        return associatedExit;
    }

    /*
    public void finish(VehicleExit associatedExit) {
        if (associatedExit == null) throw new NullPointerException();
        if (hasFinished()) throw new IllegalStateException();
        if (associatedExit.getAssociatedEntry() != this) throw new IllegalArgumentException();

        this.associatedExit = associatedExit;
    }*/

    /*
    public void restart() {
        if (!hasFinished()) throw new IllegalStateException();

        this.associatedExit = null;
    }*/

    public boolean hasFinished() {
        return associatedExit != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VehicleEntry that = (VehicleEntry) o;

        if (!place.equals(that.place)) return false;
        if (!vehicle.equals(that.vehicle)) return false;
        if (!getAssociatedExit().equals(associatedExit)) return false;
        if (!getDate().equals(that.getDate())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = vehicle.hashCode();
        result = 31 * result + place.hashCode();
        result = 31 * result + getExitDate().hashCode();
        result = 31 * result * getDate().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "VehicleEntry{" +
                "vehicle=" + vehicle +
                ", place=" + place +
                ", date=" + getDate() +
                ", associatedExit=" + associatedExit +
                '}';
    }
}
