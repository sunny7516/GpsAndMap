package com.example.tacademy.gpsandmap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GpsDetecting extends Service implements LocationListener {

    public GpsDetecting(){
        Log.i("GPS", "GpsDetecting : 생성");
    }

    Context context;
    int type;

    public GpsDetecting(Context context, int type) {
        this.context = context;
        this.type = type;
        Log.i("GPS", "GpsDetecting : 생성");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("GPS", "onStartCommand : 서비스 가동");
        initLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    //=======================================================================
    // variable
    boolean isGPSEnable = false;            // GPS 사용 여부
    boolean isNetworkEnable = false;        // 네트워크 사용여부
    boolean isGetLocation = false;          // GPS 상태값
    boolean isPassiveEnable = false;        //
    Location location;                      // 위치 정보
    double lat, lng;                        // 위도, 경도
    LocationManager locationManager;        // 위치 관리자
    final float MIN_DISTANCE_UPDATE = 10.0f;    // GPS를 갱신하는 최소 이동 거리
    final long MIN_TIME_UPDATE = 1000 * 60 * 1; // GPS를 갱신하는 최소 시간

    //========================================================================
    // GPS start
    public Location initLocation() {
        Log.i("GPS", "getLocation : GPS 획득 시작");
        // 1. 위치 관리자 획득
        if(type == 3){
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        }else{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        // 2. 하드웨어 설정 확인
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isPassiveEnable = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        // 3. 모두 안되면 GG
        if (!isGPSEnable && !isNetworkEnable && !isPassiveEnable) {
            return null;
        }
        // 4. 되긴 된다!
        isGetLocation = true;   // 위치 정보를 받을 수 있다.
        try {
            if (isGPSEnable) {
                if (locationManager != null) {
                    // 위치 정보 업데이트 요청!
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_UPDATE,
                            MIN_DISTANCE_UPDATE,
                            this
                    );
                    // 위치 정보 획득 루트 1
                    // 단말기가 gps 공급자를 통해 마지막으로 측정되었던 gps값을 획득
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        lat = location.getLatitude();   // 위도
                        lng = location.getLongitude();  // 경도
                        sendGps();
                    }
                }
            }
            if (isNetworkEnable) {
                if (locationManager != null) {
                    // 위치 정보 업데이트 요청!
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_UPDATE,
                            MIN_DISTANCE_UPDATE,
                            this
                    );
                    // 위치 정보 획득 루트 1
                    // 단말기가 gps 공급자를 통해 마지막으로 측정되었던 gps값을 획득
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        lat = location.getLatitude();   // 위도
                        lng = location.getLongitude();  // 경도
                        sendGps();
                    }
                }

            }
            if (isPassiveEnable) {
                if (locationManager != null) {
                    // 위치 정보 업데이트 요청!
                    locationManager.requestLocationUpdates(
                            LocationManager.PASSIVE_PROVIDER,
                            MIN_TIME_UPDATE,
                            MIN_DISTANCE_UPDATE,
                            this
                    );
                    // 위치 정보 획득 루트 1
                    // 단말기가 gps 공급자를 통해 마지막으로 측정되었던 gps값을 획득
                    location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    if (location != null) {
                        lat = location.getLatitude();   // 위도
                        lng = location.getLongitude();  // 경도
                        sendGps();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void freeLocation() {
        //관리자에 리스너를 제거하여 디텍팅을 중단한다.
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getRealLat() {
        if (location != null)
            return location.getLatitude();
        return lat;
    }

    public double getRealLng() {
        if (location != null)
            return location.getLongitude();
        return lng;
    }

    public Location getLocation() {
        return location;
    }

    public void sendGps() {
        if (location != null) {
            // 나의 위치정보를 계속해서 업데이트한다.
            U.getInstance().setMyLocation(location);
            U.getInstance().setMyLat(location.getLatitude());
            U.getInstance().setMyLng(location.getLongitude());
            // 나의 위치정보를 특정 연결된 곳으로 보낸다.
            U.getInstance().getBus().post(location);
        }
    }

    // LocationListener start
    // 위치가 변경되면 호출
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            //변경된 위치 정보 세팅
            this.location = location;
            lat = location.getLatitude();   //위도
            lng = location.getLongitude();  //경도
            sendGps();
        }
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
//========================================================================