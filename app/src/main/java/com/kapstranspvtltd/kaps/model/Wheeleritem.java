package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

public class Wheeleritem {


	private boolean isSelct;


	@SerializedName("after_price")
	private double afterPrice;

	@SerializedName("img")
	private String img;

	@SerializedName("size")
	private String size;

	@SerializedName("start_distance")
	private int startDistance;

	@SerializedName("description")
	private String description;

	@SerializedName("capcity")
	private String capcity;

	@SerializedName("start_price")
	private double startPrice;

	@SerializedName("id")
	private String id;

	@SerializedName("title")
	private String title;

	@SerializedName("is_available")
	private String isAvailable;

	@SerializedName("time_taken")
	private int timeTaken;

	public double getAfterPrice(){
		return afterPrice;
	}

	public String getImg(){
		return img;
	}

	public String getSize(){
		return size;
	}

	public int getStartDistance(){
		return startDistance;
	}

	public String getDescription(){
		return description;
	}

	public String getCapcity(){
		return capcity;
	}

	public double getStartPrice(){
		return startPrice;
	}

	public String getId(){
		return id;
	}

	public String getTitle(){
		return title;
	}

	public String getIsAvailable(){
		return isAvailable;
	}

	public int getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(int timeTaken) {
		this.timeTaken = timeTaken;
	}

	public boolean isSelct() {
		return isSelct;
	}

	public void setSelct(boolean selct) {
		isSelct = selct;
	}
}