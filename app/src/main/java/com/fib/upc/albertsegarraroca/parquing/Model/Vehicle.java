package com.fib.upc.albertsegarraroca.parquing.Model;

import java.util.IllegalFormatCodePointException;
import java.util.regex.Pattern;

/**
 * Created by albert on 26/12/15.
 */
public class Vehicle {
    private final String registration;

    private static final Pattern p = Pattern.compile("[a-zA-Z\\d\\-]{6,}+");

    public void checkRegistration(String registration) {
        if (registration == null || registration.isEmpty())
            throw new IllegalArgumentException();
    }

    public Vehicle(String registration) throws IllegalArgumentException {
        checkRegistration(registration);
        this.registration = registration;
    }

    public String getRegistration() {
        return registration;
    }

    public boolean seemsValidRegistration() {
        return p.matcher(this.registration).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vehicle vehicle = (Vehicle) o;

        if (!registration.equals(vehicle.registration)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return registration.hashCode();
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "registration='" + registration + '\'' +
                '}';
    }
}
