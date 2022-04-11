package com.example.trainappol;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String trainNo;
    public String startLocation;
    public String endLocation;
    public double latitude;
    public double longitude;
    public double altitude;
    public double speed;
    public String times;
    public double distance_per_sec;
    public String datetime;

    public User(String trainNo, String startLocation, String endLocation, double latitude, double longitude,
                double altitude, double speed, String times, double distance_per_sec, String datetime){
        this.trainNo = trainNo;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.times = times;
        this.distance_per_sec = distance_per_sec;
        this.datetime = datetime;
    }
    public String getTrainNo(){
        return trainNo;
    }

    public void setTrainNo(String trainNo){
        this.trainNo = trainNo;
    }

    public String getStartLocation(){
        return startLocation;
    }

    public void setStartLocation(String startLocation){
        this.startLocation = startLocation;
    }

    public String getEndLocation(){
        return endLocation;
    }

    public void setEndLocation(String endLocation){
        this.endLocation = endLocation;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setLatitude(double latitude){ this.latitude = latitude; }

    public double getLongitude(){ return longitude; }

    public void setLongitude(double longitude){ this.longitude = longitude; }

    public double getAltitude(){ return altitude;}

    public void setAltitude(double altitude){ this.altitude = altitude; }

    public double getSpeed(){
        return speed;
    }

    public void setSpeed(double speed){
        this.speed = speed;
    }

    public String getTimes(){
        return times;
    }

    public void setTimes(String times){
        this.times = times;
    }

    public double getDistance_per_sec(){
        return distance_per_sec;
    }

    public void setDistance_per_sec(double distance_per_sec){
        this.distance_per_sec = distance_per_sec;
    }
    public String getDatetime(){
        return datetime;
    }

    public void setDatetime(String datetime){
        this.datetime = datetime;
    }

    @Override
    public String toString(){
        return "User{" +
                "trainNo ='" + trainNo + '\'' +
                ", startLocation ='" + startLocation + '\'' +
                ", endLocation ='" + endLocation + '\'' +
                ", latitude ='" + latitude + '\'' +
                ", longitude ='" + longitude + '\'' +
                ", altitude ='" + altitude + '\'' +
                ", speed ='" + speed + '\'' +
                ", times ='" + times + '\'' +
                ", distance_per_sec ='" + distance_per_sec + '\'' +
                ", datetime ='" + datetime + '\'' +
                '}';
    }
}
