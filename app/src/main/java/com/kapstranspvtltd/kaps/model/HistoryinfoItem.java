package com.kapstranspvtltd.kaps.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryinfoItem implements Parcelable {

	@SerializedName("transaction_id")
	private String transactionId;

	@SerializedName("wall_amt")
	private String wallAmt;

	@SerializedName("p_method_name")
	private String pMethodName;

	@SerializedName("cou_amt")
	private String couAmt;

	@SerializedName("cat_img")
	private String catImg;

	@SerializedName("orderid")
	private String orderid;

	@SerializedName("cat_name")
	private String catName;



	@SerializedName("order_status")
	private String orderStatus;

	@SerializedName("v_img")
	private String vImg;

	@SerializedName("pick_lat")
	private double pickLat;



	@SerializedName("date_time")
	private String dateTime;

	@SerializedName("pick_lng")
	private double pickLng;

	@SerializedName("subtotal")
	private String subtotal;

	@SerializedName("p_method_img")
	private String pMethodImg;

	@SerializedName("pick_address")
	private String pickAddress;

	@SerializedName("p_total")
	private String pTotal;

	@SerializedName("v_type")
	private String vType;

	@SerializedName("rider_name")
	private String riderName;

	@SerializedName("rider_img")
	private String riderImg;

	@SerializedName("package_number")
	private String packageNumber;

	@SerializedName("parceldata")
	@Expose
	private List<Dropitem> parceldata = null;

	protected HistoryinfoItem(Parcel in) {
		transactionId = in.readString();
		wallAmt = in.readString();
		pMethodName = in.readString();
		couAmt = in.readString();
		catImg = in.readString();
		orderid = in.readString();
		catName = in.readString();

		orderStatus = in.readString();
		vImg = in.readString();
		pickLat = in.readDouble();

		dateTime = in.readString();
		pickLng = in.readDouble();
		subtotal = in.readString();
		pMethodImg = in.readString();
		pickAddress = in.readString();
		pTotal = in.readString();
		vType = in.readString();
		riderName = in.readString();
		riderImg = in.readString();
		packageNumber = in.readString();
	}

	public static final Creator<HistoryinfoItem> CREATOR = new Creator<HistoryinfoItem>() {
		@Override
		public HistoryinfoItem createFromParcel(Parcel in) {
			return new HistoryinfoItem(in);
		}

		@Override
		public HistoryinfoItem[] newArray(int size) {
			return new HistoryinfoItem[size];
		}
	};

	public void setTransactionId(String transactionId){
		this.transactionId = transactionId;
	}

	public String getTransactionId(){
		return transactionId;
	}

	public void setWallAmt(String wallAmt){
		this.wallAmt = wallAmt;
	}

	public String getWallAmt(){
		return wallAmt;
	}

	public void setPMethodName(String pMethodName){
		this.pMethodName = pMethodName;
	}

	public String getPMethodName(){
		return pMethodName;
	}

	public void setCouAmt(String couAmt){
		this.couAmt = couAmt;
	}

	public String getCouAmt(){
		return couAmt;
	}

	public void setCatImg(String catImg){
		this.catImg = catImg;
	}

	public String getCatImg(){
		return catImg;
	}

	public void setOrderid(String orderid){
		this.orderid = orderid;
	}

	public String getOrderid(){
		return orderid;
	}

	public void setCatName(String catName){
		this.catName = catName;
	}

	public String getCatName(){
		return catName;
	}



	public void setOrderStatus(String orderStatus){
		this.orderStatus = orderStatus;
	}

	public String getOrderStatus(){
		return orderStatus;
	}

	public void setVImg(String vImg){
		this.vImg = vImg;
	}

	public String getVImg(){
		return vImg;
	}

	public void setPickLat(double pickLat){
		this.pickLat = pickLat;
	}

	public double getPickLat(){
		return pickLat;
	}



	public void setDateTime(String dateTime){
		this.dateTime = dateTime;
	}

	public String getDateTime(){
		return dateTime;
	}

	public void setPickLng(double pickLng){
		this.pickLng = pickLng;
	}

	public double getPickLng(){
		return pickLng;
	}

	public void setSubtotal(String subtotal){
		this.subtotal = subtotal;
	}

	public String getSubtotal(){
		return subtotal;
	}

	public void setPMethodImg(String pMethodImg){
		this.pMethodImg = pMethodImg;
	}

	public String getPMethodImg(){
		return pMethodImg;
	}

	public void setPickAddress(String pickAddress){
		this.pickAddress = pickAddress;
	}

	public String getPickAddress(){
		return pickAddress;
	}

	public void setPTotal(String pTotal){
		this.pTotal = pTotal;
	}

	public String getPTotal(){
		return pTotal;
	}

	public String getvType() {
		return vType;
	}

	public String getRiderName() {
		return riderName;
	}

	public String getRiderImg() {
		return riderImg;
	}

	public void setRiderName(String riderName) {
		this.riderName = riderName;
	}

	public void setvType(String vType) {
		this.vType = vType;
	}

	public String getPackageNumber() {
		return packageNumber;
	}

	public void setPackageNumber(String packageNumber) {
		this.packageNumber = packageNumber;
	}

	public List<Dropitem> getParceldata() {
		return parceldata;
	}

	public void setParceldata(List<Dropitem> parceldata) {
		this.parceldata = parceldata;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(transactionId);
		dest.writeString(wallAmt);
		dest.writeString(pMethodName);
		dest.writeString(couAmt);
		dest.writeString(catImg);
		dest.writeString(orderid);
		dest.writeString(catName);
		dest.writeString(orderStatus);
		dest.writeString(vImg);
		dest.writeDouble(pickLat);
		dest.writeString(dateTime);
		dest.writeDouble(pickLng);
		dest.writeString(subtotal);
		dest.writeString(pMethodImg);
		dest.writeString(pickAddress);
		dest.writeString(pTotal);
		dest.writeString(vType);
		dest.writeString(riderName);
		dest.writeString(riderImg);
		dest.writeString(packageNumber);
	}
}