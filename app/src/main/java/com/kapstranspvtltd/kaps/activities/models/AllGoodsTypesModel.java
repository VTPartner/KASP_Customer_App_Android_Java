package com.kapstranspvtltd.kaps.activities.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class AllGoodsTypesModel {
    private final int goodsTypeId;
    private final String goodsTypeName;

    // Constructor
    public AllGoodsTypesModel(int goodsTypeId, String goodsTypeName) {
        this.goodsTypeId = goodsTypeId;
        this.goodsTypeName = goodsTypeName;
    }

    // Factory method to create from JSON
    public static AllGoodsTypesModel fromJson(JSONObject json) {
        try {
            return new AllGoodsTypesModel(
                json.getInt("goods_type_id"),
                json.getString("goods_type_name")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to convert to JSON
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("goods_type_id", goodsTypeId);
            json.put("goods_type_name", goodsTypeName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    // Getters
    public int getGoodsTypeId() {
        return goodsTypeId;
    }

    public String getGoodsTypeName() {
        return goodsTypeName;
    }

    // Optional: Override equals and hashCode for proper object comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllGoodsTypesModel that = (AllGoodsTypesModel) o;
        return goodsTypeId == that.goodsTypeId &&
               Objects.equals(goodsTypeName, that.goodsTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goodsTypeId, goodsTypeName);
    }
}