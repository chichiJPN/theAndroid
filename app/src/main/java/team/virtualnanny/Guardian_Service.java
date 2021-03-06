package team.virtualnanny;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.ArrayList;
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

    private List<CircleOptions> circles;
    private List<PolygonOptions> polygons;

    //	private List<Db_fence> existingFences;
    Map<String, String> childLastFence = new HashMap<String, String>();
    Map<String, DataSnapshot> childrenAlarms = new HashMap<String, DataSnapshot>();
    private String currentUserID;
    private int numSteps = 0;
    boolean busyFlag = false;

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

        initComponents();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot guardian) {

				// retrieves the fences that the guardian made and saves them in variables
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


				// get the children and add a listener to them
				if(guardian.child("children").exists()) {
					for(DataSnapshot snapshotChild : guardian.child("children").getChildren()) {
                        final String childID = snapshotChild.getValue().toString();
						

                        // retrieve data of children
						mDatabase.child("users").child(childID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot childSnapshot) {
								if(childSnapshot.child("alarms").exists()) {
                                    // get alarms then save values in a list
//									alarmSnapshot = childSnapshot.child("alarms");
                                    childrenAlarms.put(childID,childSnapshot.child("alarms"));
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
								Log.d("guardservice","data chaged in child");
								DataSnapshot parent = childSnapshot.child("Parent");
								
								// check first if guardian is the parent of the child
                                if(parent.exists() && parent.getValue().toString().equals(currentUserID)) {

									Db_user childUser = childSnapshot.getValue(Db_user.class);

                                    // checks if child sent an SOS
                                    if(childUser.getsos() == true){
                                        alertSOS(childUser, childID);
                                    } else {
										
									}

                                    // check if child is in a danger zone
                                    checkCurrentLocationOfChild(childUser, childID);
                                }

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

    public void initComponents() {
        // initialize danger sound
        v = (Vibrator) getApplicationContext().getSystemService(getApplicationContext().VIBRATOR_SERVICE);

        circles = new ArrayList<CircleOptions>();
        polygons = new ArrayList<PolygonOptions>();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() { // stop running if it finds that user is not logged in anymore
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //Toast.makeText(getApplicationContext(), "Service is stopped", Toast.LENGTH_SHORT).show();
                    Log.d("Guardian Service","service is stopping");
                    Guardian_Service.this.stopSelf();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // get data of the current user ID
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);

    }

    private void checkCurrentLocationOfChild(Db_user childUser, String childID) {

        float[] distance = new float[2];
        double childPosLatitude = childUser.getLastLatitude();
        double childPosLongitude = childUser.getLastLongitude();

        // check if the guardian created a fence
        if(fenceSnapshot != null) {

            int numFencesChecked = 0;
            int numFences = (int) fenceSnapshot.getChildrenCount();
            String lastFence = childLastFence.get(childID);

            // check if the child is in any fence
            for(DataSnapshot snapshotFence : fenceSnapshot.getChildren() ) {
                Db_fence fence = snapshotFence.getValue(Db_fence.class);
                String fenceName = snapshotFence.getKey();

                // if its circle
                if(fence.getType().equals("Circle")) {
                    Location.distanceBetween( childPosLatitude, childPosLongitude,
                            fence.getLatitude(), fence.getLongitude(), distance);

                    // checks if the child is in a circle
                    if( distance[0] < fence.getRadius()  ){

                        String message = "";

                        if(fence.getSafety() == 1) {
                            message = childUser.getFirstName() + " " + childUser.getLastName() + " has entered " + fenceName + " safety zone. ";
                        } else {
                            message = "DANGER!" + childUser.getFirstName() + " " + childUser.getLastName() + " has entered " + fenceName + " danger zone. ";
                        }


                        // checks if value is already set
                        // this is to prevent the notification from being alerted everytime
                        // the child's data changes
                        // scenario : child just entered the danger zone and is walking around in it
                        if(lastFence != null && lastFence.equals(fenceName)) {
                            Log.d(TAG, "User was already alerted");
                            break;
                        } else {
                            createNotificationForGeoFences(fence, message);
                        }

                        // creates a notification to add to database
                        Db_notification dbNotification = new Db_notification();
                        dbNotification.setStatus("Not read");
                        dbNotification.setTitle(childUser.getFirstName() + " " + childUser.getLastName() + " has entered a zone");

                        // update database
                        childLastFence.put(childID,fenceName);
                        dbNotification.setContent(message);
                        String timestamp = String.valueOf(System.currentTimeMillis());

                        // add notification item to guardian's database notifications
                        mDatabase.child("users").child(currentUserID).child("notifications").child(timestamp).setValue(dbNotification);
                        break;
                    }
                } else {

                    LatLng point = new LatLng(childPosLatitude,childPosLongitude);
                    Log.d("Guardian", fenceName);

                    // check if child is in polygon
                    if(isPointInPolygon(point, fence, snapshotFence)) {
                        String message = "";

                        if(lastFence != null && lastFence.equals(fenceName)) {
                            Log.d(TAG, "User was already alerted");
                            break;
                        } else {
                            if(fence.getSafety() == 1) { message = childUser.getFirstName() + " " + childUser.getLastName() + " has entered " + fenceName + " safety zone. ";
                            } else {
                                message = "DANGER!" + childUser.getFirstName() + " " + childUser.getLastName() + " has entered " + fenceName + " danger zone. ";
                            }
                            createNotificationForGeoFences(fence, message);
                        }
                        // creates a notification to add to database
                        Db_notification dbNotification = new Db_notification();
                        dbNotification.setStatus("Not read");
                        dbNotification.setTitle(childUser.getFirstName() + " " + childUser.getLastName() + " has entered a zone");

                        // update database
                        childLastFence.put(childID,fenceName);
                        dbNotification.setContent(message);
                        String timestamp = String.valueOf(System.currentTimeMillis());

                        // add notification item to guardian's database notifications
                        mDatabase.child("users").child(currentUserID).child("notifications").child(timestamp).setValue(dbNotification);
                        break;
                    }
                }

                numFencesChecked++;
            }

            // if you checked all the fences but the child is not in any of them
            if(numFencesChecked >= numFences) {
                Log.d(TAG, "Child is not in any fence");
                childLastFence.put(childID, null) ;// set the last fence that the child was in to null
            }
        }

    }



    // creates a notification if the child has entered a geo fence
    // alerts if child enters a danger zone
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

    private void alertSOS(Db_user childUser, String childID) {
        v.vibrate(1000);
        Intent intent = new Intent(getApplicationContext(), Guardian_ChildProfileOverviewActivity.class);
        intent.putExtra("fromNotification", true);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);

        Notification notification;
        notification = new NotificationCompat.Builder(Guardian_Service.this)
                .setSmallIcon(R.drawable.fence)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(childUser.getFirstName() + " has sent an SOS! Please assist immediately!")
                .setContentIntent(pIntent)
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        mDatabase.child("users").child(childID).child("sos").setValue(false);
        //startForeground(1337, notification);
        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.notify(1340,notification);

        Db_notification sosNotification = new Db_notification("Not read","SOS "+ childUser.getFirstName(), childUser.getFirstName() + " has sent an SOS! Please assist immediately!");
        String timestamp = String.valueOf(System.currentTimeMillis());

        // add notification item to guardian's database notifications
        mDatabase.child("users").child(currentUserID).child("notifications").child(timestamp).setValue(sosNotification);
    }


    
    public boolean isPointInPolygon(LatLng point, Db_fence fence, DataSnapshot fenceSnap) {

        boolean flag = false;
        Log.d("child point",point.toString());

        Log.d("Fence name", fenceSnap.getKey().toString());

        DataSnapshot latitudeSnapshot = fenceSnap.child("points").child("latitude");
        DataSnapshot longitudeSnapshot = fenceSnap.child("points").child("longitude");
        List<Double> listLatitudes = new ArrayList<Double>();
        List<Double> listLongitudes = new ArrayList<Double>();

        for(DataSnapshot d_latitude : latitudeSnapshot.getChildren()) {
            String latitude = d_latitude.getValue().toString();
            listLatitudes.add(Double.parseDouble(latitude));
        }

        for(DataSnapshot d_longitude : longitudeSnapshot.getChildren()) {
            String longitude = d_longitude.getValue().toString();
            listLongitudes.add(Double.parseDouble(longitude));
        }

        List<LatLng> vertices = new ArrayList<LatLng>();

        for(int x = 0 ;x < listLatitudes.size(); x++) {
            LatLng vertice = new LatLng(listLatitudes.get(x),listLongitudes.get(x));
            vertices.add(vertice);
            Log.d("coordinates",vertice.toString());
        }

        // http://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon
        int i, j;
        boolean c = false;
        int nvert = vertices.size();
        double testy = point.latitude;
        double testx = point.longitude;
        for (i = 0, j = nvert-1; i < nvert; j = i++) {
            if ( ((vertices.get(i).latitude >testy) != (vertices.get(j).latitude>testy)) &&
                (testx < (vertices.get(j).longitude - vertices.get(i).longitude) *
                        (testy-vertices.get(i).latitude) / (vertices.get(j).latitude -vertices.get(i).latitude) + vertices.get(i).longitude) )
            c = !c;
        }

        return c;

    }

    public boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {

        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY)
                || (aX < pX && bX < pX)) {
            return false; // a and b can't both be above or below pt.y, and a or
            // b must be east of pt.x
        }

        double m = (aY - bY) / (aX - bX); // Rise over run
        double bee = (-aX) * m + aY; // y = mx + b
        double x = (pY - bee) / m; // algebra is neat!

        return x > pX;
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
