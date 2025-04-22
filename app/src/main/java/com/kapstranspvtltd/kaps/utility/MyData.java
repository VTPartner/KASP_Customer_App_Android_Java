package com.kapstranspvtltd.kaps.utility;

public class MyData {
    private String id;
    private String name;
    private String price;
    private int count;

    public MyData(String id, String name,String price, int count) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }
}