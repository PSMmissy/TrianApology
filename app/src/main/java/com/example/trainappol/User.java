package com.example.trainappol;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String trainNo;
    public String startLoc;
    public Double lat;
    public Double longt;
    public Double speed;
    public String mvtm;
    public Double mvst;

    public User(){

    }

    public User(String trainNo, String startLoc, Double lat, Double longt, Double speed, String mvtm, Double mvst){
        this.trainNo = trainNo;
        this.startLoc = startLoc;
        this.lat = lat;
        this.longt = longt;
        this.speed = speed;
        this.mvtm = mvtm;
        this.mvst = mvst;
    }
    public String getTrainNo(){
        return trainNo;
    }

    public void setTrainNo(String trainNo){
        this.trainNo = trainNo;
    }

    public String getStartLoc(){
        return startLoc;
    }

    public void setStartLoc(String startLoc){
        this.startLoc = startLoc;
    }

    public Double getLat(){
        return lat;
    }

    public void setLat(Double lat){
        this.lat = lat;
    }

    public Double getLongt(){ return longt; }

    public void setLong(Double longt){ this.longt = longt; }

    public Double getSpeed(){
        return speed;
    }

    public void setSpeed(Double speed){
        this.speed = speed;
    }

    public String getMvtm(){
        return mvtm;
    }

    public void setMvtm(String mvtm){
        this.mvtm = mvtm;
    }

    public Double getMvst(){
        return mvst;
    }

    public void setMvst(Double mvst){
        this.mvst = mvst;
    }

    @Override
    public String toString(){
        return "User{" +
                "trainNo ='" + trainNo + '\'' +
                ", startLoc ='" + startLoc + '\'' +
                ", lat ='" + lat + '\'' +
                ", longt ='" + longt + '\'' +
                ", speed ='" + speed + '\'' +
                ", mvtm ='" + mvtm + '\'' +
                ", mvts ='" + mvst + '\'' +
                '}';
    }
}
