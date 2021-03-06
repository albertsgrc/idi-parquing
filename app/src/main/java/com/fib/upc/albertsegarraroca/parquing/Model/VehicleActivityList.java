package com.fib.upc.albertsegarraroca.parquing.Model;

import android.app.SearchManager;
import android.content.Context;

import com.fib.upc.albertsegarraroca.parquing.Data.Database;
import com.fib.upc.albertsegarraroca.parquing.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by albert on 26/12/15.
 */
public class VehicleActivityList {
    private final ArrayList<VehicleActivity> list;
    private double totalIncome;
    private int entries;
    private int exits;

    private static final char CSV_SEPARATOR = ';';

    public VehicleActivityList(Database.ActivityListInfo info) {
        if (info.totalIncome < 0) throw new IllegalArgumentException();
        if (info.list == null) throw new NullPointerException();

        this.list = info.list;
        this.totalIncome = info.totalIncome;
        this.entries = info.entries;
        this.exits = info.exits;
    }


    public VehicleActivityList(Database db, Date beggining, Date end) {
        this(db.getActivitiesBetween(Utils.dateToDBString(beggining), Utils.dateToDBString(end)));
    }

    public VehicleActivityList(Database db) {
        this(db, Utils.getTodays00time(), new Date());
    }

    public VehicleActivityList(Database db, boolean always) {
        this(db.getAllActivities());
    }


    public ArrayList<VehicleActivity> getList() {
        return this.list;
    }

    public int getTotalEntries() { return this.entries; }

    public int getTotalExits() {
         return this.exits;
    }

    public double getTotalIncome() {
        return this.totalIncome;
    }

    /**
     *
     * @param header Type;Place;Date;Registration;Income
     * @return
     */
    public String toCsv(String header) {
        StringBuilder builder = new StringBuilder();

        builder.append(header).append("\n");

        for (VehicleActivity va : list) {
            boolean isEntry = va.getClass() == VehicleEntry.class;
            builder.append(isEntry ? "Entrada" : "Sortida").append(CSV_SEPARATOR)
                   .append(va.getPlace()).append(CSV_SEPARATOR)
                   .append(Utils.formatDateLong(va.getDate())).append(CSV_SEPARATOR)
                   .append(va.getVehicle().getRegistration()).append(CSV_SEPARATOR)
                   .append(Utils.toEurosValueString(va.getIncome()))
                   .append("\n");
        }

        return builder.toString();
    }

    /**
     *
     * @param header Place;DateEntry;DateExit;Registration;Income
     * @return
     */
    public String toCsvGrouped(String header, String not_finished) {
        StringBuilder builder = new StringBuilder();

        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance());


        builder.append(header).append("\n");

        for (VehicleActivity va : list) {
            boolean isEntry = va.getClass() == VehicleEntry.class;
            if (!isEntry) continue;

            VehicleEntry ve = (VehicleEntry) va;

            builder.append(ve.getPlace()).append(CSV_SEPARATOR)
                   .append(Utils.formatDateLong(ve.getDate())).append(CSV_SEPARATOR)
                   .append(ve.hasFinished() ? Utils.formatDateLong(ve.getExitDate()) : not_finished).append(CSV_SEPARATOR)
                   .append(va.getVehicle().getRegistration()).append(CSV_SEPARATOR)
                   .append(df.format(ve.hasFinished() ? ve.getAssociatedExit().getIncome() : 0.0))
                   .append("\n");
        }

        return builder.toString();
    }

    private int binarySearch(Date date) {
        int l = 0;
        int r = list.size() - 1;

        while (r > l) {
            int m = (l + r)/2;
            VehicleActivity a = list.get(m);
            if      (date.after(a.getDate())) l = m + 1;
            else if (a.getDate().after(date)) r = m - 1;
            else return m;
        }

        return r;
    }

    public int firstBefore(Date date) {
        int k = binarySearch(date);
        if (list.get(k).getDate().after(date)) return k-1;
        else return k;
    }

    public int firstAfter(Date date) {
        int k = binarySearch(date);
        if (list.get(k).getDate().before(date)) return k+1;
        else return k;
    }

    public VehicleActivity getActivity(Date date) {
        int l = 0;
        int r = list.size() - 1;

        while (r >= l) {
            int m = (l + r)/2;
            VehicleActivity a = list.get(m);
            if      (date.after(a.getDate())) r = m - 1;
            else if (a.getDate().after(date)) l = m + 1;
            else return a;
        }

        return null;
    }

    public double getIncomeBetween(Date beggining, Date end) {
        int l = firstAfter(beggining);
        int r = firstBefore(end);

        l = Math.max(l, 0);
        r = Math.min(list.size() - 1, r);

        double sum = 0;

        for (int i = l; i <= r; ++i) sum += list.get(i).getIncome();

        return sum;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public VehicleActivity getLastActivity() {
        return list.get(list.size() - 1);
    }

    /*
    public void removeLastActivity() {
        VehicleActivity lastActivity = getLastActivity();
        totalIncome -= lastActivity.getIncome();
        list.remove(list.size() - 1);
    }*/

    /**
     *
     * @param header Month;Benefit;Entries;Exits;AvgStayTime
     * @param css    Css for the html table
     * @return
     */
    public String toHtml(String header, String title,  String css, String entriesTxt, String exitsTxt, String minutesTxt) {
        entriesTxt = entriesTxt.toLowerCase();
        exitsTxt = exitsTxt.toLowerCase();

        StringBuilder builder = new StringBuilder();

        builder.append("<!DOCTYPE html>")
               .append("<html>")
               .append("<head>")
                .append("<title>").append(title).append("</title>")
                .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />")
                .append("<style>").append(css).append("</style>")
               .append("</head>")
               .append("<body>")
                .append("<table>")
                    .append("<thead>")
                        .append("<tr>");

        for (String h : header.split(";")) builder.append("<th>").append(h).append("</th>");

        builder.append("</tr>")
               .append("</thead>")
               .append("<tbody>");

        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getDefault());

        int i = 0;
        while (i < list.size()) {
            VehicleActivity ini = list.get(i);
            cal.setTime(ini.getDate());

            int accYear, accMonth, currentMonth;
            accYear = cal.get(Calendar.YEAR);
            accMonth = currentMonth = cal.get(Calendar.MONTH);

            double income = 0.0;
            int entries, exits, stayTime;
            entries = exits = stayTime = 0;

            VehicleActivity va = ini;
            while (i < list.size()) {
                va = list.get(i);
                cal.setTime(va.getDate());

                currentMonth = cal.get(Calendar.MONTH);

                if (currentMonth != accMonth) break;

                if (va.getClass() == VehicleEntry.class) ++entries;
                else {
                    ++exits;
                    VehicleExit ve = (VehicleExit) va;
                    stayTime += ve.getStayTimeInMinutes();
                    income += ve.getIncome();
                }

                ++i;
            }

            builder.append("<tr>")
                    .append("<td>").append(Utils.toMonthString(accMonth)).append(" ").append(accYear).append("</td>")
                    .append("<td>").append(Utils.toEurosValueString(income)).append(" €").append("</td>")
                    .append("<td>").append(entries).append(" ").append(entriesTxt).append("</td>")
                    .append("<td>").append(exits).append(" ").append(exitsTxt).append("</td>")
                    .append("<td>").append(stayTime / exits).append(" ").append(minutesTxt).append("</td>")
                   .append("</tr>");
        }

        builder.append("</tbody>")
               .append("</table>")
               .append("</body>")
               .append("</html>");

        return builder.toString();
    }

    // TODO: Filtering operations are done in database
    // TODO: Sorting maybe in android listAdapter? Otherwise here
}
