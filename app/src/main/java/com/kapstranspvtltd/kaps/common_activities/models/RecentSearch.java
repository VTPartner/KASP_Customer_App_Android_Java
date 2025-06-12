package com.kapstranspvtltd.kaps.common_activities.models;

public class RecentSearch {

    private String companyName;
    private String placeName;
    private String address;
    private double latitude;
    private double longitude;
    private long timestamp;
    private double distance;

    public RecentSearch(String companyName, String address, double latitude, double longitude, double distance) {
        this.companyName = companyName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.timestamp = System.currentTimeMillis();
    }

    public RecentSearch(String placeName,String address, double latitude, double longitude) {
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

    public RecentSearch(String address, double latitude, double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCompanyName() { return companyName; }
    public double getDistance() { return distance; }
    public String getPlaceName() { return placeName; }
    // Getters
    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // For comparison in lists
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentSearch that = (RecentSearch) o;
        return (address != null && address.equals(that.address)) ||
                (placeName != null && placeName.equals(that.placeName));
    }

    @Override
    public int hashCode() {
        int result = placeName != null ? placeName.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }
}