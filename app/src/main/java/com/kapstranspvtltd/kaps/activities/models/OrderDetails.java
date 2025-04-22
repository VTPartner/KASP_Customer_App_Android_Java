package com.kapstranspvtltd.kaps.activities.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetails {
    private String orderId;
    private String bookingId;
    private String bookingTiming;
    private String bookingDate;
    private String bookingStatus;
    private String vehicleId;
    private String vehicleName;
    private String vehicleImage;
    private String vehiclePlateNo;
    private String vehicleFuelType;
    private String driverId;
    private String driverName;
    private String driverImage;
    private String driverMobile;
    private String driverAuthToken;
    private String customerId;
    private String customerName;
    private String customerMobile;
    private String customerAuthToken;
    private String pickupAddress;
    private String dropAddress;
    private double pickupLat;
    private double pickupLng;
    private double dropLat;
    private double dropLng;
    private String distance;
    private String senderName;
    private String senderNumber;
    private String receiverName;
    private String receiverNumber;
    private String paymentMethod;
    private String totalPrice;
    private String basePrice;
    private String gstAmount;
    private String igstAmount;
    private String totalTime;
    private String driverArrivalTime;
    private String pickupTime;
    private String dropTime;
    private String otp;
    private String ratings;
    private String goodsTypeId;
    private String cityId;

    private int couponID;
    private String couponApplied;
    private double couponDiscountAmount;

    public List<DropLocation> getDropLocations() {
        return dropLocations;
    }

    public void setDropLocations(List<DropLocation> dropLocations) {
        this.dropLocations = dropLocations;
    }

    public boolean isMultipleDrops() {
        return isMultipleDrops;
    }

    public void setMultipleDrops(boolean multipleDrops) {
        isMultipleDrops = multipleDrops;
    }

    private List<DropLocation> dropLocations;
    private boolean isMultipleDrops;

    // Constructor
    public OrderDetails() {
        // Empty constructor
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingTiming() {
        return bookingTiming;
    }

    public void setBookingTiming(String bookingTiming) {
        this.bookingTiming = bookingTiming;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVehicleImage() {
        return vehicleImage;
    }

    public void setVehicleImage(String vehicleImage) {
        this.vehicleImage = vehicleImage;
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

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverImage() {
        return driverImage;
    }

    public void setDriverImage(String driverImage) {
        this.driverImage = driverImage;
    }

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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

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

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getGoodsTypeId() {
        return goodsTypeId;
    }

    public void setGoodsTypeId(String goodsTypeId) {
        this.goodsTypeId = goodsTypeId;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
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

    // Helper method to format booking timing
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
    public static OrderDetails fromJson(JSONObject json) throws JSONException {
        OrderDetails details = new OrderDetails();

        // Basic order info
        details.setOrderId(json.getString("order_id"));
        details.setBookingId(json.getString("booking_id"));
        details.setBookingTiming(json.getString("booking_timing"));
        details.setBookingDate(json.getString("booking_date"));
        details.setBookingStatus(json.getString("booking_status"));

        // Vehicle details
        details.setVehicleId(json.getString("vehicle_id"));
        details.setVehicleName(json.getString("vehicle_name"));
        details.setVehicleImage(json.getString("vehicle_image"));
        details.setVehiclePlateNo(json.getString("vehicle_plate_no"));
        details.setVehicleFuelType(json.getString("vehicle_fuel_type"));

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
        details.setDropLat(Double.parseDouble(json.getString("destination_lat")));
        details.setDropLng(Double.parseDouble(json.getString("destination_lng")));
        details.setDistance(json.getString("distance"));

        // Sender and Receiver details
        if (json.has("sender_name") && !json.isNull("sender_name")) {
            details.setSenderName(json.getString("sender_name"));
        }
        if (json.has("sender_number") && !json.isNull("sender_number")) {
            details.setSenderNumber(json.getString("sender_number"));
        }
        if (json.has("receiver_name") && !json.isNull("receiver_name")) {
            details.setReceiverName(json.getString("receiver_name"));
        }
        if (json.has("receiver_number") && !json.isNull("receiver_number")) {
            details.setReceiverNumber(json.getString("receiver_number"));
        }

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
        if (json.has("goods_type_id") && !json.isNull("goods_type_id")) {
            details.setGoodsTypeId(json.getString("goods_type_id"));
        }

        details.setCityId(json.getString("city_id"));
        details.setCouponApplied(json.getString("coupon_applied"));
        details.setCouponID(Integer.parseInt(json.getString("coupon_id")));
        details.setCouponDiscountAmount(Double.parseDouble(json.getString("coupon_amount")));

        // Parse multiple drops for goods service
//        details.isMultipleDrops = json.optBoolean("multiple_drops", false);
        int multipleDrops = json.optInt("multiple_drops", 1);
        if(multipleDrops > 1){
            details.isMultipleDrops = true;
        }
        if (details.isMultipleDrops) {
            try {
                // Parse the string representations of JSON arrays into actual JSONArrays
                String dropLocationsStr = json.optString("drop_locations");
                String dropContactsStr = json.optString("drop_contacts");

                JSONArray dropsArray = new JSONArray(dropLocationsStr);
                JSONArray contactsArray = new JSONArray(dropContactsStr);

                System.out.println("dropsArray::" + dropsArray);
                System.out.println("contactsArray::" + contactsArray);

                if (dropsArray != null && contactsArray != null) {
                    List<DropLocation> drops = new ArrayList<>();
                    for (int i = 0; i < dropsArray.length(); i++) {
                        JSONObject dropJson = dropsArray.getJSONObject(i);
                        JSONObject contactJson = contactsArray.getJSONObject(i);

                        DropLocation drop = new DropLocation();
                        drop.setAddress(dropJson.getString("address"));
                        drop.setLatitude(dropJson.getDouble("lat"));
                        drop.setLongitude(dropJson.getDouble("lng"));

                        // Parse contact details
                        drop.setSenderName(contactJson.getString("name")); // Note: changed from "sender_name" to "name"
                        drop.setSenderNumber(contactJson.getString("mobile")); // Note: changed from "sender_number" to "mobile"

                        // For the last drop location, use the receiver details from the main order
                        if (i == dropsArray.length() - 1) {
                            drop.setReceiverName(json.optString("receiver_name"));
                            drop.setReceiverNumber(json.optString("receiver_number"));
                        } else {
                            // For intermediate drops, use the next contact as receiver
                            JSONObject nextContact = contactsArray.getJSONObject(i + 1);
                            drop.setReceiverName(nextContact.getString("name"));
                            drop.setReceiverNumber(nextContact.getString("mobile"));
                        }

                        drop.setCompleted(true); // Since this is order details, all drops are completed

                        drops.add(drop);
                    }
                    details.setDropLocations(drops);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("Error parsing drop locations: " + e.getMessage());
            }
        }
        return details;
    }
}