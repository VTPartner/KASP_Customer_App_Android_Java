package com.kapstranspvtltd.kaps.activities.models;

public class VehicleModel {
    private int vehicleId;
    private String vehicleName;
    private String vehicleImage;
    private double baseFare;
    private double pricePerKm;
    private String sizeImage;
    private String weight;
    private double startDistance;
    private double afterPrice;
    private double timeTaken;

    private boolean isSelected;

    private int outStationDistance;

    // Default constructor
    public VehicleModel() {
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    // Full constructor
    public VehicleModel(int vehicleId, String vehicleName, String vehicleImage,
                        double baseFare, double pricePerKm, String sizeImage,
                        String weight,int outStationDistance) {
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.vehicleImage = vehicleImage;
        this.baseFare = baseFare;
        this.pricePerKm = pricePerKm;
        this.sizeImage = sizeImage;
        this.weight = weight;
        this.outStationDistance = outStationDistance;

    }

    public int getOutStationDistance() {
        return outStationDistance;
    }

    public void setOutStationDistance(int outStationDistance) {
        this.outStationDistance = outStationDistance;
    }

    // Getters
    public int getVehicleId() {
        return vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getVehicleImage() {
        return vehicleImage;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getPricePerKm() {
        return pricePerKm;
    }

    public String getSizeImage() {
        return sizeImage;
    }

    public String getWeight() {
        return weight;
    }

    public double getStartDistance() {
        return startDistance;
    }

    public double getAfterPrice() {
        return afterPrice;
    }

    public double getTimeTaken() {
        return timeTaken;
    }

    // Setters
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public void setVehicleImage(String vehicleImage) {
        this.vehicleImage = vehicleImage;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public void setPricePerKm(double pricePerKm) {
        this.pricePerKm = pricePerKm;
    }

    public void setSizeImage(String sizeImage) {
        this.sizeImage = sizeImage;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setStartDistance(double startDistance) {
        this.startDistance = startDistance;
    }

    public void setAfterPrice(double afterPrice) {
        this.afterPrice = afterPrice;
    }

    public void setTimeTaken(double timeTaken) {
        this.timeTaken = timeTaken;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "VehicleModel{" +
                "vehicleId=" + vehicleId +
                ", vehicleName='" + vehicleName + '\'' +
                ", vehicleImage='" + vehicleImage + '\'' +
                ", baseFare=" + baseFare +
                ", pricePerKm=" + pricePerKm +
                ", sizeImage='" + sizeImage + '\'' +
                ", weight='" + weight + '\'' +
                ", startDistance=" + startDistance +
                ", afterPrice=" + afterPrice +
                ", timeTaken=" + timeTaken +
                '}';
    }

    // equals and hashCode methods for comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleModel that = (VehicleModel) o;
        return vehicleId == that.vehicleId;
    }

    @Override
    public int hashCode() {
        return vehicleId;
    }
}