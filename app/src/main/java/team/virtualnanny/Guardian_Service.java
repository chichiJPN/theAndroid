package team.virtualnanny;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Guardian_Service extends Service {

    public static double StepSize = 0.6;
    private MyLocationListener myLocationListener;
    private LocationManager myManager;
    private Location lastLocation;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private DatabaseReference userRef;
    private DatabaseReference mDatabase;
	
	private DataSnapshot fenceSnapshot;
	private DataSnapshot alarmSnapshot;
	
	
	private List<Db_fence> existingFences;
	private List<DataSnapshot> existingAlarms;
	
    private String currentUserID;
    private int numSteps = 0;
    boolean busyFlag = false;

    Map<String, String> childLastFence = new HashMap<String, String>(); //
    private MediaPlayer mp = null;
    Vibrator v = null;
    final String TAG = "Guardian Service";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("Guardian service","onCreate");
        super.onCreate();


        // initialize danger sound
        v = (Vibrator) getApplicationContext().getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        /*
        mp = new MediaPlayer();
        mp.setDataSource(R.raw.danger_mp3);
        mp.prepare();
        mp.start();

        mp = MediaPlayer.create(getApplicationContext(), R.raw.danger_mp3);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.start();
        */
//        mp.setVolume(50,50);ZZZZ

        mAuthListener = new FirebaseAuth.AuthStateListener() { // stop running if it finds that user is not logged in anymore
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(getApplicationContext(), "Service is stopped", Toast.LENGTH_SHORT).show();
                    Log.d("Guardian Service","service is stopping");
                    Guardian_Service.this.stopSelf();
                }
            }
        };

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthListener);
		mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

		
		// get data of the current user ID
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot guardian) {
				// retrieves the fences and saves them in variables
                if(guardian.child("Fences").exists()) {
                    fenceSnapshot = guardian.child("Fences");
                }
				
				// initiates the pedometer module
                numSteps = Integer.parseInt(guardian.child("numSteps").getValue().toString());
                createNotificationForStartForeground();
                myLocationListener = new MyLocationListener();
                myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if(!checkLocationPermission()){
                    return;
                }
                myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, myLocationListener);
                myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, myLocationListener);
				// retrieves the fences and saves them in variables
			


				// get the children and add a listener to them
				if(guardian.child("children").exists()) {
					for(DataSnapshot snapshotChild : guardian.child("children").getChildren()) {
                        final String childID = snapshotChild.getValue().toString();
						
						
						mDatabase.child("users").child(childID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot childSnapshot) {
								if(childSnapshot.child("alarms").exists()) {
                                    // get alarms then save values in a list
//									alarmSnapshot = childSnapshot.child("alarms");
								}
							}

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

						
						// adds a listener to child and is triggered each time data is changed
                        mDatabase.child("users").child(childID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot childSnapshot) {
								DataSnapshot parent = childSnapshot.child("Parent");
								
								// checks first if guardian is the parent of the child
                                if(parent.exists() && parent.getValue().toString().equals(currentUserID)) {

									// check if child is in fences
									// if in danger fenc, create a notification
									Db_user childUser = childSnapshot.getValue(Db_user.class);
                                    Log.d(TAG,"A data in child "+childUser.getFirstName()+"changed!");

									float[] distance = new float[2];
									double childPosLatitude = childUser.getLastLatitude();
									double childPosLongitude = childUser.getLastLongitude();
									
									// check if the child is in any fence
									for(DataSnapshot snapshotFence : fenceSnapshot.getChildren() ) {
										Db_fence fence = snapshotFence.getValue(Db_fence.class);
										String fenceName = snapshotFence.getKey();
										Location.distanceBetween( childPosLatitude, childPosLongitude,
																  fence.getLatitude(), fence.getLongitude(), distance);
                                        Log.d(TAG, "distance is " + distance[0]);


                                        // checks if the child is in the circle
										if( distance[0] < fence.getRadius()  ){
                                            String message = "";
                                            Log.d("service", "I am inside fence");

                                            // creates a variable to add to database
                                            Db_notification dbNotification = new Db_notification();
                                            dbNotification.setStatus("Not read");
                                            dbNotification.setTitle(childUser.getFirstName() + " " + childUser.getLastName());



											if(fence.getSafety() == 1) {
												// make noti that child is in circle
                                                Log.d("service", "I am inside safety zone");
                                                message = childUser.getFirstName() + " " + childUser.getLastName() + " has entered " + fenceName + " safety zone. ";
											} else {
                                                Log.d("service", "I am inside danger zone");
                                                message = "DANGER!" + childUser.getFirstName() + " " + childUser.getLastName() + " has entered " + fenceName + " danger zone. ";
//                                                final MediaPlayer mp = MediaPlayer.create(Guardian_Service.this, Settings.System.DEFAULT_ALARM_ALERT_URI);


                                            }

                                            String lastFence = childLastFence.get(childID);

                                            // checks if value is already set
                                            // this is to prevent the notification from being alerted everytime
                                            // the child's data changes
                                            if(lastFence != null && lastFence.equals(fenceName)) {
                                                Log.d(TAG, "User was already alerted");
                                                break;
                                            } else {
                                                createNotificationForGeoFences(fence, message);
                                            }



                                            childLastFence.put(childID,fenceName);

                                            dbNotification.setContent(message);
                                            String timestamp = String.valueOf(System.currentTimeMillis());

                                            // add notification item to guardian's database notifications
                                            mDatabase.child("users").child(currentUserID).child("notifications").child(timestamp).setValue(dbNotification);



                                            break;
										}
									}
									
									// check if there are any alarms that should be rung
                                    /*
									for(DataSnapshot fence : fenceSnapshot.getChildren() ) {
										Fence fence = fence.getValue(Db_fence.class);
										
										Location.distanceBetween( childPosLatitude, childPosLongitude.longitude,
																  fence.getLatitude(), fence.getLongitude(), distance);

										if( distance[0] < fence.getRadius()  ){
											if(fence.getSafety() == 2) {
												// make noti that child is in circle
												Toast.makeText(getBaseContext(), "Inside", Toast.LENGTH_LONG).show();
											}
										}
									}
									*/
									
								}
                            }

                            private void createNotificationForGeoFences(Db_fence fence, String message) {
                                Intent intent = new Intent(getApplicationContext(), Guardian_ChildProfileOverviewActivity.class);
                                intent.putExtra("fromNotification", true);
                                PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);

                                Notification notification;

                                if(fence.getSafety() == 1) {
                                    Log.d(TAG, "Creating low priority");
                                    notification = new NotificationCompat.Builder(Guardian_Service.this)
                                            .setSmallIcon(R.drawable.fence)
                                            .setContentTitle(getResources().getString(R.string.app_name))
                                            .setContentText(message)
                                            .setContentIntent(pIntent)
                                            .setOngoing(false)
                                            .build();
                                } else {
                                    Log.d(TAG, "Creating high priority");
                                    v.vibrate(1000);
                                    notification = new NotificationCompat.Builder(Guardian_Service.this)
                                            .setSmallIcon(R.drawable.fence)
                                            .setContentTitle(getResources().getString(R.string.app_name))
                                            .setContentText(message)
                                            .setContentIntent(pIntent)
                                            .setOngoing(false)
                                            .setDefaults(Notification.DEFAULT_ALL)
                                            .setPriority(Notification.PRIORITY_MAX)
                                            .build();
                                }

                                //startForeground(1337, notification);
                                NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notifyMgr.notify(1338,notification);

                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
						
						
					}
				}
				
                Log.d("Guardian service","i have reached here");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
		

    }

    @Override
    public void onDestroy() {
        Log.d("Guardian service","onDestroy");
        super.onDestroy();
        if (myManager != null && myLocationListener != null) {
            myManager.removeUpdates(myLocationListener);
        }
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }		
    }
    private boolean checkLocationPermission(){
        Log.d("Guardian service","checkLocationPermission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Need permission to use internet", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createNotificationForStartForeground() {
        Log.d("Guardian service","createNotificationForStartForeground");

        Intent intent = new Intent(getApplicationContext(), Guardian_ChildProfileOverviewActivity.class);
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
        Log.d("Guardian service","setLastLocation");
        busyFlag = true;
        float distance = 0;
        if(this.lastLocation != null){
            distance = lastLocation.distanceTo(location);
        }else {
            this.lastLocation = location;
        }

        if(distance < 8) { // if distance walked is less than 25 meters
            numSteps += ((int)(distance / StepSize));
        }

        double lastLatitude = location.getLatitude();
        double lastLongitude = location.getLatitude();

        Map<String, Object> numStepObject = new HashMap<String, Object>(); //
        numStepObject.put("numSteps", numSteps);
        numStepObject.put("lastLatitude", lastLatitude);
        numStepObject.put("lastLongitude", lastLongitude);
        userRef.updateChildren(numStepObject);
        this.lastLocation = location;
        Toast.makeText(Guardian_Service.this, "Number of steps:"+numSteps, Toast.LENGTH_LONG).show();
        busyFlag = false;

    }


    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Guardian service","onLocationChanged");
            if(location == null){
                return;
            }
            //Toast.makeText(Guardian_Service.this, "Latitude:"+location.getLatitude()+"Longitude:"+location.getLongitude(), Toast.LENGTH_LONG).show();
            if (location.getSpeed() * 60 * 60 / 1000 < 10 && location.getSpeed() * 60 * 60 / 1000 > 2 && busyFlag == false) {
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
