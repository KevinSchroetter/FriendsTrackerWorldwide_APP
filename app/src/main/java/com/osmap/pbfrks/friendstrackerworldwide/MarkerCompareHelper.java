package com.osmap.pbfrks.friendstrackerworldwide;

/**
 * Created by kevin on 27-Jun-17.
 */

public class MarkerCompareHelper {
    private double distance;
    private String description;
    public MarkerCompareHelper(String description, double distance){
        this.description = description;
        this.distance = distance;
    }
    public double getDistance(){
        return this.distance;
    }
    public String getDescription(){
        return this.description;
    }
    public void setDistance(double dist){
        this.distance = dist;
    }
    public void setDescription(String descr){
        this.description = descr;
    }
}
