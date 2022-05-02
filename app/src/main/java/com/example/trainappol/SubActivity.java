package com.example.trainappol;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

public class SubActivity extends AppCompatActivity {

    private FloatingActionButton floatingButton;
    private SQLiteDatabase db;
    private View mLayout;
    private String av = "";
    private ListView listView;
    private String[] itemlist;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int position = -1;
    private BackTasking task;
    private String[] name = new String[5];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_activity);
        mLayout = findViewById(R.id.layout_main);
        listView = findViewById(R.id.listview);
        floatingButton = findViewById(R.id.floatingActionButton);
        DTlogHelper mDTloghelper = new DTlogHelper(SubActivity.this);
        mDTloghelper.open();
        Cursor c = mDTloghelper.getDBNAME();
        while (c.moveToNext()) {
            itemlist = new String[c.getColumnNames().length];
            for (int i = 0; i < c.getColumnNames().length; i++) {
                System.out.println(c.getString(i));
                arrayList.add(c.getString(i));
            }
        }
        Collections.reverse(arrayList);
        adapter = new ArrayAdapter<String>(this, R.layout.memolist_type, arrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                position = i;
            }
        });

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position != -1){
                    AlertDialog.Builder dlg = new AlertDialog.Builder(SubActivity.this);
                    dlg.setTitle("알림").setMessage("해당 측정정보의 csv 파일을 다운로드 하시겠습니까?");
                    dlg.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SQLiteHelper.DATABASE_NAME = arrayList.get(position) + ".db";
                            Cursor c = mDTloghelper.getCSVname(arrayList.get(position));
                            while(c.moveToNext()){
                                for(int j = 0; j < 5; j++){
                                    name[j] = c.getString(j);
                                }
                            }
                            av = name[0] + "-" + name[1] + "-" + name[2] + "-" + name[3] + "-" + name[4] + ".csv";
                            task = new BackTasking();
                            task.execute();
                            position = -1;
                        }
                    }).setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                }
            }
        });
    }


    public class BackTasking extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(SubActivity.this);

        // Export CSV의 진행도를 보여주는 ProgressBar
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("데이터 내보내는 중...");
            this.dialog.show();
        }

        // 측정한 값이 저장된 DB를 읽고 커서를 통해 CSV로 옮겨 쓴 후, 지정된 경로에 저장
        //경로: /storage/emulated/0/export
        protected Boolean doInBackground(final String... args) {
            SQLiteHelper myDBHelper = new SQLiteHelper(SubActivity.this);
            String currentDBPath = "/data/com.example.trainappol/databases/" + SQLiteHelper.DATABASE_NAME;
            File dbFile = getDatabasePath(currentDBPath);
            System.out.println(dbFile);
            File exportDir = new File("/storage/emulated/0/export/");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, av);
            try {
                file.createNewFile();
                // 기존엔 FileWriter 메서드를 사용하였으나, 한글이 깨지는 현상이 발생하여, BufferedWriter를 사용하여, euc-kr 로 인코딩하였음.
                CSVWriter csvWrite = new CSVWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "euc-kr")));
                db = myDBHelper.getWritableDatabase();
                Cursor curCSV = db.rawQuery("select * from " + myDBHelper.TABLE_NAME, null);
                csvWrite.writeNext(curCSV.getColumnNames());
                //커서를 이용하여 DB값 하나하나 탐색한 후, CSV로 옮겨주는 작업
                while (curCSV.moveToNext()) {
                    String arrStr[] = null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for (int i = 0; i < curCSV.getColumnNames().length; i++) {
                        mySecondStringArray[i] = curCSV.getString(i);
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                Log.e("MapsActivity", e.getMessage(), e);
                return false;
            }
        }

        //doInBackground 함수 리턴값이 true라면 저장 성공, false라면 저장 실패.
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                //Toast.makeText(SubActivity.this, "데이터 저장에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                Snackbar.make(mLayout, "내부폴더(export)에 데이터 저장을 성공하였습니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                }).show();
            } else {
                //Toast.makeText(SubActivity.this, "데이터 저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                Snackbar.make(mLayout, "데이터 저장에 실패하였습니다. \n내부폴더 용량 및 네트워크를 확인하세요.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                }).show();
            }
        }
    }
}
