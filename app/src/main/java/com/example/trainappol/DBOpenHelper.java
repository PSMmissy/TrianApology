package com.example.trainappol;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBOpenHelper {
    private final static String TAG = "DataBaseHelper";
    private static String DB_PATH = "";
    private static String DB_NAME = "train_app.db";
    private SQLiteDatabase mDataBase;
    private DataBaseHelper mDBHelper;
    private Context mContext;


    private class DataBaseHelper extends SQLiteOpenHelper {
        public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version ) {
            super(context, DB_NAME, null, 1);

            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
            dataBaseCheck();
        }
        private void dataBaseCheck(){
            File dbFile = new File(DB_PATH + DB_NAME);
            if(!dbFile.exists()){
                dbCopy();
                Log.d(TAG,"Database is copied");
            }
        }
        @Override
        public synchronized void close(){
            if (mDataBase != null){
                mDataBase.close();
            }
            super.close();;
        }
        @Override
        public void onCreate(SQLiteDatabase db){
            Log.d(TAG,"onCreate()");
        }
        @Override
        public void onOpen(SQLiteDatabase db){
            super.onOpen(db);
            Log.d(TAG,"onOpen() : DB Opening!");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            Log.d(TAG,"onUpgrade() : DB Schema Modified and Excuting onCreate()");
        }

        private void dbCopy(){

            try{
                File folder = new File(DB_PATH);
                if(!folder.exists()){
                    folder.mkdir();
                }
                InputStream inputStream = mContext.getAssets().open(DB_NAME);
                String out_filename = DB_PATH + DB_NAME;
                OutputStream outputStream = new FileOutputStream(out_filename);
                byte[] mBuffer = new byte[1024];
                int mLength;
                while((mLength = inputStream.read(mBuffer)) > 0){
                    outputStream.write(mBuffer,0,mLength);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
                Log.d("dbCopy","IOException 발생");
            }
        }

    }
    public DBOpenHelper(Context context){
        this.mContext = context;
    }
    public DBOpenHelper open() throws SQLException{
        mDBHelper = new DataBaseHelper(mContext, DB_NAME, null, 1);
        mDataBase = mDBHelper.getWritableDatabase();
        return this;
    }
    public void close(){
        mDataBase.close();
    }
    public Cursor getStName(String train_No){
        Cursor c = mDataBase.rawQuery("SELECT info_st_nm FROM tb_start_info WHERE train_No = '" + train_No + "'", null);
        return c;
    }
    public Cursor getTrKind(String train_No){
        Cursor c = mDataBase.rawQuery("SELECT train_kind FROM tb_train_master WHERE train_No = '" + train_No + "'", null);
        return c;
    }
}
