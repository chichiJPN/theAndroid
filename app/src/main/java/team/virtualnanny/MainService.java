package team.virtualnanny;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class MainService extends Service {
    private MyLocationListener myLocationListener;
    private LocationManager myManager;
    private Location lastLocation;
    private String coords ="";
    private Calendar lastCoordsDate;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("service","onCreate");
        super.onCreate();
        createNotificationForStartForeground();
        myLocationListener = new MyLocationListener();
        myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!checkLocationPermission()){
            return;
        }
        myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
    }

    @Override
    public void onDestroy() {
        Log.d("service","onDestroy");
        super.onDestroy();
        if (myManager != null && myLocationListener != null) {
            if(!checkLocationPermission()){
                return;
            }
            myManager.removeUpdates(myLocationListener);
        }
        writeToFile();
    }
    private boolean checkLocationPermission(){
        Log.d("service","checkLocationPermission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "need_access_location_and_write_external_storage_permission", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createNotificationForStartForeground() {
        Log.d("service","createNotificationForStartForeground");

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("fromNotification", true);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Is working")
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
        startForeground(1337, notification);
    }
    private void setLastLocation(Location location){
        Log.d("service","setLastLocation");
        float distance = 0;
        if(this.lastLocation != null){
            distance = lastLocation.distanceTo(location);
        }else {
            this.lastLocation = location;
        }
        SharedPreferences sp = getSharedPreferences(MapsActivity.PREFS_FILE_NAME, MODE_PRIVATE);
        double fullDistance = Double.parseDouble(sp.getString("distance", "0"));
        String distanceS = String.valueOf(fullDistance + distance);
        sp.edit().putString("lat", String.valueOf(location.getLatitude()))
                .putString("lng", String.valueOf(location.getLongitude()))
                .putBoolean("newValue", true)
                .putString("distance", distanceS).commit();
        addLatLngToString(new LatLng(location.getLatitude(), location.getLongitude()));
        this.lastLocation = location;
    }

    public static void deleteFile(){
        Log.d("service","deleteFile");
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath(), MapsActivity.LAT_LNG_FILE_NAME);
        if(file.exists()){
            file.delete();
        }
    }
    private void addLatLngToString(LatLng latLng){
        Log.d("service","addLatLngToString");
        if(lastCoordsDate == null){
            lastCoordsDate = Calendar.getInstance();
        }else {
            Calendar calendar = Calendar.getInstance();
            long diffInMs = calendar.getTimeInMillis() - lastCoordsDate.getTimeInMillis();
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
            if(diffInSec < 20){
                return;
            }else {
                lastCoordsDate = calendar;
            }
        }
        String line = getDateFormatted() + " " + latLng.latitude + " " + latLng.longitude;
        coords += "\n"+line;
    }
    private void writeToFile(){
        Log.d("service","writeToFile");
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath());
        dir.mkdirs();
        File file = new File(dir, MapsActivity.LAT_LNG_FILE_NAME);
        try {
//            FileOutputStream f = new FileOutputStream(file);
            FileWriter  pw = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(pw);
            out.write(coords);
            out.close();
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String getDateFormatted() {
        Log.d("service","getDateFormatted");

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return addZeroIfNeed(hour)+":"+addZeroIfNeed(minute)+":"+addZeroIfNeed(second)+" "+addZeroIfNeed(day)+"."+addZeroIfNeed(month)+"."+year;
    }
    private static String addZeroIfNeed(int value){
        if(value < 10){
            return "0" + value;
        }
        return value+"";
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("service","onLocationChanged");
            if(location == null){
                return;
            }
            Toast.makeText(MainService.this, "loc:"+location.getSpeed(), Toast.LENGTH_SHORT).show();
            if (location.getSpeed() * 60 * 60 / 1000 < 10 && location.getSpeed() * 60 * 60 / 1000 > 2) {
                setLastLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
