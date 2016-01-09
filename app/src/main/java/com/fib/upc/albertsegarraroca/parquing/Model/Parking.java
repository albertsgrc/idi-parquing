package com.fib.upc.albertsegarraroca.parquing.Model;

import com.fib.upc.albertsegarraroca.parquing.Data.Database;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by albert on 26/12/15.
 */
public class Parking {
    private static Parking ourInstance = new Parking();

    public static Parking getInstance() {
        return ourInstance;
    }

    private Parking() {}

    private ArrayList<ParkingPlace> places;

    private Database db;

    private static final int PLACES_PER_SECTION = 4;
    private static final char FIRST_SECTION_CHAR = 'A';

    private static final int SIZE = 16;

    public void init(Database db) {
        this.places = db.getPlaces();
        this.db = db;

        if (this.places.size() != SIZE) reset();
    }

    public void reset() {
        this.places = new ArrayList<ParkingPlace>(SIZE);

        ArrayList<String> ids = new ArrayList<String>();

        for (int i = 0; i < SIZE; ++i) {
            this.places.add(new ParkingPlace(getPlaceId(i)));
            ids.add(getPlaceId(i));
        }

        db.resetParking(ids);
    }

    // Throws if an exit is undone and the corresponding place has been marked unactive
    private VehicleActivity undoLastActivity(boolean force) throws IllegalStateException {
        VehicleActivity va = db.undoLastActivity(force);


        if (va == null) return null;

        if (force && va.getClass() == VehicleExit.class && !getPlace(va.getPlace()).isActive())
            activatePlace(va.getPlace());

        int index = getPlaceIndex(va.getPlace());

        if (va.getClass() == VehicleEntry.class) {
            places.get(index).free();
        }
        else {
            Assert.assertEquals(places.get(index).isActive(), true);
            places.set(index, new ParkingPlace(va.getPlace(), va.getVehicle(), ((VehicleExit) va).getEntryDate(), true));
        }

        return va;
    }

    public VehicleActivity getLastActivity() { return db.getLastActivity(); }

    public VehicleActivity undoLastActivity() throws IllegalStateException {
        return undoLastActivity(false);
    }

    public VehicleActivity undoLastActivityForced() {
        return undoLastActivity(true);
    }

    private int getPlaceIndex(String id) {
        int section = id.charAt(0) - FIRST_SECTION_CHAR;
        int number = Integer.valueOf(id.substring(1));

        return section*PLACES_PER_SECTION + number - 1;
    }

    private String getPlaceId(int index) {
        char section = (char) (index/PLACES_PER_SECTION + FIRST_SECTION_CHAR);
        Integer number = index%PLACES_PER_SECTION + 1;

        return section + number.toString();
    }

    public ParkingPlace getPlace(String id) {
        return places.get(getPlaceIndex(id));
    }

    public ParkingPlace getFreePlace() {
        for (ParkingPlace p : places)
            if (p.isFree()) return p;
        return null;
    }

    public boolean isFull() {
        return getFreePlace() == null;
    }

    public ArrayList<Vehicle> getVehicles() {
        ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();

        for (ParkingPlace p : places)
            if (p.isOccupied()) vehicles.add(p.getOcuppyingVehicle());

        return vehicles;
    }

    public ArrayList<ParkingPlace> getPlaces() {
        return this.places;
    }

    public void activatePlace(String placeId) {
        db.activatePlace(placeId);
        this.places.get(getPlaceIndex(placeId)).activate();
    }

    public boolean deactivatePlace(String placeId) {
        ParkingPlace place = this.places.get(getPlaceIndex(placeId));

        if (place.isOccupied()) return false;

        db.deactivatePlace(placeId);
        place.deactivate();

        return true;
    }

    // Returns null if vehicle is inside
    // Returns its new place otherwise
    public ParkingPlace enterVehicle(Vehicle vehicle) {
        if (vehicle == null) throw new NullPointerException();

        if (isInside(vehicle)) return null;

        Date entryDate = new Date();

        ParkingPlace freePlace = getFreePlace();

        if (freePlace == null) throw new IllegalStateException();

        db.occupyPlace(freePlace.getId(), vehicle.getRegistration(), Utils.dateToDBString(entryDate));
        freePlace.occupy(vehicle, entryDate);

        return freePlace;
    }

    public int countActivePlaces() {
        int c = 0;
        for (ParkingPlace p : places)
            if (p.isActive()) ++c;
        return c;
    }

    // Throws if now is before the associated entry's date
    public VehicleExit exitVehicle(Vehicle vehicle) throws IllegalStateException {
        if (vehicle == null) throw new NullPointerException();
        Date exitDate = new Date();

        ParkingPlace vehiclePlace = getVehiclePlace(vehicle);

        if (vehiclePlace == null) return null;

        Date associatedEntryDate = vehiclePlace.getLastEntranceDate();

        Assert.assertNotNull(associatedEntryDate);

        if (associatedEntryDate.after(exitDate)) throw new IllegalStateException();

        double income = VehicleExit.calculateIncome(associatedEntryDate, exitDate);

        VehicleExit exit = db.freePlace(vehiclePlace.getId(), Utils.dateToDBString(exitDate), income,
                     Utils.dateToDBString(associatedEntryDate));

        vehiclePlace.free();

        return exit;
    }

    public ParkingPlace getVehiclePlace(Vehicle vehicle) {
        if (vehicle == null) throw new NullPointerException();

        for (ParkingPlace place : places)
            if (vehicle.equals(place.getOcuppyingVehicle())) return place;

        return null;
    }

    public boolean isInside(Vehicle vehicle) {
        return getVehiclePlace(vehicle) != null;
    }
}