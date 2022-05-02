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

public class DTlogHelper{
    private static final String TAG = "DatalogDB";
    private static String DB_PATH = "";
    private static String DB_NAME = "datalog.db";
    private DatalogHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "datalog";


    private class DatalogHelper extends SQLiteOpenHelper {
        public DatalogHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, DB_NAME, null, DATABASE_VERSION);
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
            dataBaseCheck();
        }

        private void dataBaseCheck() {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (!dbFile.exists()) {
                dbCopy();
                Log.d(TAG, "Database is copied");
            }
        }

        @Override
        public synchronized void close() {
            if (db != null) {
                db.close();
            }
            super.close();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG,"onCreate()");
        }

        @Override
        public void onOpen(SQLiteDatabase db){
            super.onOpen(db);
            Log.d(TAG,"onOpen() : DB Opening!");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG,"onUpgrade()");
        }

        private void dbCopy() {
            try {
                File folder = new File(DB_PATH);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                InputStream inputStream = context.getAssets().open(DB_NAME);
                String out_filename = DB_PATH + DB_NAME;
                OutputStream outputStream = new FileOutputStream(out_filename);
                byte[] mBuffer = new byte[1024];
                int mLength;
                while ((mLength = inputStream.read(mBuffer)) > 0) {
                    outputStream.write(mBuffer, 0, mLength);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("dbCopy", "IOException 발생");
            }
        }
    }
    public DTlogHelper(Context context){
        this.context = context;
    }
    public DTlogHelper open() throws SQLException {
        dbHelper = new DatalogHelper(context, DB_NAME, null, 1);
        db = dbHelper.getWritableDatabase();
        return this;
    }
    public void close(){
        db.close();
    }

    public Cursor getDBNAME(){
        Cursor c = db.rawQuery("SELECT dbname FROM " + TABLE_NAME, null);
        return c;
    }
    public void dbInsert(String dbname,String Date, String trainNo, String tnkind, String Startst, String Endst){
        db.execSQL("INSERT INTO " + TABLE_NAME +
                "(dbname, date, trainNo, tnKind, startst, endst)" +
                " VALUES('" + dbname + "'"
                +" ,'" + Date + "'"
                +" ," + trainNo
                +" ,'" + tnkind + "' "
                +" ,'" + Startst + "' "
                +" ,'" + Endst + "');");

    }
    public Cursor getCSVname(String dbname){
        Cursor c = db.rawQuery("SELECT date, trainNo, tnKind, startst, endst FROM " + TABLE_NAME + " WHERE dbname = '" +dbname + "'" , null);
        return c;
    }
}
