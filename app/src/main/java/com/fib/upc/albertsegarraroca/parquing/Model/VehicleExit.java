package com.fib.upc.albertsegarraroca.parquing.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Created by albert on 26/12/15.
 */
public class VehicleExit extends VehicleActivity {
    private final VehicleEntry associatedEntry;
    private final double income;

    private static final double PRICE_PER_MINUTE = 0.02f;
    private static final int MILLISECONDS_PER_MINUTE = 60*1000;

    public VehicleExit(Date date, VehicleEntry associatedEntry, double income) {
        super(date);

        if (associatedEntry == null) throw new NullPointerException();
        if (associatedEntry.getDate().after(getDate())) throw new IllegalArgumentException();

        this.associatedEntry = associatedEntry;
        this.income = income;
    }
    public VehicleExit(Date date, VehicleEntry associatedEntry) {
        this(date, associatedEntry, calculateIncome(associatedEntry.getDate(), date));
    }

    public int getStayTimeInMinutes() {
        return calculateStayTimeInMinutes(associatedEntry.getDate(), getDate());
    }

    private static int calculateStayTimeInMinutes(Date entryDate, Date exitDate) {
        double parkDurationInMillis = exitDate.getTime() - entryDate.getTime();
        return (int) (parkDurationInMillis/((double) MILLISECONDS_PER_MINUTE));
    }

    public static double calculateIncome(Date entryDate, Date exitDate) {
        int parkDurationInMinutes = calculateStayTimeInMinutes(entryDate, exitDate);
        return Utils.roundEuros(PRICE_PER_MINUTE*parkDurationInMinutes);
    }

    public VehicleEntry getAssociatedEntry() {
        return associatedEntry;
    }

    @Override
    public double getIncome() {
        return income;
    }

    @Override
    public String getPlace() {
        return associatedEntry.getPlace();
    }

    @Override
    public Vehicle getVehicle() {
        return associatedEntry.getVehicle();
    }

    public Date getEntryDate() {
        return associatedEntry.getDate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VehicleExit that = (VehicleExit) o;

        if (!associatedEntry.equals(that.getAssociatedEntry())) return false;
        if (income != that.getIncome()) return false;
        if (!getDate().equals(that.getDate())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = associatedEntry.hashCode();
        result = 31 * result + ((Double)income).hashCode();
        result = 31 * result * getDate().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "VehicleExit{" +
                "date=" + getDate() +
                ", associatedEntry=" + associatedEntry +
                ", accumulatedIncome=" + income +
                '}';
    }
}
