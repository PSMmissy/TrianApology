package com.example.trainappol;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    public static String DATABASE_NAME;
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "testTB1";
    public static final String COLUMN_ID = "id"; //기본 key
    public static final String COLUMN_TRN = "trainNo"; //열차번호
    public static final String COLUMN_STL = "startLocation"; //출발역
    public static final String COLUMN_EDL = "endLocation"; // 도착역
    public static final String COLUMN_LTT = "latitude"; // 위도
    public static final String COLUMN_LGT = "longitude"; //경도
    public static final String COLUMN_ATT = "altitude"; // 고도
    public static final String COLUMN_SPEED = "speed"; // 속도
    public static final String COLUMN_TIMES = "times"; //경과시간
    public static final String COLUMN_DPS = "distance_per_sec"; //초당 이동거리
    public static final String COLUMN_DATE = "datetime"; // 현재시각
    private static final String DATABASE_CREATE_TEAM = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY "
            + "," + COLUMN_TRN + " INTEGER"
            + "," + COLUMN_STL + " TEXT"
            + "," + COLUMN_EDL + " TEXT"
            + "," + COLUMN_LTT + " TEXT"
            + "," + COLUMN_LGT + " TEXT"
            + "," + COLUMN_ATT + " TEXT"
            + "," + COLUMN_SPEED + " TEXT"
            + "," + COLUMN_TIMES + " TEXT"
            + "," + COLUMN_DPS + " TEXT"
            + "," + COLUMN_DATE + " TEXT);";

    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(DATABASE_CREATE_TEAM);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
