package com.osmap.pbfrks.friendstrackerworldwide;

/**
 * A Class designed for a comparison used for sorting double values using a quicksort
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 * @deprecated not used in the actual project
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
