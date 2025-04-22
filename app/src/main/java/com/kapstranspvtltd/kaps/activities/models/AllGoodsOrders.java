package com.kapstranspvtltd.kaps.activities.models;

public class AllGoodsOrders {
    private String order_id;
    private String vehicle_name;
    private String vehicle_image;
    private String booking_timing;
    private String total_time;
    private String total_price;
    private String payment_method;
    private String sender_name;
    private String sender_number;
    private String receiver_name;
    private String receiver_number;
    private String pickup_address;
    private String drop_address;

    public AllGoodsOrders(String order_id, String vehicle_name, String vehicle_image,
                         String booking_timing, String total_time, String total_price,
                         String payment_method, String sender_name, String sender_number,
                         String receiver_name, String receiver_number, String pickup_address,
                         String drop_address) {
        this.order_id = order_id;
        this.vehicle_name = vehicle_name;
        this.vehicle_image = vehicle_image;
        this.booking_timing = booking_timing;
        this.total_time = total_time;
        this.total_price = total_price;
        this.payment_method = payment_method;
        this.sender_name = sender_name;
        this.sender_number = sender_number;
        this.receiver_name = receiver_name;
        this.receiver_number = receiver_number;
        this.pickup_address = pickup_address;
        this.drop_address = drop_address;
    }

    // Getters
    public String getOrder_id() { return order_id; }
    public String getVehicle_name() { return vehicle_name; }
    public String getVehicle_image() { return vehicle_image; }
    public String getBooking_timing() { return booking_timing; }
    public String getTotal_time() { return total_time; }
    public String getTotal_price() { return total_price; }
    public String getPayment_method() { return payment_method; }
    public String getSender_name() { return sender_name; }
    public String getSender_number() { return sender_number; }
    public String getReceiver_name() { return receiver_name; }
    public String getReceiver_number() { return receiver_number; }
    public String getPickup_address() { return pickup_address; }
    public String getDrop_address() { return drop_address; }

    // Setters
    public void setOrder_id(String order_id) { this.order_id = order_id; }
    public void setVehicle_name(String vehicle_name) { this.vehicle_name = vehicle_name; }
    public void setVehicle_image(String vehicle_image) { this.vehicle_image = vehicle_image; }
    public void setBooking_timing(String booking_timing) { this.booking_timing = booking_timing; }
    public void setTotal_time(String total_time) { this.total_time = total_time; }
    public void setTotal_price(String total_price) { this.total_price = total_price; }
    public void setPayment_method(String payment_method) { this.payment_method = payment_method; }
    public void setSender_name(String sender_name) { this.sender_name = sender_name; }
    public void setSender_number(String sender_number) { this.sender_number = sender_number; }
    public void setReceiver_name(String receiver_name) { this.receiver_name = receiver_name; }
    public void setReceiver_number(String receiver_number) { this.receiver_number = receiver_number; }
    public void setPickup_address(String pickup_address) { this.pickup_address = pickup_address; }
    public void setDrop_address(String drop_address) { this.drop_address = drop_address; }
}