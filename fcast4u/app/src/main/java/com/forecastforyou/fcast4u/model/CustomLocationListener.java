package com.forecastforyou.fcast4u.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Jovch on 17-Mar-16.
 */
public class CustomLocationListener implements LocationListener {

    /** On how many refreshes with GPS turned off to toast a suggestion to turn on the GPS */
    public static final int GPS_REMINDER_FREQUENCY = 5;

    private LocationUser activity;
    private LocationManager locationManager;

    private int gpsReminderCounter;

    public CustomLocationListener(Activity activity) {
        this.activity = (LocationUser) activity;
        locationManager = this.activity.getLocationManager();
        gpsReminderCounter = 0;
    }

    @Override
    public void onLocationChanged(Location location) {
        activity.refreshLocation(location.getLongitude(), location.getLatitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            gpsReminderCounter = ++gpsReminderCounter%GPS_REMINDER_FREQUENCY;
            if (gpsReminderCounter == 1) {
                Toast.makeText((Activity) activity, "Enable your GPS for better positioning", Toast.LENGTH_SHORT).show();
            }
        }
        if (ActivityCompat.checkSelfPermission((Context) activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Context) activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return;
        }
        try {
            locationManager.requestLocationUpdates(provider.equals(LocationManager.GPS_PROVIDER) ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        catch (IllegalArgumentException e){
            Toast.makeText((Activity) activity, "Unable to detect your location, please enter the name of your city manually", Toast.LENGTH_LONG).show();
            activity.hideProgressBar();
        }
    }

    /**
     * communicator Design Pattern to be implemented in Activities using CustomLocationListener
     */
    public interface LocationUser{
        void refreshLocation(double longitude, double latitude);
        LocationManager getLocationManager();
        void hideProgressBar();
    }

}
