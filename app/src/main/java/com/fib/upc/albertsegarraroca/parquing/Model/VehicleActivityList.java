package com.fib.upc.albertsegarraroca.parquing.Model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by albert on 26/12/15.
 */
public class VehicleActivityList {
    private final ArrayList<VehicleActivity> list;
    private final double totalIncome;

    private static final char CSV_SEPARATOR = ';';

    public VehicleActivityList(ArrayList<VehicleActivity> list, double totalIncome) {
        if (totalIncome < 0) throw new IllegalArgumentException();
        if (list == null) throw new NullPointerException();

        this.list = list;
        this.totalIncome = totalIncome;
    }

    public VehicleActivityList(ArrayList<VehicleActivity> list) {
        this(list, computeTotalIncome(list));
    }

    private static double computeTotalIncome(ArrayList<VehicleActivity> list) {
        double sum = 0.0;

        for (VehicleActivity va : list)
            if (va.getClass() == VehicleExit.class) sum += ((VehicleExit) va).getIncome();

        return Utils.roundEuros(sum); // En teoría no hace falta, pero por si las moscas
    }

    public ArrayList<VehicleActivity> getList() {
        return this.list;
    }

    public double getTotalIncome() {
        return this.totalIncome;
    }

    public String toCsv() {
        StringBuilder builder = new StringBuilder();

        builder.append("Tipus;Plaça;Data;Matricula;Benefici\n");

        for (VehicleActivity va : list) {
            boolean isEntry = va.getClass() == VehicleEntry.class;
            builder.append(isEntry ? "Entrada" : "Sortida").append(CSV_SEPARATOR)
                   .append(va.getPlace().getId()).append(CSV_SEPARATOR)
                   .append(Utils.formatDateLong(va.getDate())).append(CSV_SEPARATOR)
                   .append(va.getVehicle().getRegistration()).append(CSV_SEPARATOR)
                   .append(Utils.toEurosValueString(isEntry ? 0.0 : ((VehicleExit) va).getIncome()))
                   .append("\n");
        }

        return builder.toString();
    }

    public String toCsvGrouped() {
        StringBuilder builder = new StringBuilder();

        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance());

        builder.append("Plaça;Data Entrada;Data Sortida;Matricula;Benefici\n");

        for (VehicleActivity va : list) {
            boolean isEntry = va.getClass() == VehicleEntry.class;
            if (!isEntry) continue;

            VehicleEntry ve = (VehicleEntry) va;

            builder.append(ve.getPlace().getId()).append(CSV_SEPARATOR)
                   .append(Utils.formatDateLong(ve.getDate())).append(CSV_SEPARATOR)
                   .append(ve.hasFinished() ? Utils.formatDateLong(ve.getExitDate()) : "").append(CSV_SEPARATOR)
                   .append(va.getVehicle().getRegistration()).append(CSV_SEPARATOR)
                   .append(df.format(ve.getIncome()))
                   .append("\n");
        }

        return builder.toString();
    }

    // TODO: Filtering operations are done in database
    // TODO: Sorting maybe in android listAdapter? Otherwise here
}
