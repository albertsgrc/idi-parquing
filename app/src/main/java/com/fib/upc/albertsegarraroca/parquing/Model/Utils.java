package com.fib.upc.albertsegarraroca.parquing.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by albert on 26/12/15.
 */
public class Utils {
    public static double roundEuros(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public static String formatDateLong(Date date) {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        return formatter.format(date);
    }

    public static String toEurosValueString(double value) {
        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance());
        return df.format(value);
    }
}
