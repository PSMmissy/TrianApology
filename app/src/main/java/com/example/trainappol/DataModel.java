package com.example.trainappol;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DataModel {
    /*
    id= primaryKEY
    autoGenerate -> 자동으로 증가
     */
    @PrimaryKey
    private int id;
    private String title;

    //생성자
    public DataModel(String title){
        this.title = title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public int getId(){
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    /*
    toString 재정의 - 내용 확인하기 위함
     */
    @NonNull
    @Override
    public String toString(){
        final StringBuffer sb = new StringBuffer("DataModel{");
        sb.append("id=").append(id);
        sb.append(",title=").append(title).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
