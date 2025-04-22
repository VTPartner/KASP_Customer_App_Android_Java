package com.kapstranspvtltd.kaps.activities.models;

public class GoodsTypeModel {
    private int goods_type_id;
    private String goods_type_name;

    public GoodsTypeModel(int id, String name) {
        this.goods_type_id = id;
        this.goods_type_name = name;
    }

    public int getId() { return goods_type_id; }
    public String getName() { return goods_type_name; }
}