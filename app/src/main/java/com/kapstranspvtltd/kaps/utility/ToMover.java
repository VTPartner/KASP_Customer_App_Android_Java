package com.kapstranspvtltd.kaps.utility;

import java.io.Serializable;

public class ToMover implements Serializable {
    String address;
    double lats;
    double lngs;
    String lift;
    String floor;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLats() {
        return lats;
    }

    public void setLats(double lats) {
        this.lats = lats;
    }

    public double getLngs() {
        return lngs;
    }

    public void setLngs(double lngs) {
        this.lngs = lngs;
    }

    public String getLift() {
        return lift;
    }

    public void setLift(String lift) {
        this.lift = lift;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }
}
