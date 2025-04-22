package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CategoryDataItem implements Serializable {

	@SerializedName("main_cat_id")
	private int mainCatId;

	@SerializedName("main_cat_title")
	private String mainCatTitle;

	@SerializedName("subcat_data")
	private List<SubcatDataItem> subcatData;

	public int getMainCatId(){
		return mainCatId;
	}

	public String getMainCatTitle(){
		return mainCatTitle;
	}

	public List<SubcatDataItem> getSubcatData(){
		return subcatData;
	}
}