package com.fib.upc.albertsegarraroca.parquing;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.fib.upc.albertsegarraroca.parquing.Data.Database;
import com.fib.upc.albertsegarraroca.parquing.Data.Files;
import com.fib.upc.albertsegarraroca.parquing.Data.Preferences;
import com.fib.upc.albertsegarraroca.parquing.Model.Parking;
import com.fib.upc.albertsegarraroca.parquing.Model.ParkingPlace;
import com.fib.upc.albertsegarraroca.parquing.Model.Vehicle;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivity;
import com.fib.upc.albertsegarraroca.parquing.Model.VehicleActivityList;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


public class MainActivity extends FragmentActivity
        implements PlacesFragment.OnFragmentInteractionListener,
                    SettingsFragment.OnFragmentInteractionListener,
                    ActivitiesFragment.OnFragmentInteractionListener {

    private static final int NUM_TABS = 3;

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
            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        addTab(0, tabListener);
        addTab(1, tabListener);
        addTab(2, tabListener);

        Preferences.getInstance().init(getApplicationContext());

        Database db = new Database(getApplicationContext());
        Parking parking = Parking.getInstance();
        parking.init(db);

        try {
            Files.getInstance().init();
        } catch (IOException e) {
            Log.e("FIles", "Error while creating folders");
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void addTab(int pos, ActionBar.TabListener tabListener) {
        ActionBar actionBar = getActionBar();

        String title;
        int icon;

        switch(pos) {
            case 0: title = "Places"; icon = R.drawable.ic_car; break;
            case 1: title = "Activitat"; icon = R.drawable.ic_swap_vertical; break;
            default: title = "Menú"; icon = R.drawable.ic_menu;
        }

        ActionBar.Tab tab = actionBar.newTab();
        tab.setCustomView(R.layout.tab_layout);
        ImageView iv = (ImageView) tab.getCustomView().findViewById(R.id.tab_icon);
        iv.setImageResource(icon);
        TextView tv = (TextView) tab.getCustomView().findViewById(R.id.tab_title);
        tv.setText(title.toUpperCase());
        tab.setTabListener(tabListener);

        actionBar.addTab(tab);
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
            switch(pos) {
                case 0: return new PlacesFragment();
                case 1: return new ActivitiesFragment();
                default: return new PlacesFragment();
            }
        }
    }
}
