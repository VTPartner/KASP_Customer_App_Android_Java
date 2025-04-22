package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SubcatDataItem implements Serializable {

	@SerializedName("subcat_id")
	private int subcatId;

	@SerializedName("subcat_title")
	private String subcatTitle;

	@SerializedName("product_data")
	private List<ProductDataItem> productData;

	public int getSubcatId(){
		return subcatId;
	}

	public String getSubcatTitle(){
		return subcatTitle;
	}

	public List<ProductDataItem> getProductData(){
		return productData;
	}
}