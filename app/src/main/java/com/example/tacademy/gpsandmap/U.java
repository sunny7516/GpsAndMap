package com.example.tacademy.gpsandmap;

import android.location.Location;

import com.squareup.otto.Bus;

/**
 * Created by Tacademy on 2017-01-26.
 */
public class U {
    private static U ourInstance = new U();

    public static U getInstance() {
        return ourInstance;
    }

    private U() {
    }

    Bus bus = new Bus();

    public Bus getBus() {
        return bus;
    }

    double myLat, myLng;
    Location myLocation;

    public double getMyLat() {
        return myLat;
    }

    public void setMyLat(double myLat) {
        this.myLat = myLat;
    }

    public double getMyLng() {
        return myLng;
    }

    public void setMyLng(double myLng) {
        this.myLng = myLng;
    }

    public Location getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(Location myLocation) {
        this.myLocation = myLocation;
    }
}

