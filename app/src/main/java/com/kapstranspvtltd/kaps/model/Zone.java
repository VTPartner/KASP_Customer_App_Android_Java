package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Zone{
	@SerializedName("Main_data")
	@Expose
	private MainData mainData;

	@SerializedName("Zone")
	private List<ZoneItem> zones;

	@SerializedName("BannerList")
	private List<String> bannerList;

	@SerializedName("Historyinfo")
	private List<HistoryinfoItem> historyinfo;

	public List<ZoneItem> getZones(){
		return zones;
	}

	public void setZones(List<ZoneItem> zones) {
		this.zones = zones;
	}

	public MainData getMainData() {
		return mainData;
	}

	public void setMainData(MainData mainData) {
		this.mainData = mainData;
	}

	public List<String> getBannerList() {
		return bannerList;
	}

	public void setBannerList(List<String> bannerList) {
		this.bannerList = bannerList;
	}

	public List<HistoryinfoItem> getHistoryinfo() {
		return historyinfo;
	}

	public void setHistoryinfo(List<HistoryinfoItem> historyinfo) {
		this.historyinfo = historyinfo;
	}
}