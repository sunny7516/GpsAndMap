package com.example.tacademy.gpsandmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    int goType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        U.getInstance().getBus().register(this);
        // 1. 네트워크 체크 -> GPS On 체크
        // 2. 6.0이냐 아니냐 -> GPS 동의 여부
        // 3. 디텍팅 -> GPS (투트랩으로 체킹) -> 지오코더(GPS <-> 주소) -> 2초이내
        checkGpsOn();
    }

    @Override
    protected void onDestroy() {
        //이벤트 수신 등록 삭제
        U.getInstance().getBus().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (goType == 1) {
            goType = 2;
            // GPS 설정하고 돌아온 것이다.
            checkGpsUseOn();    // GPS 사용할래 안사용권한 체크 (6.0이상)
        }
    }

    public void checkGpsOn() {
        String gps =
                android.provider.Settings.Secure.getString(this.getContentResolver(),
                        Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!(gps.matches(".*gps*.") || gps.matches(".*network*."))) {
            // GPS를 사용할 수 없습니다. 설정 화면으로 이동하시겠습니까?
            final SweetAlertDialog alertDialog = new SweetAlertDialog(this);
            alertDialog.setTitle("알림");
            alertDialog.setContentText("GPS를 사용할 수 없습니다. 설정 화면으로 이동하시겠습니까?");
            alertDialog.setConfirmText("예");
            alertDialog.setCancelText("아니오");
            alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                    goType = 1;
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                    // gps 사용 허가 체크 (6.0 이상일 때)
                }
            });
            alertDialog.show();
        } else {
            checkGpsUseOn();
            // 설정되어있다 => 다음단계 이동
        }
    }

    int PERMISSION_ACCESS_FINE_LOCATION = 1;

    public void checkGpsUseOn() {
        // 단말기 버전이 6.0 이상부터 권한을 체크해야 한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // 동의되었다.
                    // getAddress();
                    checkGpsDetectingOn(1);
                } else {

                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
            } else {
                // 동의 후 6.0 이상에서는 퍼미션을 동의 했으므로 바로 실행
                // getAddress();
                checkGpsDetectingOn(2);
            }
        } else {
            // 6.0 이하 단말기는 동의가 필요 없으므로 바로 실행
            // getAddress();
            checkGpsDetectingOn(3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_ACCESS_FINE_LOCATION) {
            // 퍼미션 허가에 대해서 동의를 누르면 호출!
            if (grantResults.length > 0) {
                if (grantResults[0] < 0) {   // 거부
                    // GPS 요청 불가
                    // 미동의
                    checkGpsDetectingOn(5);
                } else {  // 동의
                    checkGpsDetectingOn(4);
                }
            }
            for (String a : permissions) {
                Log.i("GPS", "[S]요청응답 : " + a);
            }
            for (int a : grantResults) {
                Log.i("GPS", "[I]요청응답 : " + a);
            }
        } else {

        }
    }

    public void checkGpsDetectingOn(int type) {
        Log.i("GPS", "타입 : " + type);
        if (type == 2 || type == 4) {    // 6.0 이상에서 GPS 동의자
            // GPS 획득 요청
            startService();
        } else if (type == 3) { // 6.0 이하이므로 권한동의 없이 바로 GPS 요청
            // GPS 획득 요청
            GpsDetecting gps = new GpsDetecting(this, type);
            gps.initLocation();
        } else if (type == 5) {   // 6.0 이상에서 거부
            // 보류
        }
    }

    public void startService() {
        Intent intent = new Intent(this, GpsDetecting.class);
        startService(intent);
    }

    boolean flag;
    // GPS 최신 갱신 내용 수신
    @Subscribe
    public void FinishLoad(Location location) {
        Log.i("GPS", "새로운위치 정보 : " + location.getLatitude() + "," + location.getLongitude());
        Toast.makeText(this, "새로운위치 정보 : " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
        // 주소 획득
        getAddress(location);

        if (!flag) {    // 위치 정보가 한번이라도 왔다는 뜻
            flag = true;
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
    }

    // GPS를 입력받으면 주소 획득
    public void getAddress(Location location) {
        if (location != null) {
            getAddress(location.getLatitude(), location.getLongitude());
        }
    }
    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> addressList = null;
        try {
            // 1은 결과 개수의 MAX
            addressList = geocoder.getFromLocation(lat, lng, 1);
            if (addressList != null && addressList.size() > 0) {
                // 주소 정보 1개 이상 획득이면
                for (Address address : addressList) {
                    Log.i("GPS", address.toString() + address.getThoroughfare()); //getThoroughfare 동단위 정보
                }
                // 새로운위치 정보 : 37.4663367,126.9605719
                // Address[
                // addressLines=[0:"대한민국 서울특별시 관악구 낙성대동 산4-8"],
                // feature=산4-8,
                // admin=서울특별시,
                // sub-admin=null,
                // locality=관악구,
                // thoroughfare=낙성대동,
                // postalCode=null,
                // countryCode=KR,
                // countryName=대한민국,
                // hasLatitude=true,
                // latitude=37.4651426,
                // hasLongitude=true,
                // longitude=126.959815,
                // phone=null,url=null,extras=null]

                //37.4841668,126.9536859,17
            } else {
                // 결과 없음
                Log.i("GPS", "결과 없음");
            }
        } catch (Exception e) {
            Log.i("GPS", "예외 상황");
        }
        Log.i("GPS", "거리" + distance(lat, lng, 37.4841668, 126.9536859, 'K'));
    }

    // 거리 계산 => 두 개의 포인트가 필요(내위치, 스팟)
    public double distance(Location myLocation, Location youLocation, char unit) {
        return distance(myLocation.getLatitude(), myLocation.getLongitude(),
                youLocation.getLatitude(), youLocation.getLongitude(), unit);
    }
    public double distance(double lat1, double lng1, double lat2, double lng2, char unit) {
        double theta = lng2 - lng1;
        double dist =
                Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;  // Miles
        if (unit == 'K') {
            dist = dist * 1.609344;   // Mile -> km 환산
        } else if (unit == 'N') {
            dist = dist * 0.8684; // Mile (Nautal Miles)
        }
        return dist;
    }
    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    public double rad2deg(double rad) {
        return (rad * Math.PI * 180.0);
    }
}


