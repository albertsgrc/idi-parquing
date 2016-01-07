package com.fib.upc.albertsegarraroca.parquing;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fib.upc.albertsegarraroca.parquing.Data.Database;
import com.fib.upc.albertsegarraroca.parquing.Data.Files;
import com.fib.upc.albertsegarraroca.parquing.Data.Preferences;
import com.fib.upc.albertsegarraroca.parquing.Model.Parking;
import com.fib.upc.albertsegarraroca.parquing.Model.ParkingPlace;
import com.fib.upc.albertsegarraroca.parquing.Model.Utils;
import com.fib.upc.albertsegarraroca.parquing.Model.Vehicle;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivity;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleEntry;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleExit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private static final int NUM_TABS = 3;
    private static PlacesFragment placesFragment;
    private static RevenueFragment revenueFragment;
    private static MenuFragment settingsFragment;

    private Runnable currentRunnable;

    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        final PagerAdapter mAdapter = new PagerAdapter(getSupportFragmentManager());
        final ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(NUM_TABS);

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
        };

        addTab(0, tabListener);
        addTab(1, tabListener);
        addTab(2, tabListener);

        Preferences.getInstance().init(getApplicationContext());

        db = new Database(getApplicationContext());
        Parking parking = Parking.getInstance();
        parking.init(db);
        Utils.setContext(getApplicationContext());

        try {
            Files.getInstance().init();
        } catch (IOException e) {
            Utils.showAlertDialog(getString(R.string.error_create_folders));
        }

        setBottomBarListeners();
        updateOccupation();
    }

    public void updateOccupation() {
        TextView occupation = (TextView) findViewById(R.id.txtOccupationInfo);

        int occupiedPlaces = Parking.getInstance().getVehicles().size();
        int activePlaces = Parking.getInstance().countActivePlaces();
        occupation.setText(occupiedPlaces + "/" + activePlaces);

        int barColor;
        switch((int) ((4.0*occupiedPlaces)/activePlaces)) {
            case 0: barColor = R.color.okay; break;
            case 1: barColor = R.color.normal; break;
            case 2: barColor = R.color.delicate; break;
            default: barColor = R.color.bad; break;
        }

        findViewById(R.id.bottom_bar).setBackgroundResource(barColor);
    }

    private void setBottomBarListeners() {
        Button newEntry = (Button) findViewById(R.id.btnNewEntry);
        Button newExit = (Button) findViewById(R.id.btnNewExit);

        newEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.vibrateClick();
                if (Parking.getInstance().isFull())
                    Utils.showToast(getString(R.string.error_parking_full), Toast.LENGTH_SHORT);
                else addNewEntry();
            }
        });

        newExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.vibrateClick();
                addNewExit();
            }
        });
    }

    public void undoLastActivity(View v) {

        try {
            VehicleActivity va = Parking.getInstance().undoLastActivity();

            if (va == null) {
                Utils.showToast(getString(R.string.error_no_last_activity), Toast.LENGTH_LONG);
                return;
            }

            undoComplete(va);
        } catch (IllegalStateException e) {
            new AlertDialog.Builder(this).setTitle(R.string.error)
                    .setMessage(R.string.error_cannot_undo)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.activate, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            VehicleActivity va = Parking.getInstance().undoLastActivityForced();
                            undoComplete(va);
                        }
                    }).create().show();
        }

        findViewById(R.id.undoBar).setVisibility(View.GONE);
    }

    private void undoComplete(VehicleActivity va) {
        boolean entry = va.getClass() == VehicleEntry.class;
        updateOccupation();
        if (placesFragment != null) placesFragment.updatePlace(va.getPlace());
        if (entry) Utils.showToast(getString(R.string.undo_entry_complete), Toast.LENGTH_SHORT);
        else Utils.showToast(getString(R.string.undo_exit_complete), Toast.LENGTH_SHORT);
    }

    private void showUndo(int txt, String vehicle) {
        String text = getString(txt);

        final View undoBar = findViewById(R.id.undoBar);
        ((TextView) undoBar.findViewById(R.id.undoText)).setText(text.replace("_X_", vehicle));

        undoBar.setVisibility(View.VISIBLE);
        undoBar.removeCallbacks(currentRunnable);

        currentRunnable = new Runnable() {
            @Override
            public void run() {
                undoBar.setVisibility(View.GONE);
            }
        };

        undoBar.postDelayed(currentRunnable, 10000);
    }

    private void addNewEntry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.new_entry_title);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        builder.setView(inflater.inflate(R.layout.dialog_new_entry, null));

        builder.setPositiveButton(R.string.enter, null)
               .setNegativeButton(R.string.cancel, null);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {

                EditText et = (EditText) dialog.findViewById(R.id.editRegistration);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView tv = (TextView) dialog.findViewById(R.id.editRegistration);
                        final String text = tv.getText().toString();

                        if (text.isEmpty()) Utils.showToast(getString(R.string.error_empty_registration), Toast.LENGTH_LONG);
                        else {
                            Vehicle vehicle = new Vehicle(text);

                            if (Preferences.getInstance().registrationValidation() && !vehicle.seemsValidRegistration()) {
                                Utils.showToast(getString(R.string.error_invalid_registration), Toast.LENGTH_LONG);
                            }
                            else if (Parking.getInstance().isInside(vehicle)) Utils.showToast(getString(R.string.error_vehicle_inside), Toast.LENGTH_LONG);
                            else {
                                dialog.dismiss();
                                addNewVehicleEntry(vehicle);
                            }
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void addNewVehicleEntry(Vehicle vehicle) {
        ParkingPlace place = Parking.getInstance().enterVehicle(vehicle);

        // TODO: Show undo

        if (placesFragment != null) {
            placesFragment.updatePlace(place);
        }

        // TODO: Update revenue fragment

        updateOccupation();
        showUndo(R.string.undo_entry, vehicle.getRegistration());
    }

    private void addNewExit() {

        List<Vehicle> vehicles = Parking.getInstance().getVehicles();

        if (vehicles.size() == 0) {
            Utils.showToast(getString(R.string.error_parking_empty), Toast.LENGTH_SHORT);
            return;
        }

        final String[] l = new String[vehicles.size()];

        int i = 0;
        for (Vehicle v : vehicles) l[i++] = v.getRegistration();

        Arrays.sort(l);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(R.string.new_exit_title)
                .setNegativeButton(R.string.cancel, null);

        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_list, null);
        b.setView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, l);

        final AlertDialog d = b.create();

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                d.dismiss();
                exitVehicle(l[position]);
            }
        });

        d.setCanceledOnTouchOutside(true);
        d.show();
    }

    public void exitVehicle(String vehicle) {
        findViewById(R.id.undoBar).setVisibility(View.GONE);

        VehicleExit exit = Parking.getInstance().exitVehicle(new Vehicle(vehicle));

        View view = getLayoutInflater().inflate(R.layout.dialog_ticket, null);

        ((TextView) view.findViewById(R.id.ticketPlaceInfo).findViewById(R.id.title)).setText(R.string.parking_place_short);
        ((TextView) view.findViewById(R.id.ticketRegistrationInfo).findViewById(R.id.title)).setText(R.string.registration_long);
        ((TextView) view.findViewById(R.id.ticketTimeEntryInfo).findViewById(R.id.title)).setText(R.string.entry_date);
        ((TextView) view.findViewById(R.id.ticketTimeExitInfo).findViewById(R.id.title)).setText(R.string.exit_date);
        ((TextView) view.findViewById(R.id.ticketIncomeInfo).findViewById(R.id.title)).setText(R.string.price);

        ((TextView) view.findViewById(R.id.ticketPlaceInfo).findViewById(R.id.info)).setText(exit.getPlace());
        ((TextView) view.findViewById(R.id.ticketRegistrationInfo).findViewById(R.id.info)).setText(vehicle);
        ((TextView) view.findViewById(R.id.ticketTimeEntryInfo).findViewById(R.id.info)).setText(Utils.formatDateLong(exit.getEntryDate()));
        ((TextView) view.findViewById(R.id.ticketTimeExitInfo).findViewById(R.id.info)).setText(Utils.formatDateLong(exit.getDate()));
        ((TextView) view.findViewById(R.id.ticketIncomeInfo).findViewById(R.id.info)).setText(Utils.toEurosValueString(exit.getIncome()) + " €");

        new AlertDialog.Builder(this)
                .setTitle(R.string.ticket)
                .setView(view)
                .setPositiveButton(R.string.okay, null)
                .setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        undoLastActivity(null);
                    }
                }).create().show();

        if (placesFragment != null)
            placesFragment.updatePlace(exit.getPlace());

        // TODO: Update revenue fragment
        updateOccupation();
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

    private void addTab(int pos, ActionBar.TabListener tabListener) {
        ActionBar actionBar = getActionBar();

        int title;
        int icon;

        switch(pos) {
            case 0: title = R.string.places; icon = R.drawable.ic_car; break;
            case 1: title = R.string.revenue; icon = R.drawable.ic_coins; break;
            default: title = R.string.menu; icon = R.drawable.ic_menu;
        }

        ActionBar.Tab tab = actionBar.newTab();
        tab.setCustomView(R.layout.tab_layout);
        ImageView iv = (ImageView) tab.getCustomView().findViewById(R.id.tab_icon);
        iv.setImageResource(icon);
        TextView tv = (TextView) tab.getCustomView().findViewById(R.id.tab_title);
        tv.setText(title);
        tab.setTabListener(tabListener);

        actionBar.addTab(tab);
    }

    public Database getDatabase() {
        return this.db;
    }

    public static class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return placesFragment = new PlacesFragment();
                case 1:
                    return revenueFragment = new RevenueFragment();
                default:
                    return settingsFragment = new MenuFragment();
            }
        }
    }
}
