package com.example.trainappol;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    public static String DATABASE_NAME;
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "testTB1";
    public static final String COLUMN_ID = "id"; //기본 key
    public static final String COLUMN_TRN = "열차번호"; //열차번호
    public static final String COLUMN_STL = "출발역"; //출발역
    public static final String COLUMN_EDL = "도착역"; // 도착역
    public static final String COLUMN_LTT = "위도"; // 위도
    public static final String COLUMN_LGT = "경도"; //경도
    public static final String COLUMN_ATT = "고도"; // 고도
    public static final String COLUMN_SPEED = "속도"; // 속도
    public static final String COLUMN_TIMES = "경과시간"; //경과시간
    public static final String COLUMN_DPS = "초당이동거리"; //초당 이동거리
    public static final String COLUMN_DATE = "측정시각"; // 현재시각
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
    public Cursor getQuery(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME,null);
        return c;
    }
}
