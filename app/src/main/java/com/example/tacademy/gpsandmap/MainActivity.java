package com.example.tacademy.gpsandmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    int goType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1. 네트워크 체크 -> GPS On 체크
        // 2. 6.0이냐 아니냐 -> GPS 동의 여부
        // 3. 디텍팅 -> GPS (투트랩으로 체킹) -> 지오코더(GPS <-> 주소) -> 2초이내
        checkGpsOn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (goType == 1) {
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
                } else {

                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
            } else {
                // 6.0이상에서는 퍼미션을 동의 했으므로 바로 실행
                // getAddress();
            }
        } else {
            // 6.0 이하 단말기는 동의가 필요 없으므로 바로 실행
            // getAddress();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_ACCESS_FINE_LOCATION) {
            // 동의
        } else {
            // 미동의
        }

    }
}

