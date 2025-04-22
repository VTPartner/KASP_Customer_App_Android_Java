package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

public class LogisticProduct {

    @SerializedName("total")
    private String total;

    @SerializedName("quantity")
    private String quantity;

    @SerializedName("price")
    private String price;

    @SerializedName("id")
    private String id;

    @SerializedName("product_name")
    private String productName;

    public String getTotal(){
        return total;
    }

    public String getQuantity(){
        return quantity;
    }

    public String getPrice(){
        return price;
    }

    public String getId(){
        return id;
    }

    public String getProductName(){
        return productName;
    }
}
