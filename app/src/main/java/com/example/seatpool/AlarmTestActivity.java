package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class AlarmTestActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMyLocationClickListener {


    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private EditText addressEdit;
    private Button addressBtn;
    private Geocoder geocoder;
    LocationManager locationManager;

    double mLatitude;  //위도
    double mLongitude; //경도


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_test);
        addressEdit = findViewById(R.id.alarmTestActivityEditText);
        addressBtn = findViewById(R.id.alarmTestActivityButton);
        //LocationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        fragmentManager = getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
        //GPS가 켜져있는지 체크
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
            finish();
        }

        //마시멜로 이상이면 권한 요청하기
        if (Build.VERSION.SDK_INT >= 23) {
            //권한이 없는 경우
            if (ContextCompat.checkSelfPermission(AlarmTestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(AlarmTestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AlarmTestActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            //권한이 있는 경우
            else {
                requestMyLocation();
            }
        }
        //마시멜로 아래
        else {
            requestMyLocation();
        }


    }

    //권한 요청후 응답 콜백
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //ACCESS_COARSE_LOCATION 권한
        if (requestCode == 1) {
            //권한받음
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestMyLocation();
            }
            //권한못받음
            else {
                Toast.makeText(this, "권한없음", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //나의 위치 요청
    public void requestMyLocation() {
        if (ContextCompat.checkSelfPermission(AlarmTestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(AlarmTestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //요청
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, locationListener);
    }

    //위치정보 구하기 리스너
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (ContextCompat.checkSelfPermission(AlarmTestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(AlarmTestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //나의 위치를 한번만 가져오기 위해
            locationManager.removeUpdates(locationListener);

            //위도 경도
            mLatitude = location.getLatitude();   //위도
            mLongitude = location.getLongitude(); //경도

            //맵생성
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
            //콜백클래스 설정
            mapFragment.getMapAsync(AlarmTestActivity.this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("gps", "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);

        /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                mOptions.title("마커 좌표");
                Double latitude = point.latitude;
                Double longitude = point.longitude;
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));
                // 마커(핀) 추가
                googleMap.addMarker(mOptions);
            }
        });*/

        addressBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = addressEdit.getText().toString();
                List<Address> addressList = null;
                try {
                    // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressList = geocoder.getFromLocationName(
                            str, // 주소
                            10); // 최대 검색 결과 개수
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addressList != null) {
                    String city = "";
                    String country = "";
                    if (addressList.size() == 0) {
                    } else {
                        Address address = addressList.get(0);
                        double lat = address.getLatitude();
                        double lon = address.getLongitude();
                        LatLng point = new LatLng(lat, lon);
                        // 마커 생성
                        MarkerOptions mOptions2 = new MarkerOptions();
                        mOptions2.title("search result");
                        mOptions2.snippet(address.toString());
                        mOptions2.position(point);
                        // 마커 추가
                        mMap.addMarker(mOptions2);
                        // 해당 좌표로 화면 줌
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
                    }
                }

                /*System.out.println(addressList.get(0).toString());
                // 콤마를 기준으로 split
                String []splitStr = addressList.get(0).toString().split(",");
                String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
                System.out.println(address);

                String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
                System.out.println(latitude);
                System.out.println(longitude);*/

                // 좌표(위도, 경도) 생성

            }
        });
        ////////////////////

        // Add a marker in Sydney and move the camera
        LatLng Ajou = new LatLng(37.283508398373634, 127.04653195560451);
        //mMap.addMarker(new MarkerOptions().position(Ajou).title("Marker in Ajou"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Ajou,16));

        /*this.mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //나의 위치 설정
        LatLng position = new LatLng(mLatitude , mLongitude);

        //화면중앙의 위치와 카메라 줌비율
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));*/

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
        mMap.setMyLocationEnabled(true);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongitude), 17);
        /*mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(@NonNull Location location) {
                double d1=location.getLatitude();
                double d2=location.getLongitude();
                Log.e("onMyLocationChange", d1 + "," + d2);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1, d2), 16));

            }
        });*/



    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }


        /*LatLng SEOUL = new LatLng(37.2713928798554, 126.953651271199);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("노준석");
        markerOptions.snippet("같이놀아줘 제발");
        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL,17));*/



}