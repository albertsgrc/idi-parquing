package com.fib.upc.albertsegarraroca.parquing.Model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Vibrator;
import android.widget.Toast;

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

    public static String toMonthString(int m) {
        return "";
        // TODO: Implement
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

    public static void showAlertDialog(String txt) {
        AlertDialog d = new AlertDialog.Builder(context).setTitle(context.getString(idstoi("warning")))
                .setMessage(txt).setPositiveButton(context.getString(idstoi("okay")), null).create();

        d.show();
    }

    public static int idstoi(String id) {
        return context.getResources().getIdentifier(id, "id", context.getPackageName());
    }

    public static String iditos(int id) {
        return context.getResources().getResourceEntryName(id);
    }
}
