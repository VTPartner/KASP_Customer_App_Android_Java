package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

public class ZoneItem{

	@SerializedName("lng")
	private double lng;

	@SerializedName("lat")
	private double lat;

	public double getLng(){
		return lng;
	}

	public double getLat(){
		return lat;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}
}