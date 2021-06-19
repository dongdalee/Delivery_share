package com.example.sns_project.activity;

import com.example.sns_project.PostInfo;
import com.example.sns_project.adapter.GpsTracker;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import com.example.sns_project.R;
import com.example.sns_project.adapter.HomeAdapter;
import com.example.sns_project.fragment.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.sns_project.activity.LocationActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback  {

    // 구글 맵 참조변수 생성
    GoogleMap mMap;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_maps_api);

        // mapFragment에 구글맵 호출
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); // 반드시 메인 thread에 호출

    }

    // <================================= 지도 위 롱클릭 시 활동 =================================>
    // input: googleMap 객체     output: -
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // 구글 맵 객체 생성

        LatLng seoul = new LatLng(37.52487, 126.92723); // 서울에 대한 위치 설정
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,14)); // 카메라 이동


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) { // 지도 위에서 longClick 시
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng); // 마커위치설정
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng)); // 마커생성위치로 카메라 이동
                mMap.addMarker(markerOptions); // 마커 생성

                Double latitude = latLng.latitude; // 위도
                Double longitude = latLng.longitude; // 경도
                Toast.makeText(MapsActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

                address = getCurrentAddress(latitude, longitude); // 인텐트로 넘겨줄 위도, 경도 위치 설정
                Intent mapIntent = new Intent(getApplicationContext(),LocationActivity.class);
                mapIntent.putExtra("사용자 주소1", address);
                startActivity(mapIntent);
            }
        });
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

}

