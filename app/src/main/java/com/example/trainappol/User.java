package com.example.trainappol;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public int TrainNo;
    public String StartLoc;
    public Double Lat;
    public Double Long;
    public Double Speed;
    public String Mvtm;
    public Double Mvst;

    public User(){

    }

    public User(int TrainNo, String StartLoc, Double Lat, Double Long, Double Speed, String Mvtm, Double Mvst){
        this.TrainNo = TrainNo;
        this.StartLoc = StartLoc;
        this.Lat = Lat;
        this.Long = Long;
        this.Speed = Speed;
        this.Mvtm = Mvtm;
        this.Mvst = Mvst;
    }
    public int getTrainNo(){
        return TrainNo;
    }

    public void setTrainNo(int TrainNo){
        this.TrainNo = TrainNo;
    }

    public String getStartLoc(){
        return StartLoc;
    }

    public void setStartLoc(String StartLoc){
        this.StartLoc = StartLoc;
    }

    public Double getLat(){
        return Lat;
    }

    public void setLat(Double Lat){
        this.Lat = Lat;
    }

    public Double getLong(){
        return Long;
    }

    public void setLong(Double Long){
        this.Long = Long;
    }

    public Double getSpeed(){
        return Speed;
    }

    public void setSpeed(Double Speed){
        this.Speed = Speed;
    }

    public String getMvtm(){
        return Mvtm;
    }

    public void setMvtm(String Mvtm){
        this.Mvtm = Mvtm;
    }

    public Double getMvst(){
        return Mvst;
    }

    public void setMvst(Double Mvst){
        this.Mvst = Mvst;
    }

    @Override
    public String toString(){
        return "User{" +
                "TrainNo ='" + TrainNo + '\'' +
                ", StartLoc ='" + StartLoc + '\'' +
                ", Lat ='" + Lat + '\'' +
                ", Long ='" + Long + '\'' +
                ", Speed ='" + Speed + '\'' +
                ", Mvtm ='" + Mvtm + '\'' +
                ", Mvts ='" + Mvst + '\'' +
                '}';
    }
}
