package com.fib.upc.albertsegarraroca.parquing.Data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by albert on 29/12/15.
 */
public class Preferences {
    private static Preferences ourInstance = new Preferences();

    public static Preferences getInstance() {
        return ourInstance;
    }

    private Preferences() {}

    private SharedPreferences dataPrefs;
    private SharedPreferences settingsPrefs;

    public void init(Context applicationContext) {
        dataPrefs = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE);
        settingsPrefs = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }


    public void setLastActivityDate(String date) {
        SharedPreferences.Editor e = dataPrefs.edit();

        e.putString("lastActivityDate", date);

        e.apply();
    }

    public String getLastActivityDate() {
        return dataPrefs.getString("lastActivityDate", null);
    }
}
