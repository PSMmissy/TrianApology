package com.example.trainappol

import android.Manifest
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.OnMapReadyCallback
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.appcompat.widget.AppCompatButton
import android.widget.TextView
import android.widget.EditText
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.database.DatabaseReference
import android.os.Bundle
import com.example.trainappol.R
import android.view.WindowManager
import android.content.DialogInterface
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.example.trainappol.MapsActivity
import com.google.android.gms.maps.SupportMapFragment
import com.example.trainappol.MapsActivity.SpeedoActionListener
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.example.trainappol.DataBaseHelper
import android.database.sqlite.SQLiteDatabase
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import android.os.Looper
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.*
import android.location.LocationListener
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.io.IOException
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : FragmentActivity(), OnMapReadyCallback, OnRequestPermissionsResultCallback {
    //버튼 변수들
    private var Check_Start: AppCompatButton? = null
    private var Check_Termin: AppCompatButton? = null

    //타이머 변수
    private var Time: TextView? = null
    private var et_TrainNo: EditText? = null
    private var Slbtn: TextView? = null
    private var Elbtn: TextView? = null
    private var Setbtn: AppCompatButton? = null
    private val Trkind: TextView? = null
    private var MillisecondTime = 0L // 측정 시작 버튼을 누르고 흐른 시간
    private var StartTime = 0L // 측정 시작 버튼 누르고 난 이후 부터의 시간
    var TimeBuff = 0L // 측정 종료 버튼 눌렀을 때의 총 시간
    var UpdateTime = 0L // 측정 종료 버튼 눌렀을 때의 총 시간 + 시작 버튼 누르고 난 이후 부터의 시간 = 총 시간
    private var i // 데이터 Sequence
            = 0
    private var latitude = 0.0
    private var longitude = 0.0
    private var s = 0.0
    private var s1 = 0.0
    private var isRunning = false // 타이머 작동중
    private var handler: Handler? = null
    private var Sec = 0
    private var Seconds = 0
    private var Minutes = 0
    private var Hour = 0

    //구글맵 컨트롤러 변수들
    private var mMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var lm: LocationManager? = null
    private var ll: LocationListener? = null
    var mySpeed = 0.0

    // 권한 요구에 대한 결과
    var needRequest = false

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) // 외부 저장소
    var mCurrentLocatiion: Location? = null
    var currentPosition: LatLng? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var location: Location? = null
    private val mTimer = Timer()
    private var mTimerTask: TimerTask? = null
    private var mDatabase: DatabaseReference? = null

    //Date Types of in
    private var mNow: Long = 0
    private var mDate: Date? = null
    private val mFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    private var mLayout // Snackbar 사용하기 위해서는 View가 필요합니다.
            : View? = null

    // (참고로 Toast에서는 Context가 필요했습니다.)
    private val mContext: Context? = null
    private var arr: Array<String?>
    private val list: MutableList<String?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        Time = findViewById(R.id.Time)
        Time.setText("경과시간:00:00:00")
        et_TrainNo = findViewById(R.id.edit2)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        mLayout = findViewById(R.id.layout_main)

        //버튼을 뷰와 연결
        Check_Start = findViewById(R.id.btn1)
        Check_Termin = findViewById(R.id.btn2)
        Check_Termin.setEnabled(false)
        Slbtn = findViewById(R.id.slbtn)
        Elbtn = findViewById(R.id.elbtn)
        Setbtn = findViewById(R.id.btn3)
        Setbtn.setOnClickListener(View.OnClickListener {
            val TrainNumber = et_TrainNo.getText().toString()
            if (TrainNumber.toByteArray().size <= 0) {
                val builder = AlertDialog.Builder(this@MapsActivity)
                builder.setTitle("알림").setMessage("열차번호를 입력하세요.")
                builder.setPositiveButton("OK") { dialogInterface, i ->
                    Toast.makeText(
                        applicationContext,
                        "Please, Set TrainNo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val alertDialog = builder.create()
                alertDialog.show()
            } else {
                arr = getStation(list!!)
            }
        })
        Slbtn.setOnClickListener(View.OnClickListener {
            val dlg = AlertDialog.Builder(this@MapsActivity)
            dlg.setTitle("출발역 선택")
            dlg.setItems(arr) { dialogInterface, i -> Slbtn.setText(arr[i]) }
            dlg.show()
        })
        handler = Handler()
        Check_Start.setOnClickListener(View.OnClickListener {
            val Startlocation = Slbtn.toString()
            val EndLocation = Elbtn.toString()
            if (Startlocation == "출발역" || EndLocation == "도착역") {
                val builder = AlertDialog.Builder(this@MapsActivity)
                builder.setTitle("알림").setMessage("출발,도착역을 선택하세요.")
                builder.setPositiveButton("OK") { dialogInterface, i ->
                    Toast.makeText(
                        applicationContext,
                        "Please, Set Start and End Station Name.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val alertDialog = builder.create()
                alertDialog.show()
            } else {
                isRunning = !isRunning
                // SystemClock.uptimeMillis()는 디바이스를 부팅한후 부터 쉰 시간을 제외한 밀리초를 반환
                StartTime = SystemClock.uptimeMillis()
                handler!!.postDelayed(runnable, 0)
                Check_Start.setEnabled(false)
                Check_Termin.setEnabled(true)
                val timer = Timer()
                et_TrainNo.setEnabled(false)
                Slbtn.setEnabled(false)
                Elbtn.setEnabled(false)
                mDatabase =
                    FirebaseDatabase.getInstance("https://zippy-elf-341602-default-rtdb.firebaseio.com/").reference
                readUser()
                i = 1 //데이터 seq 1번부터 시작
                latitude = String.format("%.5f", location!!.latitude).toDouble()
                longitude = String.format("%.5f", location!!.longitude).toDouble()
                mTimerTask = createTimertask()
                mTimer.schedule(mTimerTask, 0, 1000)
            }
        })
        Check_Termin.setOnClickListener(View.OnClickListener {
            isRunning = !isRunning
            Check_Start.setEnabled(true)
            if (mTimerTask != null) {
                mTimerTask!!.cancel()
            }
            if (isRunning) {
                Check_Termin.setText("측정종료")
                Time.setText("경과시간:00:00:00")
                isRunning = !isRunning
                TimeBuff = 0L
                et_TrainNo.setText(null)
                Check_Termin.setEnabled(false)
                et_TrainNo.setEnabled(true)
                Slbtn.setEnabled(true)
                Elbtn.setEnabled(true)
                Hour = 0
                Minutes = 0
                Sec = 0
                Seconds = 0
            } else {
                if (Time == null) {
                    return@OnClickListener
                } else {
                    TimeBuff += MillisecondTime
                    Check_Termin.setText("초기화")
                }
            }

            // Runnable 객체 제거
            handler!!.removeCallbacks(runnable)
        })
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS.toLong())
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS.toLong())
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        mySpeed = 0.0
        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        ll = SpeedoActionListener()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        lm!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, ll)
    }

    private fun createTimertask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                mNow = System.currentTimeMillis()
                mDate = Date(mNow)
                val getTrainNo = et_TrainNo!!.text.toString()
                val getStartLocation = Slbtn!!.text.toString()
                val getEndLocation = Elbtn!!.text.toString()
                val getLatitude = String.format("%.5f", location!!.latitude).toDouble()
                val getLongitude = String.format("%.5f", location!!.longitude).toDouble()
                // lat과 long의 변화량이 둘다 0일경우 속도 0으로 판정
                if (latitude - getLatitude == 0.0 && longitude - getLongitude == 0.0) {
                    s = 0.0
                    s1 = 0.0
                } else {
                    s = String.format("%.5f", 3.6 * location!!.speed).toDouble()
                    s1 = location!!.speed * 3.6
                }
                val getSpeed = s
                val getTimes = Time!!.text.toString().substring(5, 13)
                // 데이터 타입 자체를 신경쓸것
                val getDistance_per_sec = String.format("%.5f", s1 * 10 / 36).toDouble()
                val getDatetime = mFormat.format(mDate)
                val result: HashMap<String, Any> = HashMap()
                //o1_trainNo, o2_startLoc, o3_latitude, o4_longitude, o5_speed, o6_times, o7_distance_per_sec
                if (location!!.speed != 0f && getSpeed != 0.0) {
                    result["trainNo"] = getTrainNo
                    result["startLoc"] = getStartLocation
                    result["endLoc"] = getEndLocation
                    result["latitude"] = getLatitude
                    result["longitude"] = getLongitude
                    result["speed"] = getSpeed
                    result["distance_per_sec"] = getDistance_per_sec
                    result["times"] = getTimes
                    result["datetime"] = getDatetime
                    writeNewUser(
                        Integer.toString(i),
                        getTrainNo,
                        getStartLocation,
                        getEndLocation,
                        getLatitude,
                        getLongitude,
                        getSpeed,
                        getTimes,
                        getDistance_per_sec,
                        getDatetime
                    )
                    i++
                    latitude = String.format("%.5f", location!!.latitude).toDouble()
                    longitude = String.format("%.5f", location!!.longitude).toDouble()
                }
            }
        }
    }

    // WHERE train_No = " +"'" + et_TrainNo.getText().toString() + "'"
    fun getStation(list: MutableList<String?>): Array<String?> {
        val dbHelper = DataBaseHelper(this)
        val db = dbHelper.readableDatabase
        var idx = 0
        val cursor = db.rawQuery(
            "SELECT info_st_nm FROM tb_start_info WHERE train_No = '" + et_TrainNo!!.text.toString() + "'",
            null
        )
        cursor.moveToFirst()
        do {
            list.add(cursor.getString(0))
            idx++
        } while (cursor.moveToNext())
        cursor.close()
        return list.toTypedArray()
    }

    private fun writeNewUser(
        userId: String,
        trainNo: String,
        startLocation: String,
        endLocation: String,
        latitude: Double,
        longitude: Double,
        speed: Double,
        times: String,
        distance_per_sec: Double,
        datetime: String
    ) {
        val user = User(
            trainNo,
            startLocation,
            endLocation,
            latitude,
            longitude,
            speed,
            times,
            distance_per_sec,
            datetime
        )
        mDatabase!!.child(Trkind!!.text.toString() + "-" + et_TrainNo!!.text.toString() + "-" + Slbtn!!.text.toString() + "-" + Elbtn!!.text.toString())
            .child(userId).setValue(user)
            .addOnFailureListener { // 데이터 쓰기 실패 시
                Toast.makeText(this@MapsActivity, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun readUser() {
        mDatabase!!.child("users").child("1").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.getValue(User::class.java) != null) {
                    val post = dataSnapshot.getValue(
                        User::class.java
                    )
                    Log.w("FireBaseData", "getData" + post.toString())
                } else {
                    Toast.makeText(this@MapsActivity, "데이터 적재 시작", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FireBaseData", "loadPost: onCancelled", databaseError.toException())
            }
        })
    }

    var runnable: Runnable = object : Runnable {
        override fun run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime
            // 스탑워치 일시정지 버튼 눌렀을 때의 총 시간 + 시작 버튼 누르고 난 이후 부터의 시간 = 총 시간
            UpdateTime = TimeBuff + MillisecondTime
            Seconds = (UpdateTime / 1000).toInt()
            Sec = Seconds % 60
            Minutes = Seconds / 60 % 60
            Hour = Seconds / 3600

            // TextView에 UpdateTime을 갱신해준다
            val result = "경과시간:" + String.format("%02d", Hour) + ":" + String.format(
                "%02d",
                Minutes
            ) + ":" + String.format("%02d", Sec)
            Time!!.text = result
            handler!!.postDelayed(this, 0)
        }
    }

    private inner class SpeedoActionListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            val V = findViewById<TextView>(R.id.V)
            val D = findViewById<TextView>(R.id.D)
            if (location != null) {
                mySpeed = 3.6 * location.speed
                val Distance = mySpeed * 10 / 36
                //속도는 여기입니다
                V.text = """현재속도:
${String.format("%.2f", mySpeed)} km/h"""
                D.text = """1초동안 이동한 거리: 
${String.format("%.2f", Distance)} m"""
            }
        }

        //메소드 핸들러 부문
        override fun onProviderDisabled(provider: String) {
            // TODO Auto-generated method stub
            // low_indetion of priod.
        }

        override fun onProviderEnabled(provider: String) {
            // TODO Auto-generated method stub
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            // TODO Auto-generated method stub
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady :")
        mMap = googleMap

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation()


        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates() // 3. 위치 업데이트 시작
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    REQUIRED_PERMISSIONS[0]
                )
            ) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(
                    mLayout!!, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("확인") { // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(
                            this@MapsActivity, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE
                        )
                    }.show()
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
        mMap!!.uiSettings.isMyLocationButtonEnabled = true
        // 현재 오동작을 해서 주석처리

        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap!!.setOnMapClickListener { Log.d(TAG, "onMapClick :") }
    }

    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            //map collection priod
            val LatLong = findViewById<TextView>(R.id.latlong)
            var i: Int
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                location = locationList[locationList.size - 1]
                //location = locationList.get(0);
                currentPosition = LatLng(location!!.latitude, location!!.longitude)
                val markerTitle = getCurrentAddress(currentPosition!!)
                val markerSnippet = """
                    위도:${location!!.latitude}
                    경도:${location!!.longitude}
                    """.trimIndent()
                Log.d(TAG, "onLocationResult : $markerSnippet")
                LatLong.text = "위도: " + String.format(
                    "%.3f",
                    location!!.latitude
                ) + "\n경도: " + String.format("%.3f", location!!.longitude)

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet)
                mCurrentLocatiion = location
            }
        }
    }

    private fun startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting")
            showDialogForLocationServiceSetting()
        } else {
            val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음")
                return
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
            if (checkPermission()) mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
            if (mMap != null) mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates")
            mFusedLocationClient!!.removeLocationUpdates(locationCallback)
        }
    }

    fun getCurrentAddress(latlng: LatLng): String {

        //GPS를 주소로 변환
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?
        addresses = try {
            geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )
        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(this, "서비스 사용불가", Toast.LENGTH_LONG).show()
            return "서비스 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }
        return if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show()
            "주소 미발견"
        } else {
            val address = addresses[0]
            address.getAddressLine(0).toString()
        }
    }

    fun checkLocationServicesStatus(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun setCurrentLocation(location: Location?, markerTitle: String?, markerSnippet: String?) {
        if (currentMarker != null) currentMarker!!.remove()
        val currentLatLng = LatLng(
            location!!.latitude, location.longitude
        )
        val markerOptions = MarkerOptions()
        markerOptions.position(currentLatLng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        currentMarker = mMap!!.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
        mMap!!.moveCamera(cameraUpdate)
    }

    fun setDefaultLocation() {


        //디폴트 위치, Seoul
        val DEFAULT_LOCATION = LatLng(37.56, 126.97)
        val markerTitle = "위치정보 가져올 수 없음"
        val markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요"
        if (currentMarker != null) currentMarker!!.remove()
        val markerOptions = MarkerOptions()
        markerOptions.position(DEFAULT_LOCATION)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        currentMarker = mMap!!.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f)
        mMap!!.moveCamera(cameraUpdate)
    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private fun checkPermission(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else false
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String>,
        grandResults: IntArray
    ) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            var check_result = true


            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates()
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[0]
                    )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[1]
                    )
                ) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(
                        mLayout!!, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("확인") { finish() }.show()
                } else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(
                        mLayout!!, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("확인") { finish() }.show()
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {
        val builder = AlertDialog.Builder(this@MapsActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            """
    앱을 사용하기 위해서는 위치 서비스가 필요합니다.
    위치 설정을 수정하실래요?
    """.trimIndent()
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }
        builder.setNegativeButton("취소") { dialog, id -> dialog.cancel() }
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음")
                        needRequest = true
                        return
                    }
                }
        }
    }

    companion object {
        //상수들
        private const val TAG = "googlemap_example"
        private const val GPS_ENABLE_REQUEST_CODE = 2001
        private const val UPDATE_INTERVAL_MS = 1000 // 1초
        private const val FASTEST_UPDATE_INTERVAL_MS = 500 // 0.5초

        // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
        private const val PERMISSIONS_REQUEST_CODE = 100
    }
}