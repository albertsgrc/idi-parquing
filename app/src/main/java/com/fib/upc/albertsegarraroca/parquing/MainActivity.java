package com.fib.upc.albertsegarraroca.parquing;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.fib.upc.albertsegarraroca.parquing.Data.Database;
import com.fib.upc.albertsegarraroca.parquing.Data.Files;
import com.fib.upc.albertsegarraroca.parquing.Data.Preferences;
import com.fib.upc.albertsegarraroca.parquing.Model.Parking;
import com.fib.upc.albertsegarraroca.parquing.Model.ParkingPlace;
import com.fib.upc.albertsegarraroca.parquing.Model.Vehicle;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivity;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivityList;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Preferences.getInstance().init(getApplicationContext());

        Database db = new Database(getApplicationContext());

        Parking parking = Parking.getInstance();
        parking.init(db);

        parking.enterVehicle(new Vehicle("2372882"));

        boolean b = parking.getVehicles().get(0).seemsValidRegistration();

        parking.deactivatePlace("B1");

        ParkingPlace newPlace = parking.enterVehicle(new Vehicle("2372882"));

        parking.deactivatePlace("C1");
        parking.activatePlace("B1");

        boolean inside = parking.isInside(new Vehicle("2372882"));

        try {
            parking.undoLastActivity();
        } catch (IllegalStateException e) {
            Log.e("undo", "There was exception while undoing");
        }


        ParkingPlace occupyingPlace = parking.enterVehicle(new Vehicle("2734A23"));

        boolean b3 = parking.getVehicles().get(0).seemsValidRegistration();

        boolean b4 = parking.deactivatePlace(occupyingPlace.getId());

        parking.exitVehicle(new Vehicle("2734A23"));

        boolean b5 = parking.deactivatePlace(occupyingPlace.getId());

        try {
            parking.undoLastActivity();
        } catch (IllegalStateException e) {
            Log.e("undo", "Cannot undo");
        }

        parking.activatePlace(occupyingPlace.getId());

        parking.undoLastActivity();

        parking.exitVehicle(parking.getVehicles().get(0));

        parking.enterVehicle(new Vehicle("fd"));
        parking.enterVehicle(new Vehicle("dhawio"));
        parking.exitVehicle(new Vehicle("dhawio"));

        VehicleActivityList va = new VehicleActivityList(db);

        try {
            Files.getInstance().init();
        } catch (IOException e) {
            Log.e("FIles", "Error while creating folders");
        }

        try {
            Files.getInstance().saveDocument("lamevallista.csv", va.toCsv("Tipus;Plaça;Data;Matrícula;Benefici"));
            Files.getInstance().saveDocument("lamevallistaagrupada", va.toCsvGrouped("Plaça;Data d'entrada;Data de sortida;Matrícula;Benefici"));
            Files.getInstance().saveDocument("elmeuhtml.html", va.toHtml("Mes;Benefici;Entrades;Sortides;Temps mitjà d'estada", "Resum", "table {\n" +
                    "            font-family: Arial;\n" +
                    "            border: 1px solid #999;\n" +
                    "            empty-cells: show;\n" +
                    "            border-collapse: collapse;\n" +
                    "            border-spacing: 0;\n" +
                    "        }\n" +
                    "\n" +
                    "        table td,\n" +
                    "        table th {\n" +
                    "            border-left: 1px solid #999;\n" +
                    "            border-width: 0 0 0 1px;\n" +
                    "            font-size: inherit;\n" +
                    "            margin: 0;\n" +
                    "            overflow: visible;\n" +
                    "            padding: 0.5em 1em;\n" +
                    "        }\n" +
                    "\n" +
                    "        table td {\n" +
                    "            background-color: white;\n" +
                    "        }\n" +
                    "\n" +
                    "        tbody tr td:first-child {\n" +
                    "            font-weight: bold;\n" +
                    "        }\n" +
                    "\n" +
                    "        thead {\n" +
                    "            background-color: #ffaa00;\n" +
                    "            color: black;\n" +
                    "            text-align: left;\n" +
                    "        }\n" +
                    "\n" +
                    "        tbody tr:nth-child(even) td {\n" +
                    "            background-color: #e2e2e2;\n" +
                    "        }"));
        } catch (IOException e) {
            Log.e("escriure", e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
