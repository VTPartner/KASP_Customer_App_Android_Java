package com.kapstranspvtltd.kaps.activities.models;

import java.util.Objects;

public class AllVehicleModel {
    private final int vehicleId;
    private final String vehicleName;
    private final String vehicleImage;
    private final String vehicleSizeImage;
    private final double basePrice;
    private final double perKmPrice;
    private final String vehicleWeight;

    public AllVehicleModel(int vehicleId,
                           String vehicleName,
                           String vehicleImage,
                           String vehicleSizeImage,
                           double basePrice,
                           double perKmPrice,
                           String vehicleWeight) {
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.vehicleImage = vehicleImage;
        this.vehicleSizeImage = vehicleSizeImage;
        this.basePrice = basePrice;
        this.perKmPrice = perKmPrice;
        this.vehicleWeight = vehicleWeight;
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

    public String getVehicleSizeImage() {
        return vehicleSizeImage;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getPerKmPrice() {
        return perKmPrice;
    }

    public String getVehicleWeight() {
        return vehicleWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllVehicleModel that = (AllVehicleModel) o;
        return vehicleId == that.vehicleId &&
                Double.compare(that.basePrice, basePrice) == 0 &&
                Double.compare(that.perKmPrice, perKmPrice) == 0 &&
                Objects.equals(vehicleName, that.vehicleName) &&
                Objects.equals(vehicleImage, that.vehicleImage) &&
                Objects.equals(vehicleSizeImage, that.vehicleSizeImage) &&
                Objects.equals(vehicleWeight, that.vehicleWeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, vehicleName, vehicleImage, vehicleSizeImage, 
                          basePrice, perKmPrice, vehicleWeight);
    }

    @Override
    public String toString() {
        return "AllVehicleModel{" +
                "vehicleId=" + vehicleId +
                ", vehicleName='" + vehicleName + '\'' +
                ", vehicleImage='" + vehicleImage + '\'' +
                ", vehicleSizeImage='" + vehicleSizeImage + '\'' +
                ", basePrice=" + basePrice +
                ", perKmPrice=" + perKmPrice +
                ", vehicleWeight='" + vehicleWeight + '\'' +
                '}';
    }
}