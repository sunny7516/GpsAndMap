package com.example.tacademy.gpsandmap;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.os.Build.VERSION_CODES.M;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Location location;
        // Add a marker in Sydney and move the camera
        LatLng myPosition = new LatLng(U.getInstance().getMyLat(), U.getInstance().getMyLng());
        // 마킹
        Marker marker =
        mMap.addMarker(new MarkerOptions().position(myPosition).title("내 위치").snippet("우리집")); // 지도 위에 점 찍기
        marker.setTag("자취방이다!!");   // 서버로부터 받은 샵 정보를 넣는다.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12)); // 위치를 중심으로 이동 / 코드 제거 시 위치 상에 표시만

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Log.i("GPS", "구글지도상내위치정보:" + location.getLatitude() +"," + location.getLongitude());
            }
            //구글지도상내위치정보:37.4663367,126.9605719
            //새로운위치 정보 : 37.4663367,126.9605719
        });
        // 지도 클릭
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i("GPS", "내가찍은 위치:"+latLng.latitude + "," + latLng.longitude);
                mMap.addMarker(new MarkerOptions().position(latLng).title("신규위치"));
                CameraPosition MARKER_POS = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(16)
                        .bearing(60)
                        .tilt(30)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(MARKER_POS));
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.i("GPS", "내가길게찍은 위치:"+latLng.latitude + "," + latLng.longitude);
                mMap.addMarker(new MarkerOptions().position(latLng).title("롱규위치"));
                CameraPosition MARKER_POS = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(12)
                        .bearing(-60)
                        .tilt(30)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(MARKER_POS));
            }
        });
        // 마커 클릭
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("GPS", "내가길게찍은 위치:" + marker.getPosition().latitude
                        + "," + marker.getPosition().longitude + "/" + marker.getTag().toString());
                Snackbar.make(null, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
                return false;
            }
        });
        // 반경 표시
        // 마커 변경
        // 마커 이동
    }
}