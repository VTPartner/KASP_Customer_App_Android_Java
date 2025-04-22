package com.kapstranspvtltd.kaps.common_activities.models;

public class OtherService {
    private int serviceId;
    private String serviceName;
    private String serviceImage;
    private double pricePerHour;
    private double serviceBasePrice;
    
    // Constructor
    public OtherService(int serviceId, String serviceName, String serviceImage, 
                       double pricePerHour, double serviceBasePrice) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceImage = serviceImage;
        this.pricePerHour = pricePerHour;
        this.serviceBasePrice = serviceBasePrice;
    }
    
    // Getters
    public int getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public String getServiceImage() { return serviceImage; }
    public double getPricePerHour() { return pricePerHour; }
    public double getServiceBasePrice() { return serviceBasePrice; }
}