package com.kapstranspvtltd.kaps.polygon;


public class Point {

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x;
    public double y;

    @Override
    public String toString() {
        return String.format("(%f,%f)", x, y);
    }
}