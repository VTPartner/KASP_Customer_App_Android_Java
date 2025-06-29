package com.kapstranspvtltd.kaps.activities.models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingDetails {
    private String bookingId;
    private String customerName;
    private String customerId;
    private String pickupAddress;
    private String dropAddress;
    private String driverName;
    private String driverMobileNo;
    private String bookingTiming;
    private String paymentMethod;
    private String bookingStatus;
    private String senderName;
    private String senderNumber;
    private String receiverName;
    private String receiverNumber;
    private String vehicleName;
    private String vehiclePlateNo;
    private String vehicleFuelType;
    private String driverImage;
    private String totalPrice;
    private String otp;
    private String driverId;
    private String vehicleImage;
    private String ratings;
    private String distance;
    private double pickupLat;
    private double pickupLng;
    private double dropLat;
    private double dropLng;

    private int couponID;

    private String couponApplied;

    private double couponDiscountAmount;

    private double penaltyAmount;

    public double getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(double walletAmount) {
        this.walletAmount = walletAmount;
    }

    public double getCoinsGiven() {
        return coinsGiven;
    }

    public void setCoinsGiven(int coinsGiven) {
        this.coinsGiven = coinsGiven;
    }

    private double walletAmount;

    private int coinsGiven;

    public double getPenaltyChargeAmount() {
        return penaltyChargeAmount;
    }

    public void setPenaltyChargeAmount(double penaltyChargeAmount) {
        this.penaltyChargeAmount = penaltyChargeAmount;
    }

    private double penaltyChargeAmount;

    public void setPenaltyAmount(double amount) {
        this.penaltyAmount = amount;
    }

    public double getPenaltyAmount() {
        return penaltyAmount;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    private double basePrice;

    public List<DropLocation> getDropLocations() {
        return dropLocations;
    }

    public void setDropLocations(List<DropLocation> dropLocations) {
        this.dropLocations = dropLocations;
    }

    private List<DropLocation> dropLocations;

    public int getCouponID() {
        return couponID;
    }

    public void setCouponID(int couponID) {
        this.couponID = couponID;
    }

    public String getCouponApplied() {
        return couponApplied;
    }

    public void setCouponApplied(String couponApplied) {
        this.couponApplied = couponApplied;
    }

    public double getCouponDiscountAmount() {
        return couponDiscountAmount;
    }

    public void setCouponDiscountAmount(double couponDiscountAmount) {
        this.couponDiscountAmount = couponDiscountAmount;
    }

    public double getBeforeCouponAmount() {
        return beforeCouponAmount;
    }

    public void setBeforeCouponAmount(double beforeCouponAmount) {
        this.beforeCouponAmount = beforeCouponAmount;
    }

    private double beforeCouponAmount;

    // Default constructor
    public BookingDetails() {}

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDropAddress() {
        return dropAddress;
    }

    public void setDropAddress(String dropAddress) {
        this.dropAddress = dropAddress;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverMobileNo() {
        return driverMobileNo;
    }

    public void setDriverMobileNo(String driverMobileNo) {
        this.driverMobileNo = driverMobileNo;
    }

    public String getBookingTiming() {
        return bookingTiming;
    }

    public void setBookingTiming(String bookingTiming) {
        this.bookingTiming = bookingTiming;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverNumber() {
        return receiverNumber;
    }

    public void setReceiverNumber(String receiverNumber) {
        this.receiverNumber = receiverNumber;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVehiclePlateNo() {
        return vehiclePlateNo;
    }

    public void setVehiclePlateNo(String vehiclePlateNo) {
        this.vehiclePlateNo = vehiclePlateNo;
    }

    public String getVehicleFuelType() {
        return vehicleFuelType;
    }

    public void setVehicleFuelType(String vehicleFuelType) {
        this.vehicleFuelType = vehicleFuelType;
    }

    public String getDriverImage() {
        return driverImage;
    }

    public void setDriverImage(String driverImage) {
        this.driverImage = driverImage;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getVehicleImage() {
        return vehicleImage;
    }

    public void setVehicleImage(String vehicleImage) {
        this.vehicleImage = vehicleImage;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public double getDropLat() {
        return dropLat;
    }

    public void setDropLat(double dropLat) {
        this.dropLat = dropLat;
    }

    public double getDropLng() {
        return dropLng;
    }

    public void setDropLng(double dropLng) {
        this.dropLng = dropLng;
    }

    // Helper method to get pickup LatLng
    public LatLng getPickupLatLng() {
        return new LatLng(pickupLat, pickupLng);
    }

    // Helper method to get drop LatLng
    public LatLng getDropLatLng() {
        return new LatLng(dropLat, dropLng);
    }

    // Helper method to parse booking timing from epoch
    public String getFormattedBookingTiming() {
        try {
            double epochTime = Double.parseDouble(bookingTiming);
            long milliseconds = (long) (epochTime * 1000);
            Date date = new Date(milliseconds);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, hh:mm a", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return bookingTiming;
        }
    }

    // Helper method to get current status text
//    public String getCurrentStatus() {
//        switch (bookingStatus) {
//            case "Driver Accepted":
//                return "Accepted by the driver";
//            case "Driver Arrived":
//                return "Driver arrived";
//            case "Otp Verified":
//                return "Otp Verified";
//            case "Start Trip":
//                return "Pickup Done On the way to deliver";
//            case "Make Payment":
//                return "Delivery Done Do the Payment";
//
//            default:
//                return "Cancelled";
//        }
//    }

    private String serviceType; // "goods", "cab", "jcb", "crane", "driver", "handyman"

    // Add getter and setter
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getCurrentStatus() {
        if (bookingStatus == null) return "Unknown";

        switch (bookingStatus) {
            // Common initial stages
            case "Driver Accepted":
                return "Accepted by the provider";
            case "Driver Arrived":
                return "Provider arrived at location";
            case "Otp Verified":
                return "OTP verified successfully";

            // Goods specific stages
            case "Start Trip":
                return "Pickup done - On the way to deliver";
            case "Reached Drop Location 1":
                return "Reached first drop location";
            case "Reached Drop Location 2":
                return "Reached second drop location";
            case "Reached Drop Location 3":
                return "Reached final drop location";

            // Cab specific stages
            case "Start Ride":
                return "Ride started";
            case "End Ride":
                return "Ride completed";

            // JCB/Crane specific stages
            case "Start Service":
                return "Work started";


            // Driver service specific stages
            case "Start Duty":
                return "Duty started";
            case "End Duty":
                return "Duty completed";

            // Handyman specific stages
            case "Start Work":
                return "Work started";
            case "Work In Progress":
                return "Work in progress";


            // Common completion stages
            case "Make Payment":
                return isGoodsService() ? "Delivery completed - Payment pending" :
                        isCabService() ? "Ride completed - Payment pending" :
                                isJCBService() ? "Work completed - Payment pending" :
                                        isDriverService() ? "Duty completed - Payment pending" :
                                                "Service completed - Payment pending";

            case "End Trip":
            case "End Service":
            case "End Work":
                return isGoodsService() ? "Delivery completed" :
                        isCabService() ? "Ride completed" :
                                isJCBService() ? "Work completed" :
                                        isDriverService() ? "Duty completed" :
                                                "Service completed";

            case "Cancelled":
                return "Booking cancelled";

            default:
                return bookingStatus;
        }
    }

    // Helper methods to determine service type
    private boolean isGoodsService() {
        // Add your logic to determine if this is a goods service
        return serviceType != null && serviceType.equals("goods");
    }

    private boolean isCabService() {
        // Add your logic to determine if this is a cab service
        return serviceType != null && serviceType.equals("cab");
    }

    private boolean isJCBService() {
        // Add your logic to determine if this is a JCB/Crane service
        return serviceType != null && (serviceType.equals("jcb") || serviceType.equals("crane"));
    }

    private boolean isDriverService() {
        // Add your logic to determine if this is a driver service
        return serviceType != null && serviceType.equals("driver");
    }

    private boolean isHandymanService() {
        // Add your logic to determine if this is a handyman service
        return serviceType != null && serviceType.equals("handyman");
    }
}