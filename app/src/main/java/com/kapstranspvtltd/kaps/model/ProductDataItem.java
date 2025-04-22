package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProductDataItem implements Serializable {

	@SerializedName("product_id")
	private String productId;

	@SerializedName("product_price")
	private String productPrice;

	@SerializedName("product_title")
	private String productTitle;

	public String getProductId(){
		return productId;
	}

	public String getProductPrice(){
		return productPrice;
	}

	public String getProductTitle(){
		return productTitle;
	}
}