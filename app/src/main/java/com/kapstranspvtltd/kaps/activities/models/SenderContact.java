package com.kapstranspvtltd.kaps.activities.models;

import java.util.Objects;

public class SenderContact {
    private final String name;
    private final String phoneNumber;

    public SenderContact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SenderContact that = (SenderContact) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(phoneNumber, that.phoneNumber);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(name, phoneNumber);
    }

    // toString() method
    @Override
    public String toString() {
        return "SenderContact{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}