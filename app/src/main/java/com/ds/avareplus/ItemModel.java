package com.ds.avareplus;

import java.util.Scanner;

/**
 * Created by clay d
 */
public class ItemModel {
    
    private String waypoint;
    public String type;
    public String distance;
    public String time;
    public String course;
    public String heading;
    public String wind;
    public String fuel;


    public ItemModel(String waypoint, String type, String distance, String time, String course,
                     String heading, String wind, String fuel) {
        this.waypoint = waypoint;
        this.type = type;
        this.distance = distance;
        this.time = time;
        this.course = course;
        this.heading = heading;
        this.wind = wind;
        this.fuel = fuel;
    }

    public String getWaypoint() {
        return waypoint;
    }
    public String getType() {
        return type;
    }

    public String getDistance() {
        return distance;
    }
    public String getTime() {
        return time;
    }
    public String getCourse() {
        return course;
    }
    public String getHeading() {
        return heading;
    }
    public String getWind() {
        return wind;
    }
    public String getFuel() {
        return fuel;
    }


}
