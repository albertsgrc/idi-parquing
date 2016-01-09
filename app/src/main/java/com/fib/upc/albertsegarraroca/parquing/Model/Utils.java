package com.fib.upc.albertsegarraroca.parquing.Model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.widget.Toast;

import com.fib.upc.albertsegarraroca.parquing.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by albert on 26/12/15.
 */
public class Utils {
    public static final String APPLICATION_NAME = "Parquing";
    private static Context context;
    private static Toast toast;

    public static void setContext(Context c) { context = c; }

    public static double roundEuros(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public static String formatDateLong(Date date) {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        return formatter.format(date);
    }

    public static String formatDateShort(Date date) {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        return formatter.format(date);
    }

    public static String toEurosValueString(double value) {
        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance());
        return df.format(value);
    }

    public static String capitalize(String s) {
        if (s != null && s.length() > 0) s = (""+s.charAt(0)).toUpperCase() + s.substring(1);
        return s;
    }

    public static String toMonthString(int m) {
        Calendar c = Calendar.getInstance();
        c.set(1995, m, 1);
        return capitalize(c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
    }

    public static String dateToDBString(Date date) {
        return Long.toString(date.getTime());
    }

    public static Date dbStringToDate(String date) {
        return new Date(Long.parseLong(date));
    }

    public static Date getTodays00time() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static void vibrateClick() {
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(3);
    }

    public static void showToast(String txt, int len) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context,
                txt,
                len);
        toast.show();
    }

    public static void showToast(String txt, int len, int dur) {
        showToast(txt, Toast.LENGTH_SHORT);

        new CountDownTimer(dur*1000, 1000)
        {

            public void onTick(long millisUntilFinished) {toast.show();}
            public void onFinish() {toast.show();}

        }.start();
    }

    public static void showAlertDialog(String txt) {
        AlertDialog d = new AlertDialog.Builder(context).setTitle(R.string.warning)
                .setMessage(txt).setPositiveButton(R.string.okay, null).create();

        d.show();
    }

    public static int idstoi(String id) {
        return context.getResources().getIdentifier(id, "id", context.getPackageName());
    }

    public static String fileTimeStamp() {
        return new SimpleDateFormat("dd-MM-yyyy-hh-mm").format(new Date());
    }
}
