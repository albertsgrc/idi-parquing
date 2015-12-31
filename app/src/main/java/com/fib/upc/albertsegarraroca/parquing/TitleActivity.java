package com.fib.upc.albertsegarraroca.parquing;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by albert on 31/12/15.
 */
public class TitleActivity extends FragmentActivity {
    protected TextView txtOccupation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        setContentView(R.layout.activity_main);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
    }
}
