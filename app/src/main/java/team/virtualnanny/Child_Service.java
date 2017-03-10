package team.virtualnanny;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Child_Service extends Service {

    public static double StepSize = 0.6;
    private MyLocationListener myLocationListener;
    private LocationManager myManager;
    public Location lastLocation;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference userRef;
    private String currentUserID;
    private int numSteps = 0;
    private int numStepsToday = 0;
    boolean busyFlag = false;
    int LOCATION_UPDATE_INTERVAL = 5 * 1000;  // 5 is the number of seconds
    int LOCATION_HISTORY_UPDATE_INTERVAL = 60 * 1000; // 60 is the number of seconds = 1 minutes
    int historyCounter = 0;

    boolean remoteTracking = true;
    boolean remoteLock = false;

    // for the alarms
    List<AlarmManager> listAlarmManagers;
    List<PendingIntent> listAlarmPendingIntents;
    int alarmIntentID = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("child service","onCreate");
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() { // stop running if it finds that user is not logged in anymore
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //Toast.makeText(getApplicationContext(), "Child Service is stopped", Toast.LENGTH_SHORT).show();
                    Child_Service.this.stopSelf();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);

        listAlarmManagers = new ArrayList<AlarmManager>();
        listAlarmPendingIntents = new ArrayList<PendingIntent>();

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Db_user currentUser = dataSnapshot.getValue(Db_user.class);

                numSteps = currentUser.getNumSteps();
                numStepsToday = currentUser.getNumStepsToday();
                remoteLock = currentUser.getRemoteLock();
                remoteTracking = currentUser.getRemoteTracking();

                createNotificationForStartForeground();
                myLocationListener = new MyLocationListener();
                myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if(!checkLocationPermission()){
                    return;
                }
                myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, 0, myLocationListener);
                myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_INTERVAL, 0, myLocationListener);
                Log.d("child service","the service has started");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // add a listener to child's "remoteLock"
        userRef.child("remoteLock").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot childLockSnapshot) {
                boolean childLock = Boolean.parseBoolean(childLockSnapshot.getValue().toString());

//                Toast.makeText(getApplicationContext(), "Child lock is now " + childLock , Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // add a listener to child's "alarms"
        userRef.child("alarms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot alarmSnapshots) {
                  setAlarms(alarmSnapshots);
//                Toast.makeText(getApplicationContext(), "Remote Tracking is now " + remoteTracking, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void setAlarms(DataSnapshot alarmSnapshots) {

        // cancel all previously set alarms
        for(int x = 0 ;x < listAlarmManagers.size() - 1; x++) {
            AlarmManager alarmManager = listAlarmManagers.get(x);
            PendingIntent pendingIntent = listAlarmPendingIntents.get(x);

            alarmManager.cancel(pendingIntent);
        }
        listAlarmManagers.clear();
        listAlarmPendingIntents.clear();

        // set new alarms with the current snapshot of alarms
        for(DataSnapshot fencealarm : alarmSnapshots.getChildren()) {

            String alarmName = fencealarm.getKey().toString();

            for(DataSnapshot alarm : fencealarm.getChildren()) {

                String alarmType = alarm.getKey().toString();
                Db_alarm db_alarm = alarm.getValue(Db_alarm.class);

                // check if the alarm is enabled and alarm is scheduled for today
                if(db_alarm.getEnable() == true && IsAlarmEnabledForToday(db_alarm)) {

                    // set the 10-before minute notification
                    Calendar alarmTime = Calendar.getInstance();
                    alarmTime.setTimeInMillis(System.currentTimeMillis());
                    alarmTime.set(Calendar.HOUR_OF_DAY, db_alarm.getHour());
                    alarmTime.set(Calendar.MINUTE, db_alarm.getMinute() > 10 ? db_alarm.getMinute() - 10 : db_alarm.getMinute());
                    alarmTime.set(Calendar.SECOND,0);

                    AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent alarmIntent = new Intent(Child_Service.this, AlarmReceiver.class);
                    String alarmMessage= "";
                    Log.d("alarm",alarmTime.toString());
                    if(alarmType.equals("Entering")) {
                        alarmMessage = "Please enter " + alarmName + " in 10 minutes ";
                    } else {
                        alarmMessage = "Please leave " + alarmName + " in 10 minutes ";
                    }

                    alarmIntent.putExtra("message",alarmMessage);
                    alarmIntent.putExtra("alarmName",alarmName);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmIntentID, alarmIntent, 0);

                    // sets the alarm
                    manager.set(AlarmManager.RTC_WAKEUP,alarmTime.getTimeInMillis(),pendingIntent);

                    alarmIntentID++;
                    listAlarmManagers.add(manager);
                    listAlarmPendingIntents.add(pendingIntent);

///////////////////// set the on minute notification ///////////////////////////
                    Calendar alarmTime2 = Calendar.getInstance();
                    alarmTime2.setTimeInMillis(System.currentTimeMillis());
                    alarmTime2.set(Calendar.HOUR_OF_DAY, db_alarm.getHour());
                    alarmTime2.set(Calendar.MINUTE, db_alarm.getMinute() > 10 ? db_alarm.getMinute() - 10 : db_alarm.getMinute());
                    alarmTime2.set(Calendar.SECOND,0);

                    AlarmManager manager2 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent alarmIntent2 = new Intent(Child_Service.this, AlarmReceiver.class);
                    String alarmMessage2 = "";
                    Log.d("alarm",alarmTime.toString());
                    if(alarmType.equals("Entering")) {
                        alarmMessage2 = "Please enter " + alarmName + " now";
                    } else {
                        alarmMessage2 = "Please leave " + alarmName + " now";
                    }

                    alarmIntent2.putExtra("message",alarmMessage2);
                    alarmIntent2.putExtra("alarmName",alarmName);

                    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(), alarmIntentID, alarmIntent2, 0);

                    // sets the alarm
                    manager2.set(AlarmManager.RTC_WAKEUP,alarmTime.getTimeInMillis(),pendingIntent2);

                    alarmIntentID++;
                    listAlarmManagers.add(manager2);
                    listAlarmPendingIntents.add(pendingIntent2);



                }
            }
        }
    }

    private boolean IsAlarmEnabledForToday(Db_alarm db_alarm) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch(day) {
            case Calendar.SUNDAY: return db_alarm.getSunday();
            case Calendar.MONDAY: return db_alarm.getMonday();
            case Calendar.TUESDAY: return db_alarm.getTuesday();
            case Calendar.WEDNESDAY: return db_alarm.getWednesday();
            case Calendar.THURSDAY: return db_alarm.getThursday();
            case Calendar.FRIDAY: return db_alarm.getFriday();
            case Calendar.SATURDAY: return db_alarm.getSaturday();
        }

        return false;
    }

    @Override
    public void onDestroy() {
        Log.d("service","onDestroy");
        super.onDestroy();
        if (myManager != null && myLocationListener != null) {
            myManager.removeUpdates(myLocationListener);
        }

        for(int x = 0 ;x < listAlarmManagers.size() - 1; x++) {
            AlarmManager alarmManager = listAlarmManagers.get(x);
            PendingIntent pendingIntent = listAlarmPendingIntents.get(x);
            alarmManager.cancel(pendingIntent);
        }

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
		
    }

    private boolean checkLocationPermission(){
        Log.d("service","checkLocationPermission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Need permission to use internet", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createNotificationForStartForeground() {
        Log.d("child service","createNotificationForStartForeground");

        Intent intent = new Intent(getApplicationContext(), Child_ChildOverviewActivity.class);
        intent.putExtra("fromNotification", true);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Virtual Nanny Service Started")
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
        startForeground(1337, notification);
    }
    private void setLastLocation(Location location){
        Log.d("child service","setLastLocation");
        busyFlag = true;
        float distance = 0;
        if(this.lastLocation != null){
            distance = lastLocation.distanceTo(location);
        }else {
            this.lastLocation = location;
        }

        if(distance < 8) { // if distance walked is less than 8 meters
            numSteps += ((int)(distance / StepSize));
            numStepsToday += ((int)(distance / StepSize));
        }

        double lastLatitude = location.getLatitude();
        double lastLongitude = location.getLongitude();

        Map<String, Object> numStepObject = new HashMap<String, Object>(); //
        numStepObject.put("numSteps", numSteps);
        numStepObject.put("numStepsToday", numStepsToday);
        numStepObject.put("lastLatitude", lastLatitude);
        numStepObject.put("lastLongitude", lastLongitude);
        userRef.updateChildren(numStepObject); // updates numSteps, latitude and longitude of child

        // update location history of child every x seconds
        if(historyCounter > LOCATION_HISTORY_UPDATE_INTERVAL) {
            Map<String, Object> locationHistory = new HashMap<String, Object>(); //
            locationHistory.put("Latitude", lastLatitude);
            locationHistory.put("Longitude", lastLongitude);
            String timestamp = String.valueOf(System.currentTimeMillis());

            userRef.child("locationHistory").child(timestamp).updateChildren(locationHistory);

//            userRef.child("locationHistory").
            historyCounter = 0;
        }

        this.lastLocation = location;
        Toast.makeText(Child_Service.this, "Number of steps:"+numSteps, Toast.LENGTH_LONG).show();
        busyFlag = false;
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("child service","onLocationChanged");
            if(location == null){
                return;
            }

            historyCounter += LOCATION_UPDATE_INTERVAL;
            //Toast.makeText(Child_Service.this, "Latitude:"+location.getLatitude()+"Longitude:"+location.getLongitude(), Toast.LENGTH_LONG).show();
            // location.getspeed() = 3 m/s
            // 3 m /s * 60 sec * 60 min / 1000 kilometers
            //  180 m/min * 60 min / 1000 kilometers
            // 6480m/h / 1000km
            // 6.48 km / h

            if (location.getSpeed() * 60 * 60 / 1000 < 10 && location.getSpeed() * 60 * 60 / 1000 > 2 && busyFlag == false) {
//            if (busyFlag == false) {
                setLastLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    }

    // this is asynctask

}
