package com.kapstranspvtltd.kaps.activities.models;

public class AllDriverOrdersModel {

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getBooking_timing() {
        return booking_timing;
    }

    public void setBooking_timing(String booking_timing) {
        this.booking_timing = booking_timing;
    }

    public String getBooking_status() {
        return booking_status;
    }

    public void setBooking_status(String booking_status) {
        this.booking_status = booking_status;
    }

    public String getTotal_price() {
        return total_price;
    }

    public void setTotal_price(String total_price) {
        this.total_price = total_price;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getSub_cat_name() {
        return sub_cat_name;
    }

    public void setSub_cat_name(String sub_cat_name) {
        this.sub_cat_name = sub_cat_name;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTotal_time() {
        return total_time;
    }

    public void setTotal_time(String total_time) {
        this.total_time = total_time;
    }

    public String getPickup_address() {
        return pickup_address;
    }

    public void setPickup_address(String pickup_address) {
        this.pickup_address = pickup_address;
    }

    public String getDrop_address() {
        return drop_address;
    }

    public void setDrop_address(String drop_address) {
        this.drop_address = drop_address;
    }

    private String order_id;
    private String booking_timing;
    private String booking_status;
    private String total_price;
    private String payment_method;
    private String service_name;
    private String sub_cat_name;
    private String distance;
    private String total_time;
    private String pickup_address;
    private String drop_address;


    public AllDriverOrdersModel(String order_id, String booking_timing, String booking_status, String total_price, String payment_method, String service_name, String sub_cat_name, String distance, String total_time, String pickup_address, String drop_address) {
        this.order_id = order_id;
        this.booking_timing = booking_timing;
        this.booking_status = booking_status;
        this.total_price = total_price;
        this.payment_method = payment_method;
        this.service_name = service_name;
        this.sub_cat_name = sub_cat_name;
        this.distance = distance;
        this.total_time = total_time;
        this.pickup_address = pickup_address;
        this.drop_address = drop_address;
    }


}
