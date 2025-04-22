package com.kapstranspvtltd.kaps.model;

public class DUser {
    private  String name;
    private  String vimg;
    private  double lat;
    private  double lons;

    public DUser() {
    }

    public DUser(String name, String vimg, double lat, double lons) {
        this.name = name;
        this.vimg = vimg;
        this.lat = lat;
        this.lons = lons;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVimg() {
        return vimg;
    }

    public void setVimg(String vimg) {
        this.vimg = vimg;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLons() {
        return lons;
    }

    public void setLons(double lons) {
        this.lons = lons;
    }
}
