package com.fib.upc.albertsegarraroca.parquing.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fib.upc.albertsegarraroca.parquing.Model.Parking;
import com.fib.upc.albertsegarraroca.parquing.Model.ParkingPlace;
import com.fib.upc.albertsegarraroca.parquing.Model.Utils;
import com.fib.upc.albertsegarraroca.parquing.Model.Vehicle;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivity;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleEntry;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleExit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by albert on 10/02/15.
 */
public class Database extends SQLiteOpenHelper {

    private Context context;
    private static int BD_VERSION = 1;
    private static String BD_NAME = "ParkingDB";
    private static String VEHICLE_ACTIVITIES_TABLE = "vehicleactivities";
    private static String PARKING_PLACES_TABLE = "parkingplaces";

    private static String CREATE_ACTIVITIES_QUERY = "CREATE TABLE " + VEHICLE_ACTIVITIES_TABLE + " (" +
            "date TEXT PRIMARY KEY," +
            "income REAL," +
            "vehicle TEXT," +
            "place TEXT REFERENCES " + PARKING_PLACES_TABLE + "(id)," +
            "associatedActivity TEXT REFERENCES " + VEHICLE_ACTIVITIES_TABLE + "(date)" + ")";

    private static String CREATE_PLACES_QUERY = "CREATE TABLE " + PARKING_PLACES_TABLE + " (" +
            "id TEXT PRIMARY KEY," +
            "vehicle TEXT DEFAULT NULL," +
            "isActive INTEGER NOT NULL DEFAULT 1," + // Boolean
            "lastEntranceDate TEXT DEFAULT NULL" + ")";

    private static String CREATE_PLACES_INDEX_QUERY = "CREATE UNIQUE INDEX " +
            "id_UNIQUE" + " ON " + VEHICLE_ACTIVITIES_TABLE + "(date DESC)";

    public Database(Context con) {
        super(con, BD_NAME, null, BD_VERSION);
        context = con;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_ACTIVITIES_QUERY);
        sqLiteDatabase.execSQL(CREATE_PLACES_QUERY);
        sqLiteDatabase.execSQL(CREATE_PLACES_INDEX_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + VEHICLE_ACTIVITIES_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PARKING_PLACES_TABLE);
        sqLiteDatabase.execSQL(CREATE_ACTIVITIES_QUERY);
        sqLiteDatabase.execSQL(CREATE_PLACES_QUERY);
        sqLiteDatabase.execSQL(CREATE_PLACES_INDEX_QUERY);
    }

    private Cursor read(String query) {
        return this.getReadableDatabase().rawQuery(query, null);
    }

    private long insert(String table, ContentValues values) {
        return this.getWritableDatabase().insert(table, null, values);
    }

    private int update(String table, ContentValues values, String whereClause) {
        return this.getWritableDatabase().update(table, values, whereClause, null);
    }

    private int delete(String table, String whereClause) {
        return this.getWritableDatabase().delete(table, whereClause, null);
    }

    private void endBad() throws IOException {
        this.getWritableDatabase().endTransaction();
        throw new IOException();
    }

    private void endGood() {
        this.getWritableDatabase().setTransactionSuccessful();
        this.getWritableDatabase().endTransaction();
    }

    private void start() {
        this.getWritableDatabase().beginTransaction();
    }

    public void occupyPlace(String placeId, String occupyingVehicleRegistration, String date) throws IOException {
        ContentValues valuesPlace = new ContentValues();
        valuesPlace.put("vehicle", occupyingVehicleRegistration);
        valuesPlace.put("lastEntranceDate", date);

        start();

        if (update(PARKING_PLACES_TABLE, valuesPlace, "id = '" + placeId + "'") != 1) endBad();

        ContentValues valuesActivity = new ContentValues();
        valuesActivity.put("date", date);
        valuesActivity.put("vehicle", occupyingVehicleRegistration);
        valuesActivity.put("place", placeId);

        if (insert(VEHICLE_ACTIVITIES_TABLE, valuesActivity) < 0) endBad();
        else endGood();
    }

    public VehicleExit freePlace(String placeId, String date, double income, String associatedEntryDate) throws IOException {
        ContentValues valuesPlace = new ContentValues();
        valuesPlace.put("vehicle", (String) null);
        valuesPlace.put("lastEntranceDate", (String) null);

        start();

        if (update(PARKING_PLACES_TABLE, valuesPlace, "id = '" + placeId + "'") != 1) endBad();

        ContentValues valuesActivityEntry = new ContentValues();
        valuesActivityEntry.put("associatedActivity", date);
        if (update(VEHICLE_ACTIVITIES_TABLE, valuesActivityEntry, "date = '" + associatedEntryDate + "'") < 0) endBad();

        ContentValues valuesActivity = new ContentValues();
        valuesActivity.put("date", date);
        valuesActivity.put("income", income);
        valuesActivity.put("associatedActivity", associatedEntryDate);

        // Falta actualitzar la d'entrada

        if (insert(VEHICLE_ACTIVITIES_TABLE, valuesActivity) < 0) endBad();
        else endGood();

        return (VehicleExit) getActivitiesBetween(date, date).list.get(0);
    }

    public void deactivatePlace(String placeId) {
        ContentValues values = new ContentValues();
        values.put("isActive", false);
        update(PARKING_PLACES_TABLE, values, "id = '" + placeId + "'");
    }

    public void activatePlace(String placeId) {
        ContentValues values = new ContentValues();
        values.put("isActive", true);
        update(PARKING_PLACES_TABLE, values, "id = '" + placeId + "'");
    }

    public void resetParking(ArrayList<String> ids) throws IOException {
        delete(PARKING_PLACES_TABLE, null);
        delete(VEHICLE_ACTIVITIES_TABLE, null);

        ContentValues values = new ContentValues();

        start();

        for (String id : ids)  {
            values.put("id", id);
            if (insert(PARKING_PLACES_TABLE, values) < 0) endBad();
        }

        endGood();
    }

    public ArrayList<ParkingPlace> getPlaces() {
        Cursor c = read("SELECT * FROM " + PARKING_PLACES_TABLE);
        ArrayList<ParkingPlace> places = new ArrayList<ParkingPlace>();

        while (c.moveToNext()) {
            String vehicle = c.getString(c.getColumnIndex("vehicle"));
            String lastEntryDate = c.getString(c.getColumnIndex("lastEntranceDate"));
            places.add(new ParkingPlace(c.getString(0),
                    vehicle == null ? null : new Vehicle(vehicle),
                    lastEntryDate == null ? null : Utils.dbStringToDate(lastEntryDate),
                    c.getInt(2) != 0));
        }

        c.close();

        return places;
    }

    public ParkingPlace getPlace(String id) {
        Cursor c = read("SELECT * FROM " + PARKING_PLACES_TABLE + " WHERE id = '" + id + "'");

        boolean exists = c.moveToFirst();

        if (!exists) return null;

        String vehicle = c.getString(c.getColumnIndex("vehicle"));
        String date = c.getString(c.getColumnIndex("lastEntranceDate"));

        return new ParkingPlace(c.getString(c.getColumnIndex("id")),
                vehicle != null ? new Vehicle(vehicle) : null,
                date != null ? Utils.dbStringToDate(date) : null,
                c.getInt(c.getColumnIndex("isActive")) != 0);
    }

    public VehicleActivity getLastActivity() {
        Cursor cLast = read("SELECT MAX(date) AS lastDate FROM " + VEHICLE_ACTIVITIES_TABLE);

        if (!cLast.moveToFirst()) return null;

        String lastActivityDate = cLast.getString(cLast.getColumnIndex("lastDate"));

        Cursor c = read("SELECT a.*, o.date AS odate, o.vehicle AS ovehicle, o.place AS oplace" +
                " FROM " + VEHICLE_ACTIVITIES_TABLE + " a LEFT JOIN " +
                VEHICLE_ACTIVITIES_TABLE + " o ON a.associatedActivity = o.date WHERE a.date = '" + lastActivityDate + "'");

        if (!c.moveToFirst()) return null;

        String vehicle = c.getString(c.getColumnIndex("vehicle"));

        VehicleActivity va;
        if (vehicle != null) { // is entry
            va = new VehicleEntry(Utils.dbStringToDate(lastActivityDate), new Vehicle(vehicle),
                    c.getString(c.getColumnIndex("place")));

        } else { // is exit
            String entryDateString = c.getString(c.getColumnIndex("odate"));
            Date entryDate = Utils.dbStringToDate(entryDateString);
            String vehicleIString = c.getString(c.getColumnIndex("ovehicle"));
            Vehicle vehicleI = new Vehicle(vehicleIString);
            String placeId = c.getString(c.getColumnIndex("oplace"));
            VehicleEntry associatedEntry = new VehicleEntry(entryDate, vehicleI, placeId);
            va = new VehicleExit(Utils.dbStringToDate(lastActivityDate), associatedEntry);
        }

        return va;
    }

        public VehicleActivity undoLastActivity(boolean force) throws IllegalStateException, IOException {
        Cursor cLast = read("SELECT MAX(date) AS lastDate FROM " + VEHICLE_ACTIVITIES_TABLE);

        if (!cLast.moveToFirst()) return null;

        String lastActivityDate = cLast.getString(cLast.getColumnIndex("lastDate"));

        Cursor c = read("SELECT a.*, o.date AS odate, o.vehicle AS ovehicle, o.place AS oplace" +
                        " FROM " + VEHICLE_ACTIVITIES_TABLE + " a LEFT JOIN " +
                    VEHICLE_ACTIVITIES_TABLE + " o ON a.associatedActivity = o.date WHERE a.date = '" + lastActivityDate + "'");

        if (!c.moveToFirst()) return null;

        String vehicle = c.getString(c.getColumnIndex("vehicle"));

        VehicleActivity va;
        if (vehicle != null) { // is entry
            va = new VehicleEntry(Utils.dbStringToDate(lastActivityDate), new Vehicle(vehicle),
                    c.getString(c.getColumnIndex("place")));

            start();

            if (delete(VEHICLE_ACTIVITIES_TABLE, "date = '" + lastActivityDate + "'") != 1) endBad();

            ContentValues valuesPlace = new ContentValues();
            valuesPlace.put("vehicle", (String) null);
            valuesPlace.put("lastEntranceDate", (String) null);

            if (update(PARKING_PLACES_TABLE, valuesPlace, "id = '" + va.getPlace() + "'") != 1) endBad();
            else endGood();
        }
        else { // is exit
            String entryDateString = c.getString(c.getColumnIndex("odate"));
            Date entryDate = Utils.dbStringToDate(entryDateString);
            String vehicleIString = c.getString(c.getColumnIndex("ovehicle"));
            Vehicle vehicleI = new Vehicle(vehicleIString);
            String placeId = c.getString(c.getColumnIndex("oplace"));
            VehicleEntry associatedEntry = new VehicleEntry(entryDate, vehicleI, placeId);
            va = new VehicleExit(Utils.dbStringToDate(lastActivityDate), associatedEntry);

            ParkingPlace place = getPlace(placeId);

            // Place is not active, cannot occupy
            if (!place.isActive()) {
                if (!force) {
                    cLast.close();
                    c.close();
                    throw new IllegalStateException();
                }
                else activatePlace(place.getId());
            }

            start();

            // Delete exit activity
            if (delete(VEHICLE_ACTIVITIES_TABLE, "date = '" + lastActivityDate + "'") != 1) endBad();

            ContentValues values = new ContentValues();
            values.put("associatedActivity", (String) null);

            // Set associatedExit of the entry to null
            if (update(VEHICLE_ACTIVITIES_TABLE,values,  "date = '" + entryDateString + "'") != 1) endBad();

            ContentValues valuesPlace = new ContentValues();
            valuesPlace.put("vehicle", vehicleIString);
            valuesPlace.put("lastEntranceDate", entryDateString);

            // Set parking place to occupied by the entry
            if (update(PARKING_PLACES_TABLE, valuesPlace, "id = '" + placeId + "'") != 1) endBad();
            else endGood();
        }

        cLast.close();
        c.close();

        return va;
    }

    public class ActivityListInfo {
        public double totalIncome;
        public int entries;
        public int exits;
        public ArrayList<VehicleActivity> list;
        public ActivityListInfo(double totalIncome, int entries, int exits, ArrayList<VehicleActivity> list) {
            this.totalIncome = totalIncome;
            this.entries = entries;
            this.exits = exits;
            this.list = list;
        }
    }

    public ActivityListInfo getActivitiesBetween(String beggining, String end) {
        Cursor c = read(
                "SELECT a.*, o.date AS odate, o.vehicle AS ovehicle, o.place AS oplace, o.income AS oincome" +
                " FROM " + VEHICLE_ACTIVITIES_TABLE + " a" +
                " LEFT JOIN " + VEHICLE_ACTIVITIES_TABLE + " o ON a.associatedActivity = o.date" +
                (beggining != null ? " WHERE a.date >= '" + beggining + "' AND a.date <= '" + end + "'" : "") +
                " ORDER BY a.date DESC");

        ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();

        int entries = 0;
        int exits = 0;
        double totalIncome = 0.0;

        while (c.moveToNext()) {
            String vehicle = c.getString(c.getColumnIndex("vehicle"));
            String date = c.getString(c.getColumnIndex("date"));

            if (vehicle != null) { // is entry
                ++entries;
                String place = c.getString(c.getColumnIndex("place"));
                double oincome = c.getDouble(c.getColumnIndex("oincome"));
                String odate = c.getString(c.getColumnIndex("odate"));

                VehicleEntry entry = new VehicleEntry(Utils.dbStringToDate(date), new Vehicle(vehicle), place);

                if (odate != null) {
                    VehicleExit exit = new VehicleExit(Utils.dbStringToDate(odate), entry, oincome);
                    entry.setAssociatedExit(exit);
                }
                activities.add(entry);

            }
            else {
                ++exits;
                Date otherDate = Utils.dbStringToDate(c.getString(c.getColumnIndex("odate")));
                String ovehicle = c.getString(c.getColumnIndex("ovehicle"));
                String oplace = c.getString(c.getColumnIndex("oplace"));

                VehicleEntry otherEntry = new VehicleEntry(otherDate, new Vehicle(ovehicle), oplace);
                double income = c.getDouble(c.getColumnIndex("income"));
                totalIncome += income;

                VehicleExit ve = new VehicleExit(Utils.dbStringToDate(date), otherEntry, income);
                ve.getAssociatedEntry().setAssociatedExit(ve);
                activities.add(ve);
            }
        }

        c.close();

        return new ActivityListInfo(Utils.roundEuros(totalIncome), entries, exits, activities);
    }

    public ActivityListInfo getAllActivities() {
        return getActivitiesBetween(null, null);
    }
}
