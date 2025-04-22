package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mapinfo{

	@SerializedName("package_number")
	@Expose
	private String packageNumber;
	@SerializedName("orderid")
	@Expose
	private String orderid;
	@SerializedName("pick_address")
	@Expose
	private String pickAddress;
	@SerializedName("pick_lat")
	@Expose
	private double pickLat;
	@SerializedName("pick_lng")
	@Expose
	private double pickLng;
	@SerializedName("drop_address")
	@Expose
	private String dropAddress;
	@SerializedName("drop_lat")
	@Expose
	private double dropLat;
	@SerializedName("drop_lng")
	@Expose
	private double dropLng;
	@SerializedName("p_total")
	@Expose
	private String pTotal;
	@SerializedName("order_arrive_seconds")
	@Expose
	private Integer orderArriveSeconds;
	@SerializedName("p_method_name")
	@Expose
	private String pMethodName;
	@SerializedName("p_method_img")
	@Expose
	private String pMethodImg;
	@SerializedName("rider_name")
	@Expose
	private String riderName;
	@SerializedName("rider_img")
	@Expose
	private String riderImg;
	@SerializedName("rider_lats")
	@Expose
	private double riderLats;
	@SerializedName("rider_longs")
	@Expose
	private double riderLongs;
	@SerializedName("rider_mobile")
	@Expose
	private String riderMobile;
	@SerializedName("vehicle_number")
	@Expose
	private String vehicleNumber;
	@SerializedName("v_type")
	@Expose
	private String vType;
	@SerializedName("v_img")
	@Expose
	private String vImg;
	@SerializedName("order_step")
	@Expose
	private Integer orderStep;
	@SerializedName("rider_msg")
	@Expose
	private String riderMsg;
	@SerializedName("rest_msg")
	@Expose
	private String restMsg;
	@SerializedName("o_status")
	@Expose
	private String oStatus;

	@SerializedName("head_msg")
	@Expose
	private String headMsg;

	@SerializedName("sub_msg")
	@Expose
	private String subMsg;


	public String getPackageNumber() {
		return packageNumber;
	}

	public void setPackageNumber(String packageNumber) {
		this.packageNumber = packageNumber;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getPickAddress() {
		return pickAddress;
	}

	public void setPickAddress(String pickAddress) {
		this.pickAddress = pickAddress;
	}

	public double getPickLat() {
		return pickLat;
	}

	public void setPickLat(double pickLat) {
		this.pickLat = pickLat;
	}

	public double getPickLng() {
		return pickLng;
	}

	public void setPickLng(double pickLng) {
		this.pickLng = pickLng;
	}

	public String getDropAddress() {
		return dropAddress;
	}

	public void setDropAddress(String dropAddress) {
		this.dropAddress = dropAddress;
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

	public String getpTotal() {
		return pTotal;
	}

	public void setpTotal(String pTotal) {
		this.pTotal = pTotal;
	}

	public Integer getOrderArriveSeconds() {
		return orderArriveSeconds;
	}

	public void setOrderArriveSeconds(Integer orderArriveSeconds) {
		this.orderArriveSeconds = orderArriveSeconds;
	}

	public String getpMethodName() {
		return pMethodName;
	}

	public void setpMethodName(String pMethodName) {
		this.pMethodName = pMethodName;
	}

	public String getpMethodImg() {
		return pMethodImg;
	}

	public void setpMethodImg(String pMethodImg) {
		this.pMethodImg = pMethodImg;
	}

	public String getRiderName() {
		return riderName;
	}

	public void setRiderName(String riderName) {
		this.riderName = riderName;
	}

	public String getRiderImg() {
		return riderImg;
	}

	public void setRiderImg(String riderImg) {
		this.riderImg = riderImg;
	}

	public double getRiderLats() {
		return riderLats;
	}

	public void setRiderLats(double riderLats) {
		this.riderLats = riderLats;
	}

	public double getRiderLongs() {
		return riderLongs;
	}

	public void setRiderLongs(double riderLongs) {
		this.riderLongs = riderLongs;
	}

	public String getRiderMobile() {
		return riderMobile;
	}

	public void setRiderMobile(String riderMobile) {
		this.riderMobile = riderMobile;
	}

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public String getvType() {
		return vType;
	}

	public void setvType(String vType) {
		this.vType = vType;
	}

	public String getvImg() {
		return vImg;
	}

	public void setvImg(String vImg) {
		this.vImg = vImg;
	}

	public Integer getOrderStep() {
		return orderStep;
	}

	public void setOrderStep(Integer orderStep) {
		this.orderStep = orderStep;
	}

	public String getRiderMsg() {
		return riderMsg;
	}

	public void setRiderMsg(String riderMsg) {
		this.riderMsg = riderMsg;
	}

	public String getRestMsg() {
		return restMsg;
	}

	public void setRestMsg(String restMsg) {
		this.restMsg = restMsg;
	}

	public String getoStatus() {
		return oStatus;
	}

	public void setoStatus(String oStatus) {
		this.oStatus = oStatus;
	}

	public String getHeadMsg() {
		return headMsg;
	}

	public void setHeadMsg(String headMsg) {
		this.headMsg = headMsg;
	}

	public String getSubMsg() {
		return subMsg;
	}

	public void setSubMsg(String subMsg) {
		this.subMsg = subMsg;
	}
}