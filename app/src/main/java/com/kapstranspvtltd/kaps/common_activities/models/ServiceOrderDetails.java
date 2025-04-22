package com.kapstranspvtltd.kaps.common_activities.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;





public class ServiceOrderDetails {
    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    private String bookingDate;
    private String bookingId;
    private String orderId;
    private String customerName;
    private String customerId;
    private String pickupAddress;
    private String dropAddress;
    private String driverName;
    private String driverMobileNo;
    private String bookingTiming;
    private String paymentMethod;
    private String bookingStatus;
    private String subCatName;
    private String serviceName;
    private String driverImage;
    private String totalPrice;
    private String otp;
    private String driverId;
    private String ratings;
    private String distance;
    private double pickupLat;
    private double pickupLng;

    private int couponID;
    private String couponApplied;
    private double couponDiscountAmount;

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    private String basePrice;

    public String getDriverMobile() {
        return driverMobile;
    }

    public void setDriverMobile(String driverMobile) {
        this.driverMobile = driverMobile;
    }

    public String getDriverAuthToken() {
        return driverAuthToken;
    }

    public void setDriverAuthToken(String driverAuthToken) {
        this.driverAuthToken = driverAuthToken;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public String getCustomerAuthToken() {
        return customerAuthToken;
    }

    public void setCustomerAuthToken(String customerAuthToken) {
        this.customerAuthToken = customerAuthToken;
    }

    private String driverMobile;
    private String driverAuthToken;
    private String customerMobile;
    private String customerAuthToken;

    public String getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(String gstAmount) {
        this.gstAmount = gstAmount;
    }

    public String getIgstAmount() {
        return igstAmount;
    }

    public void setIgstAmount(String igstAmount) {
        this.igstAmount = igstAmount;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getDriverArrivalTime() {
        return driverArrivalTime;
    }

    public void setDriverArrivalTime(String driverArrivalTime) {
        this.driverArrivalTime = driverArrivalTime;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getDropTime() {
        return dropTime;
    }

    public void setDropTime(String dropTime) {
        this.dropTime = dropTime;
    }

    private String gstAmount;
    private String igstAmount;
    private String totalTime;
    private String driverArrivalTime;
    private String pickupTime;
    private String dropTime;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ServiceOrderDetails() {

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

    public String getSubCatName() {
        return subCatName;
    }

    public void setSubCatName(String subCatName) {
        this.subCatName = subCatName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public String getFormattedSubTotal() {
        try {
            double value = Double.parseDouble(totalPrice);
            DecimalFormat formatter = new DecimalFormat("₹#,##,##0.00");
            return formatter.format(value);
        } catch (NumberFormatException e) {
            return "₹" + totalPrice;
        }
    }

    public String getFormattedPriceTotal() {
        try {
            double value = Double.parseDouble(totalPrice);
            // Round to nearest integer
            long roundedValue = Math.round(value);

            // Format with Indian currency symbol and thousands separator
            DecimalFormat formatter = new DecimalFormat("₹#,##,##0");
            return formatter.format(roundedValue);
        } catch (NumberFormatException e) {
            return "₹" + totalPrice;
        }
    }


    // Parse from JSONObject
    public static ServiceOrderDetails fromJson(JSONObject json) throws JSONException {
        ServiceOrderDetails details = new ServiceOrderDetails();

        // Basic order info
        details.setOrderId(json.getString("order_id"));
        details.setBookingId(json.getString("booking_id"));
        details.setBookingTiming(json.getString("booking_timing"));
        details.setBookingDate(json.getString("booking_date"));
        details.setBookingStatus(json.getString("booking_status"));



        // Driver details
        details.setDriverId(json.getString("driver_id"));
        details.setDriverName(json.getString("driver_first_name"));
        details.setDriverImage(json.getString("profile_pic"));
        details.setDriverMobile(json.getString("driver_mobile_no"));
        details.setDriverAuthToken(json.getString("goods_driver_auth_token"));

        // Customer details
        details.setCustomerId(json.getString("customer_id"));
        details.setCustomerName(json.getString("customer_name"));
        details.setCustomerMobile(json.getString("customer_mobile_no"));
        details.setCustomerAuthToken(json.getString("customers_auth_token"));

        // Location details
        details.setPickupAddress(json.getString("pickup_address"));
        details.setDropAddress(json.getString("drop_address"));
        details.setPickupLat(Double.parseDouble(json.getString("pickup_lat")));
        details.setPickupLng(Double.parseDouble(json.getString("pickup_lng")));

        details.setDistance(json.getString("distance"));



        // Payment details
        details.setPaymentMethod(json.getString("payment_method"));
        details.setTotalPrice(json.getString("total_price"));
        details.setBasePrice(json.getString("base_price"));
        details.setGstAmount(json.getString("gst_amount"));
        details.setIgstAmount(json.getString("igst_amount"));

        // Time details
        details.setTotalTime(json.getString("total_time"));
        details.setDriverArrivalTime(json.getString("driver_arrival_time"));
        details.setPickupTime(json.getString("pickup_time"));
        details.setDropTime(json.getString("drop_time"));

        // Additional info
        details.setOtp(json.getString("otp"));
        details.setRatings(json.getString("ratings"));
        details.setSubCatName(json.getString("sub_cat_name"));
        details.setServiceName(json.getString("service_name"));
        details.setCouponApplied(json.getString("coupon_applied"));
        details.setCouponID(Integer.parseInt(json.getString("coupon_id")));
        details.setCouponDiscountAmount(Double.parseDouble(json.getString("coupon_amount")));
//        details.set(json.getString("before_coupon_amount"));


        return details;
    }
}

