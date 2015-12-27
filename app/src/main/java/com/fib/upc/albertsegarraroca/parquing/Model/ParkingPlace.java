package com.fib.upc.albertsegarraroca.parquing.Model;

/**
 * Created by albert on 26/12/15.
 */
public class ParkingPlace {
    private final String id;
    private Vehicle vehicle;

    public ParkingPlace(String id) {
        if (id == null) throw new NullPointerException();
        if (id.isEmpty()) throw new IllegalArgumentException();

        this.id = id;
        this.vehicle = null;
    }

    public void occupy(Vehicle vehicle) {
        if (isOccupied()) throw new IllegalStateException();
        if (vehicle == null) throw new NullPointerException();

        this.vehicle = vehicle;
    }

    public boolean isOccupied() {
        return this.vehicle == null;
    }

    public boolean isFree() {
        return !isOccupied();
    }

    public Vehicle getOcuppyingVehicle() {
        return this.vehicle;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "ParkingPlace{" +
                "id='" + id + '\'' +
                ", vehicle=" + vehicle +
                '}';
    }
}
