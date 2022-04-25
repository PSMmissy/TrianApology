package com.example.trainappol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , ActivityCompat.OnRequestPermissionsResultCallback {

    //버튼 변수들
    private AppCompatButton Check_Start;
    private AppCompatButton Check_Termin;

    //키보드 숨기기 변수
    private InputMethodManager imm;

    //뒤로가기 이벤트
    private long backBtnTime = 0;

    //타이머 변수
    private boolean speed_notZero = true;
    private TextView Time;
    private EditText et_TrainNo;
    private TextView Slbtn;
    private TextView Elbtn;
    private AppCompatButton Setbtn;
    private TextView Trkind;

    private long MillisecondTime = 0L;  // 측정 시작 버튼을 누르고 흐른 시간
    private long StartTime = 0L;        // 측정 시작 버튼 누르고 난 이후 부터의 시간
    long TimeBuff = 0L;         // 측정 종료 버튼 눌렀을 때의 총 시간
    long UpdateTime = 0L;       // 측정 종료 버튼 눌렀을 때의 총 시간 + 시작 버튼 누르고 난 이후 부터의 시간 = 총 시간
    private int num;              // 데이터 Sequence
    private double latitude;
    private double longitude;
    private double s, s1;
    private boolean isRunning = false; // 타이머 작동중
    private double lt;
    private double lg;

    private Handler handler;
    private int Sec, Seconds, Minutes, Hour;

    //구글맵 컨트롤러 변수들
    private GoogleMap mMap;
    private Marker currentMarker = null;
    private LocationManager lm;
    private LocationListener ll;
    double mySpeed;

    //상수들
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // 권한 요구에 대한 결과
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocatiion;
    LatLng currentPosition;



    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private final Timer mTimer = new Timer();
    private TimerTask mTimerTask;
    private DatabaseReference mDatabase;
    //Date Types of in
    private long mNow;
    private Date mDate;
    private String mDB;
    private String database_name;
    private final SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private final SimpleDateFormat mFormat2 = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
    private SimpleDateFormat mFormat3 = new SimpleDateFormat("yyMMddhhmmss");

    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)


    //SQLite 변수들
    private DBOpenHelper mDBOpenHelper;
    private Cursor mCursor;
    private InfoClass mInfoClass;
//    private ArrayList<InfoClass> mInfoArray;
    private ArrayList<String> mInfoArray_s;
    private ArrayList<String> mInfoArray_e;
    private String[] arrs;
    private String[] arre;
    private String trkind;


    private SQLiteDatabase db;


    // 프로그래스바
    private ProgressBar progressBar;
    private BackTasking task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        askForPermissions();
        checkPermission2();
        task = new BackTasking();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mDBOpenHelper = new DBOpenHelper(this);
        mDBOpenHelper.open();
        mInit();
        mInfoArray_s = new ArrayList<String>();
        mInfoArray_e = new ArrayList<String>();
        trkind = "";
        Slbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                dlg.setTitle("알림").setMessage("열차번호를 입력하세요.");
                dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dlg.show();
            }
        });
        Elbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                dlg.setTitle("알림").setMessage("열차번호를 입력하세요.");
                dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dlg.show();
            }
        });
        Setbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                trkind = "";
                mInfoArray_s = new ArrayList<String>();
                mInfoArray_e = new ArrayList<String>();
                String TrainNumber = et_TrainNo.getText().toString();
                if (TrainNumber.getBytes().length <= 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("알림").setMessage("열차번호를 입력하세요.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(),"Please, Set TrainNo.",Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else {
                    doWhileCursorToArray();
                    getStringCursor();
                    AlertDialog.Builder alg = new AlertDialog.Builder(MapsActivity.this);

                    Trkind.setText(trkind);
                    //mInfoArray_s.add(0, "출발역선택");
                    Collections.reverse(mInfoArray_e); // 도착역은, 출발역 열차와 역들은 같으나, 도착역을 찾기 쉽게 순서를 바꿔줌.
                    //mInfoArray_e.add(0, "도착역선택");
                    arrs = mInfoArray_s.toArray(new String[mInfoArray_s.size()]);// 출발역 아이템(출발역 명)들
                    arre = mInfoArray_e.toArray(new String[mInfoArray_e.size()]); // 도착역 아이템(도착역 명)들
                    Slbtn.setText(arrs[0]);
                    Elbtn.setText(arre[0]);
                    Slbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                            dlg.setTitle("출발역 선택").setItems(arrs, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Slbtn.setText(arrs[i]);
                                }
                            });
                            dlg.show();

                        }
                    });
                    Elbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                            dlg.setTitle("도착역 선택").setItems(arre, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Elbtn.setText(arre[i]);
                                }
                            });
                            dlg.show();
                        }
                    });
                    Slbtn.setText("출발역");
                    Elbtn.setText("도착역");
                }


                }catch (ArrayIndexOutOfBoundsException e){
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                    dlg.setTitle("알림").setMessage("존재하지 않는 열차번호입니다.");
                    dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dlg.show();
                }
            }
        });



        Time = findViewById(R.id.Time);
        Time.setText("00:00:00");
        et_TrainNo = findViewById(R.id.edit2);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLayout = findViewById(R.id.layout_main);

        //버튼을 뷰와 연결
        Check_Start = findViewById(R.id.btn1);
        Check_Termin = findViewById(R.id.btn2);
        Check_Termin.setEnabled(false);
        handler = new Handler();
        num = 1; //데이터 seq 1번부터 시작
        Check_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Startlocation = Slbtn.getText().toString();
                String EndLocation = Elbtn.getText().toString();
                if (Startlocation.equals("출발역")  || EndLocation.equals("도착역")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("알림").setMessage("출발,도착역을 선택하세요.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(),"Please, Set Start and End Station Name.",Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else if(mInfoArray_s.indexOf(Elbtn.getText().toString()) <= mInfoArray_s.indexOf(Slbtn.getText().toString())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("알림").setMessage("열차의 진행방향 기준으로, \n도착역의 정거장이 출발역의 정거장보다 뒤에 있을 수 없습니다. \n올바르게 선택하여 주세요.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else {
                    try {
                        SQLiteHelper.DATABASE_NAME = mFormat2.format(System.currentTimeMillis()) + ".db";
                        SQLiteHelper myDBHelper = new SQLiteHelper(MapsActivity.this);
                        database_name = SQLiteHelper.DATABASE_NAME;
                        mDB = SQLiteHelper.TABLE_NAME;
                        Check_Termin.setText("일시정지");
                        isRunning = !isRunning;
                        // SystemClock.uptimeMillis()는 디바이스를 부팅한후 부터 쉰 시간을 제외한 밀리초를 반환
                        StartTime = SystemClock.uptimeMillis();
                        handler.postDelayed(runnable, 0);
                        Check_Start.setEnabled(false);
                        Check_Termin.setEnabled(true);
                        Setbtn.setEnabled(false);
//                    Timer timer = new Timer();
                        et_TrainNo.setEnabled(false);
                        Slbtn.setEnabled(false);
                        Elbtn.setEnabled(false);
                        latitude = Double.parseDouble(String.format("%.5f", location.getLatitude()));
                        longitude = Double.parseDouble(String.format("%.5f", location.getLongitude()));
                        db = myDBHelper.getWritableDatabase();
                        mTimerTask = createTimertask();
                        mTimer.schedule(mTimerTask, 0, 1000);
                        Toast.makeText(getApplicationContext(),"측정을 시작합니다.", Toast.LENGTH_SHORT).show();
                    }
                    catch (NullPointerException ne){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setTitle("오류").setMessage("GPS 초기 좌표를 잡는 중입니다. 오류 지속 시, GPS를 끄고 다시 켜 주세요.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
            }
        });
        Check_Termin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRunning = !isRunning;
                Check_Start.setEnabled(true);
                if(isRunning){
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                    dlg.setTitle("알림").setMessage("측정을 종료하시겠습니까?");
                    dlg.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    arrs = new String[0];
                                    arre = new String[0];
                                    Slbtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                                            dlg.setTitle("알림").setMessage("열차번호를 입력하세요.");
                                            dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            });
                                            dlg.show();
                                        }
                                    });
                                    Elbtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder dlg = new AlertDialog.Builder(MapsActivity.this);
                                            dlg.setTitle("알림").setMessage("열차번호를 입력하세요.");
                                            dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            });
                                            dlg.show();
                                        }
                                    });

                                    task.execute();
                                    Toast.makeText(MapsActivity.this, "측정을 종료합니다.", Toast.LENGTH_SHORT).show();
                                    Check_Termin.setText("측정종료");
                                    Time.setText("00:00:00");
                                    Trkind.setText("");
                                    TimeBuff = 0L;
                                    et_TrainNo.setText(null);
                                    Check_Termin.setEnabled(false);
                                    et_TrainNo.setEnabled(true);
                                    Slbtn.setEnabled(true);
                                    Elbtn.setEnabled(true);
                                    Setbtn.setEnabled(true);
                                    speed_notZero = true;
                                    Slbtn.setText("출발역");
                                    Elbtn.setText("도착역");
                                    num = 1;
                                    Hour = 0;
                                    Minutes = 0;
                                    Sec = 0;
                                    Seconds = 0;
                                }
                            });
                    dlg.show();
                    isRunning = !isRunning;
                }
                else{
                    if(Time == null) {
                        return;
                    }
                    else{
                        mTimerTask.cancel();
                        TimeBuff += MillisecondTime;
                        Check_Termin.setText("측정종료");
                        Toast.makeText(MapsActivity.this, "측정을 일시정지합니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                // Runnable 객체 제거
                handler.removeCallbacks(runnable);

            }
        });



        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mySpeed = 0;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new SpeedoActionListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);


    }
    private void mInit(){
        Slbtn = findViewById(R.id.slbtn);
        Elbtn = findViewById(R.id.elbtn);
        Setbtn = findViewById(R.id.btn3);
        Trkind = findViewById(R.id.trkind);
    }


    // 뒤로가기 두번 누르면 종료 이벤트
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        }
        else {
            backBtnTime = curTime;
            Toast.makeText(this, "뒤로가기를 한번더 누르면 앱이 종료됩니다.",Toast.LENGTH_SHORT).show();
        }


    }

    // dp to px
    private float dpToPx(Context context, float dp){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    //키보드 숨기기 이벤트
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
    //내부DB에 저장된 열차정보 중, 출발,도착역을 탐색하기 위한 커서 메서드
    private void doWhileCursorToArray(){
        mCursor = null;
        mCursor = mDBOpenHelper.getStName(et_TrainNo.getText().toString());

        while (mCursor.moveToNext()){
            int index = mCursor.getColumnIndex("info_st_nm");
            mInfoArray_s.add(mCursor.getString(index));
            mInfoArray_e.add(mCursor.getString(index));
        }
        mCursor.close();
    }
    //내부DB에 저장된 열차정보 중, 열차종류를 탐색하기 위한 커서 메서드
    private void getStringCursor(){
        mCursor = null;
        mCursor = mDBOpenHelper.getTrKind(et_TrainNo.getText().toString());
        while (mCursor.moveToNext()){
            int index = mCursor.getColumnIndex("train_kind");
            trkind = mCursor.getString(index);
        }
        mCursor.close();
    }
    //측정용 SQLite 활성화 메소드
    @NonNull
    private TimerTask createTimertask(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mNow = System.currentTimeMillis();
                mDate = new Date(mNow);
                String getTrainNo = et_TrainNo.getText().toString();
                String getStartLocation = Slbtn.getText().toString();
                String getEndLocation = Elbtn.getText().toString();
                double getLatitude = Double.parseDouble(String.format("%.5f", location.getLatitude()));
                double getLongitude = Double.parseDouble(String.format("%.5f", location.getLongitude()));
                double getAltitude = Double.parseDouble(String.format("%.5f", location.getAltitude()));
                String resulttime = String.format("%02d",Hour) + ":" + String.format("%02d",Minutes) + ":" + String.format("%02d", Sec);
                // lat과 long의 변화량이 둘다 0일경우 속도 0으로 판정
                if (lt - getLatitude <= 0.00001 && lg - getLongitude <= 0.00001){
                    s = 0;
                    s1 = 0;
                } else {
                    s = Double.parseDouble(String.format("%.5f", 3.6 * location.getSpeed()));
                    s1 = location.getSpeed() * 3.6;
                }

                double getSpeed = s;
                String getTimes = resulttime;
                // 데이터 타입 자체를 신경쓸것
                double getDistance_per_sec = Double.parseDouble(String.format("%.5f", s1 * 10 / 36));
                String getDatetime = mFormat.format(mDate);
                HashMap<String, Object> result = new HashMap<>();
                //o1_trainNo, o2_startLoc, o3_latitude, o4_longitude, o5_speed, o6_times, o7_distance_per_sec
                if (speed_notZero){
                    result.put("trainNo", getTrainNo);
                    result.put("startLoc", getStartLocation);
                    result.put("endLoc", getEndLocation);
                    result.put("latitude", getLatitude);
                    result.put("longitude", getLongitude);
                    result.put("altitude", getAltitude);
                    result.put("speed", getSpeed);
                    result.put("distance_per_sec", getDistance_per_sec);
                    result.put("times", getTimes);
                    result.put("datetime", getDatetime);//초당 데이터 쓰기!!
                    writeNewUser(db, mDB, num, getTrainNo, getStartLocation, getEndLocation, getLatitude,
                            getLongitude, getAltitude, getSpeed, getTimes, getDistance_per_sec, getDatetime);
                    num++;
                }

                if (getSpeed < 1){
                    speed_notZero = false;
                }else{
                    if (!speed_notZero){
                        result.put("trainNo", getTrainNo);
                        result.put("startLoc", getStartLocation);
                        result.put("endLoc", getEndLocation);
                        result.put("latitude", getLatitude);
                        result.put("longitude", getLongitude);
                        result.put("altitude", getAltitude);
                        result.put("speed", getSpeed);
                        result.put("distance_per_sec", getDistance_per_sec);
                        result.put("times", getTimes);
                        result.put("datetime", getDatetime);//초당 데이터 쓰기!!
                        writeNewUser(db, mDB, num, getTrainNo, getStartLocation, getEndLocation, getLatitude,
                                getLongitude, getAltitude, getSpeed, getTimes, getDistance_per_sec, getDatetime);
                        num++;
                    }
                    speed_notZero = true;
                }
                lt = location.getLatitude();
                lg = location.getLongitude();


            }
        };
        return timerTask;
    }


    //얻어온 값들을 SQLite에 쓰기
    private void writeNewUser (SQLiteDatabase db, String mDB, int num, String trainNo, String startLocation, String endLocation,
                                double latitude, double longitude, double altitude, double speed, String times, double distance_per_sec, String datetime){
        String id = Integer.toString(num);
        String lati = Double.toString(latitude);
        String logi = Double.toString(longitude);
        String alti = Double.toString(altitude);
        String spd = Double.toString(speed);
        String dps = Double.toString(distance_per_sec);
        String INSERT_INTO =  "INSERT INTO " + mDB + "(id, trainNo, startLocation, endLocation, latitude, longitude, altitude, speed, times, distance_per_sec, datetime) "
                + "VALUES(" + id
                + " ,'" + trainNo + "'"
                + " ,'" + startLocation + "'"
                + " ,'" + endLocation + "'"
                + " ,'" + lati + "'"
                + " ,'" + logi + "'"
                + " ,'" + alti + "'"
                + " ,'" + spd + "'"
                + " ,'" + times + "'"
                + " ,'" + dps + "'"
                + " ,'" + datetime + "');";
        db.execSQL(INSERT_INTO);
    }
    //스탑워치 이벤트
    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            // 스탑워치 일시정지 버튼 눌렀을 때의 총 시간 + 시작 버튼 누르고 난 이후 부터의 시간 = 총 시간
            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Sec = Seconds % 60;

            Minutes = Seconds / 60 % 60;

            Hour = Seconds / 3600;

            // TextView에 UpdateTime을 갱신해준다
            String result = String.format("%02d",Hour) + ":" + String.format("%02d",Minutes) + ":" + String.format("%02d", Sec);
            Time.setText(result);

            handler.postDelayed(this, 0);

        }
    };
    //스피드 측정 및, 거리 측정 클래스
    private class SpeedoActionListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location){
            TextView V = findViewById(R.id.V);
            TextView D = findViewById(R.id.D);
            if (location != null){
                mySpeed = 3.6 * location.getSpeed();
                Double Distance = mySpeed * 10 / 36 ;
                //속도
                V.setText(String.format("%.2f", mySpeed) + " km/h");
                D.setText(String.format( "%.2f",Distance) +" m");
                V.setGravity(Gravity.CENTER);
                D.setGravity(Gravity.CENTER);

            }
        }
        //메소드 핸들러 부문
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
            // low_indetion of priod.

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();



        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식됨.)


            startLocationUpdates(); // 3. 위치 업데이트 시작


        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요함. 2가지 경우(3-1, 4-1)가 있음.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해 줘야함.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청. 요청 결과는 onRequestPermissionResult에서 수신됨.
                        ActivityCompat.requestPermissions( MapsActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청 즉각 실행.
                // 요청 결과는 onRequestPermissionResult에서 수신됨.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }



        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 현재 오동작을 해서 주석처리

        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d( TAG, "onMapClick :");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            //map collection priod
            TextView LatLong = findViewById(R.id.latlong);
            TextView Longi = findViewById(R.id.longi);
            TextView Alti = findViewById(R.id.alti);

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + location.getLatitude()
                        + "\n경도:" + location.getLongitude() + "\n고도:" + location.getAltitude();

                Log.d(TAG, "onLocationResult : " + markerSnippet);



                LatLong.setText(String.format("%.3f",location.getLatitude()));
                Longi.setText(String.format("%.3f",location.getLongitude()));
                Alti.setText(String.format("%.3f",location.getAltitude()));


                        //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocatiion = location;
            }


        }

    };



    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);



            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);

        }


    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }




    public String getCurrentAddress(LatLng latlng) {

        //GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "서비스 사용불가", Toast.LENGTH_LONG).show();
            return "서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0);
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);


        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED;

    }



    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드.
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                // 퍼미션을 허용했다면 위치 업데이트 시작.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱 종료.2 가지 경우가 있음.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱 사용가능.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱 사용 가능.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }
    public void checkPermission2(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    // 내부 저장소에 측정한 값을 CSV 파일로 저장하기 위한 클래스
    public class BackTasking extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);
        private Date mDate2;

        // Export CSV의 진행도를 보여주는 ProgressBar
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("데이터 내보내는 중...");
            this.dialog.show();
        }
        // 측정한 값이 저장된 DB를 읽고 커서를 통해 CSV로 옮겨 쓴 후, 지정된 경로에 저장
        //경로: /storage/emulated/0/export
        protected Boolean doInBackground(final String... args) {
            String sltext = Slbtn.getText().toString();
            String eltext = Elbtn.getText().toString();
            String tktext = Trkind.getText().toString();
            String tntext = et_TrainNo.getText().toString();
            mDate2 = new Date(mNow);
            String date = mFormat3.format(mDate2);
            final String CSV = ".csv";
            SQLiteHelper myDBHelper = new SQLiteHelper(MapsActivity.this);
            String currentDBPath = "/data/com.example.trainappol/databases/" + myDBHelper.DATABASE_NAME;
            File dbFile = getDatabasePath(currentDBPath);
            System.out.println(dbFile);
            File exportDir = new File("/storage/emulated/0/export/");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, date + "-" + tntext + "-" + tktext + "-" + sltext + "-" + eltext + CSV);
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
                dbFile.delete();
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
                Toast.makeText(MapsActivity.this, "데이터 저장에 성공하였습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapsActivity.this, "데이터 저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            }
        }
    }



}