package com.kapstranspvtltd.kaps.model;

import java.io.Serializable;

public class CategoryT implements Serializable {
    String name;
    String id;

    public CategoryT(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
