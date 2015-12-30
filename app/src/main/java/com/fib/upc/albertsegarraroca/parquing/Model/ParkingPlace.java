package com.fib.upc.albertsegarraroca.parquing.Model;

import java.util.Date;

/**
 * Created by albert on 26/12/15.
 */
public class ParkingPlace {
    private final String id;
    private Vehicle vehicle;
    private boolean isActive;
    private Date lastEntranceDate;

    public ParkingPlace(String id, Vehicle vehicle, Date lastEntranceDate, boolean isActive) {
        if (id == null) throw new NullPointerException();
        if (id.isEmpty()) throw new IllegalArgumentException();

        this.id = id.toUpperCase();
        this.vehicle = vehicle;
        this.lastEntranceDate = lastEntranceDate;
        this.isActive = isActive;
    }

    public ParkingPlace(String id, boolean isActive) {
        this(id, null, null, isActive);
    }

    public ParkingPlace(String id) {
        this(id, true);
    }

    public void occupy(Vehicle vehicle, Date date) {
        if (!isFree()) throw new IllegalStateException();
        if (vehicle == null || date == null) throw new NullPointerException();

        this.vehicle = vehicle;
        this.lastEntranceDate = date;
    }

    public void free() {
        if (!isOccupied()) throw new IllegalStateException();

        this.vehicle = null;
        this.lastEntranceDate = null;
    }

    public boolean isOccupied() {
        return this.vehicle != null;
    }

    public boolean isFree() {
        return !isOccupied() && isActive;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public Vehicle getOcuppyingVehicle() {
        return this.vehicle;
    }

    public String getId() {
        return this.id;
    }

    public Date getLastEntranceDate() {
        return this.lastEntranceDate;
    }

    @Override
    public String toString() {
        return "ParkingPlace{" +
                "id='" + id + '\'' +
                ", vehicle=" + vehicle +
                ", isActive=" + isActive +
                ", lastEntranceDate=" + lastEntranceDate +
                '}';
    }
}
