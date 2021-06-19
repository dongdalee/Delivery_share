package com.example.sns_project.activity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.sns_project.R;
import com.example.sns_project.adapter.GpsTracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pedro.library.AutoPermissions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class GPSActivity extends AppCompatActivity
{
    private GpsTracker gpsTracker;
    private Marker currentMarker = null;

    String address;
    TextView textView;
    SupportMapFragment mapFragment;
    GoogleMap map;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_api);
        if (!checkLocationServicesStatus()) { // locationService 상태 확인
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        // <================================= GPS 완료 설정 버튼 클릭 =================================>
        Button gpsChkButton = (Button) findViewById(R.id.gpsChkButton);
        gpsChkButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                gpsTracker = new GpsTracker(GPSActivity.this); // 객체 생성
                double latitude = gpsTracker.getLatitude(); // 현재 latitude 가져옴
                double longitude = gpsTracker.getLongitude(); // 현재 longitude 가져옴

                Toast.makeText(GPSActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

                String address = getCurrentAddress(latitude, longitude); // 설정한 현재 위치 가져와서 변수에 저장
                Intent mapIntent = new Intent(getApplicationContext(),LocationActivity.class); // 인텐트
                mapIntent.putExtra("사용자 주소2", address);
                startActivity(mapIntent);
            }
        });


        // <================================= mapFragment 설정  =================================>
        textView = findViewById(R.id.textView);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                startLocationService();
            }
        });

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AutoPermissions.Companion.loadAllPermissions(this, 100);

    }

    // <================================= 퍼미션 요청의 결과 =================================>
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                //위치 값을 가져올 수 있음
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료. 2 가지 경우
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(GPSActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(GPSActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    // <================================= 런타임 퍼미션 처리 =================================>
    // input: -     output: -
    void checkRunTimePermission(){

        // 위치 퍼미션을 가지고 있는지 체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(GPSActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(GPSActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 이미 퍼미션을 가지고 있다면
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 추후 위치 값을 가져올 수 있음

        } else {  // 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요

            // 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(GPSActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명
                Toast.makeText(GPSActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 사용자에게 퍼미션 요청
                ActivityCompat.requestPermissions(GPSActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);

            } else {
                // 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 진행
                ActivityCompat.requestPermissions(GPSActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    // <================================= 현재 위치 좌표 리턴  =================================>
    // input: 위도와 경도    output: Geocoder 탐색 위치 좌표
    public String getCurrentAddress( double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault()); // geocoder 객체 생성
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) { //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) { // 좌표 문제
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) { // 주소 값이 없는 문제
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0); // 리스트형 변수 addresses에 첫번째 인덱스인 최신 주소를 저장
        return address.getAddressLine(0).toString()+"\n"; // String 형으로 리턴
    }

    // <================================= 위치 서비스 활성화 허용 dialog =================================>
    // input: -     output: -
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(GPSActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() { // 설정 버튼 클릭 시
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS); // 묵시적 인텐트
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() { // 취소 버튼 클릭 시
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel(); // 창 닫기
            }
        });
        builder.create().show();
    }

    // <================================= 위치 서비스 활성화 시 =================================>
    // input: -     output: -
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE: // 사용자가 위치 서비스 활성화 시
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission(); // 퍼미션 체크
                        return;
                    }
                }
                break;
        }
    }

    // <================================= 위치 서비스 활성화 상태 체크 =================================>
    // input: -     output: -
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    // <================================= 위치 서비스 시작 =================================>
    // input: -     output: -
    private void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            // 퍼미션 체크
            int chk1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int chk2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            Location location = null;
            if (chk1 == PackageManager.PERMISSION_GRANTED && chk2 == PackageManager.PERMISSION_GRANTED) {
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else {
                return;
            }

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String msg = "최근 위치 ->  Latitue : " + latitude + "\nLongitude : " + longitude;
                showCurrentLocation(latitude, longitude);
                textView.setText(msg);
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    // <================================= location 변경 시 자동 좌표 변경 =================================>
    // input: -     output: -
    class GPSListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) { // 새로운 주소로 좌표 설정
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String message = "내 위치 -> Latitude : " + latitude + "\nLongitude:" + longitude;
            Log.d("Map", message);
            showCurrentLocation(latitude, longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    // <================================= 현재 좌표에 따른 마커 표시 및 카메라 이동 =================================>
    // input: 경도와 위도     output: -
    private void showCurrentLocation(Double latitude, Double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

        String markerTitle = "내위치";
        String markerSnippet = "위치정보가 확인되었습니다.";

        if (currentMarker != null) currentMarker.remove(); // 현재 마커 삭제

        MarkerOptions markerOptions = new MarkerOptions(); // 마커 객체 생성
        markerOptions.position(curPoint);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = map.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curPoint, 15); // 현재 좌표로 카메라 이동
        map.moveCamera(cameraUpdate);
    }

}