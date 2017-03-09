package team.virtualnanny;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Guard;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Guardian_ChildProfileOverviewActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final String TAG = "GuardChildProfOverview";
    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progress;
    private String currentlySelectedUserID = null; // id of the currently selected child
    private String currentUserNumber = null;       // phone number of currently selected child
    private String guardianID; // id of guardian


    private DatabaseReference mDatabase;
    private DatabaseReference currentUserReference;// reference that points to the data of the child
    private ValueEventListener valueEventListener;

    // below variables are for calculating the geofences
    private Marker childMarker;  // marker points where child's last location is
    private List<MapFence> mapFences;

    // below variables are for history Location
    private List<Marker> historyLocationMarkers;
    private List<Polyline> historyLocationLines;
    private List<Long> historyLocationTimes; // value is in milliseconds


    // UI Components
    Switch switch_phonelock ;
    Switch switch_locationAccess;
    private ImageButton btn_phone;
    private ImageButton btn_message;
    private ImageButton btn_fence;
    private ImageButton btn_limit;
    private ImageButton btn_alarm;
    private ImageButton btn_dashboard;
    private ImageButton btn_history;
    private ImageButton btn_task;
    private RelativeLayout layout;
    private TextView tv_name;
    private TextView tv_address;
    private TextView textview_steps;
    private LinearLayout panel_header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_child_profile_overview);
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
 //           return;
  //      }


        mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user == null) {
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					finish();
					startActivity(intent);
				}
            }
        };

        requestPermissionAccessLocation();

        Intent intent = new Intent(getApplicationContext(), Guardian_Service.class);
        startService(intent);

        initComponents();
        setClickListeners();
        getAndSetDataListeners();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void setClickListeners() {

        btn_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlySelectedUserID == null) {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "Please select a child",Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri number = Uri.parse("tel:"+ currentUserNumber);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);

            }
        });

        btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlySelectedUserID == null) {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "Please select a child",Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", currentUserNumber);
                smsIntent.putExtra("sms_body", "Body of Message");
                startActivity(smsIntent);
            }
        });

        btn_fence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlySelectedUserID == null) {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "Please select a child",Toast.LENGTH_SHORT).show();
                    return;
                }
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_SetFenceActivity.class);
                startActivity(i);
            }
        });
        btn_limit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlySelectedUserID == null) {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "Please select a child",Toast.LENGTH_SHORT).show();
                    return;
                }
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_SetLimitActivity.class);
                i.putExtra("userid",currentlySelectedUserID);

                startActivity(i);
            }
        });
        btn_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlySelectedUserID == null) {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "Please select a child",Toast.LENGTH_SHORT).show();
                    return;
                }
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_SetAlarmActivity.class);
                i.putExtra("userid",currentlySelectedUserID);
                startActivity(i);
            }
        });
        btn_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlySelectedUserID == null) {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "Please select a child",Toast.LENGTH_SHORT).show();
                    return;
                }
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_SetTasksActivity.class);
                i.putExtra("userid",currentlySelectedUserID);
                startActivity(i);
            }
        });
        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlySelectedUserID == null) {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "Please select a child",Toast.LENGTH_SHORT).show();
                    return;
                }

                // checks if there are markers on the map
                if(!historyLocationMarkers.isEmpty()) {
                    removeHistoryMarkersAndLines();
                    return;
                }

                setHistoryMarkersandLines();

            }
        });

        btn_dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlySelectedUserID == null) {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "Please select a child",Toast.LENGTH_SHORT).show();
                    return;
                }
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_DashboardActivity.class);
                i.putExtra("userid",currentlySelectedUserID);
                startActivity(i);
            }
        });

        switch_phonelock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean booleanLock) {
                if(currentlySelectedUserID == null) {
                    return;
                }
                mDatabase.child("users").child(currentlySelectedUserID).child("remoteLock").setValue(booleanLock);
            }
        });

        switch_locationAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean booleanLock) {
                if(currentlySelectedUserID == null) {
                    return;
                }
                mDatabase.child("users").child(currentlySelectedUserID).child("remoteTracking").setValue(booleanLock);
            }
        });

        // adds swiping features to the layout
        layout.setOnTouchListener(new OnSwipeTouchListener(Guardian_ChildProfileOverviewActivity.this) {
            public void onSwipeTop() {}

            // left to right
            public void onSwipeRight() {
                Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_MenuDrawerActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.left2right_enter, R.anim.left2right_exit);
            }

            public void onSwipeLeft() {}

            public void onSwipeBottom() {}
        });


    }

    private void getAndSetDataListeners() {
        // gets data of the guardian
        mDatabase.child("users").child(guardianID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if(dataSnapshot.child("children").exists()) {
                    progress.show();
                    Log.d("GuardianOverview", "Children exists");
                    for (DataSnapshot snapshot : dataSnapshot.child("children").getChildren()) {

                        final String childID = snapshot.getValue().toString();
                        Log.d("GuardianOverview", "ChildID is " + childID);
                        Log.d("GuardianOverview", "GuardianID is " + guardianID);
                        // gets the parent of the child
                        mDatabase.child("users").child(childID).child("Parent").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot parent) {


                                // checks if the parent exists and if the child's parent is the logged in user
                                if(parent.exists() && parent.getValue().toString().equals(guardianID)) {
                                    Log.d("Guardian_overview","im in here");

                                    ImageView ii = new ImageView(Guardian_ChildProfileOverviewActivity.this);
                                    ii.setBackgroundResource(R.drawable.profile_child1);
                                    ii.setTag(childID);
                                    // adds a click listener when an image at the top is clicked
                                    ii.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            currentlySelectedUserID = (String)v.getTag();
                                            currentUserReference = mDatabase.child("users").child(currentlySelectedUserID);

                                            progress.show();
                                            removeHistoryMarkersAndLines();
                                            refreshPageData();
                                        }
                                    });
                                    panel_header.addView(ii);
                                }
                                progress.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                } else {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "No Children",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void refreshPageData() {
        if(currentUserReference != null) {
            currentUserReference.removeEventListener(valueEventListener);
        }
        currentUserReference.addValueEventListener(valueEventListener);
    }

    private void setHistoryMarkersandLines() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7); // set the calendar to 7 days ago
        String date7daysAgo = ""+cal.getTimeInMillis();

        progress.show();
        mDatabase.child("users").child(currentlySelectedUserID).child("locationHistory").orderByKey().startAt(date7daysAgo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot locationHistorySnapshot) {

                for(DataSnapshot locationHistory: locationHistorySnapshot.getChildren()) {
                    String timestamp = locationHistory.getKey();

                    Double latitude = Double.parseDouble(locationHistory.child("Latitude").getValue().toString());
                    Double longitude = Double.parseDouble(locationHistory.child("Longitude").getValue().toString());

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(timestamp)); // set the time according to locationhistory timestamp

                    LatLng newLatLng = new LatLng(latitude, longitude);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(newLatLng)
                            .title(calendar.getTime().toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                    final Marker historyLocationMarker = mMap.addMarker(markerOptions);

                    historyLocationMarkers.add(historyLocationMarker);
//                            historyLocationMarkers = new Marker();
/*
                            int mYear = calendar.get(Calendar.YEAR);
                            int mMonth = calendar.get(Calendar.MONTH);
                            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                            */
                }


                // this part is for drawing lines between the markers
                // loop through all history location markers
                // and draw a line between two markers
                int totalHistoryMarkers = historyLocationMarkers.size() - 1;
                for(int x = 0; x < totalHistoryMarkers; x++) {

                    // check if there is still a next available marker in the list
                    if(x + 1 < totalHistoryMarkers) {

                        Marker marker1 = historyLocationMarkers.get(x);
                        Marker marker2 = historyLocationMarkers.get(x + 1);

                        PolylineOptions options = new PolylineOptions().add(marker1.getPosition(),marker2.getPosition())
                                .width(5)
                                .color(Color.BLUE);
                        Polyline line = mMap.addPolyline(options);
                        historyLocationLines.add(line);
                    }
                }

                Log.d(TAG, "Count of children is "+locationHistorySnapshot.getChildrenCount());
                progress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initComponents() {
        // initializes and sets all components from xml
        btn_phone = (ImageButton) findViewById(R.id.btn_phone);
        btn_message = (ImageButton) findViewById(R.id.btn_message);
        btn_fence = (ImageButton) findViewById(R.id.btn_fence);
        btn_limit = (ImageButton) findViewById(R.id.btn_limit);
        btn_alarm = (ImageButton) findViewById(R.id.btn_alarm);
        btn_dashboard = (ImageButton) findViewById(R.id.btn_dashboard);
        btn_history = (ImageButton) findViewById(R.id.btn_history);
        btn_task = (ImageButton) findViewById(R.id.btn_task);
        layout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        switch_phonelock = (Switch) findViewById(R.id.switch_phonelock);
        switch_locationAccess = (Switch) findViewById(R.id.switch_locationaccess);
        tv_name = (TextView) findViewById(R.id.textview_name);
        tv_address = (TextView) findViewById(R.id.textview_address);
        textview_steps = (TextView) findViewById(R.id.textview_steps);
        panel_header = (LinearLayout) findViewById(R.id.panel_header);

        // initializes the Lists
        mapFences = new ArrayList<MapFence>();
        /*
        existingFences = new ArrayList<Db_fence>();
        existingMarkers = new ArrayList<Marker>();
        existingCircles = new ArrayList<Circle>();
        */

        historyLocationMarkers = new ArrayList<Marker>();
        historyLocationLines = new ArrayList<Polyline>();

        // instantiates the loading screen
        progress = new ProgressDialog(Guardian_ChildProfileOverviewActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        mDatabase = FirebaseDatabase.getInstance().getReference();		// gets a reference to the database
        mAuth = FirebaseAuth.getInstance();

        guardianID = mAuth.getCurrentUser().getUid();

        // this listener listens if data changes on regarding the child
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Child overview" , "data changed");

                Db_user user = dataSnapshot.getValue(Db_user.class);
                currentUserNumber = user.getPhone();
                switch_phonelock.setChecked(user.getRemoteLock());
                switch_locationAccess.setChecked(user.getRemoteTracking());
                String firstName = user.getFirstName();
                String lastName = user.getLastName();
                String address = user.getAddress();

                int numSteps = user.getNumSteps();

                tv_name.setText(firstName + " "+ lastName);
                tv_address.setText(address);
                textview_steps.setText("" +numSteps + " steps");
                animateAndZoomToLocation(user.getLastLatitude(), user.getLastLongitude());
                childMarker.setTitle(firstName + "" + lastName);
                progress.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }


    // replaces fences on current map with fresh data from database
    private void refreshFences() {
        mDatabase.child("users").child(guardianID).child("Fences").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot SnapshotFences) {

                for(MapFence mapfence : mapFences) {
                    if(mapfence.getDb_fence().getType().equals("Circle")) {
                        mapfence.getCircle().remove();
                    } else {
                        mapfence.getPolygon().remove();
                    }
                    mapfence.getMarker().remove();
                }

                mapFences.clear();
                // checks if the guardian created any fences then saves the in variables
                if(SnapshotFences.exists()) {
                    for(DataSnapshot fenceSnapshot : SnapshotFences.getChildren()) {
                        Db_fence fence = fenceSnapshot.getValue(Db_fence.class);

                        String fenceName = fenceSnapshot.getKey();
                        Log.d("Fence",fenceName);

                        if(fence.getType().equals("Circle")) {
                            double fenceLatitude = fence.getLatitude();
                            double fenceLongitude = fence.getLongitude();

                            LatLng newLatLng = new LatLng(fenceLatitude, fenceLongitude);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(newLatLng)
                                    .title(fenceName);

                            final Marker marker = mMap.addMarker(markerOptions);

                            CircleOptions circleOptions = new CircleOptions()
                                    .center(newLatLng)
                                    .strokeColor(fence.getSafety() == 1 ? Color.GREEN : Color.RED)
                                    .radius(fence.getRadius())
                                    .zIndex(20);
                            final Circle mapCircle = mMap.addCircle(circleOptions);

                            MapFence mapFence = new MapFence(
                                    fence,
                                    marker,
                                    null,
                                    mapCircle
                            );
                            mapFences.add(mapFence);

                        } else if(fence.getType().equals("Polygon")) {
                            DataSnapshot latitudeSnapshot = fenceSnapshot.child("points").child("latitude");
                            DataSnapshot longitudeSnapshot = fenceSnapshot.child("points").child("longitude");
                            List<Double> listLatitudes = new ArrayList<Double>();

                            List<Double> listLongitudes = new ArrayList<Double>();


                            for(DataSnapshot d_latitude : latitudeSnapshot.getChildren()) {
                                String latitude = d_latitude.getValue().toString();
                                listLatitudes.add(Double.parseDouble(latitude));

                            }
                            for(DataSnapshot d_longitude : longitudeSnapshot.getChildren()) {
                                String longitude = d_longitude.getValue().toString();
                                Log.d("Fence", longitude);
                                listLongitudes.add(Double.parseDouble(longitude));
                            }

                            PolygonOptions polygonoptions = new PolygonOptions();

                            Log.d("Fence","size is "+ listLatitudes.size());
                            Log.d("Fence",listLatitudes.toString());
                            Log.d("Fence",listLongitudes.toString());

                            for(int x = 0 ;x < listLatitudes.size() - 1; x++) {
                                Log.d("Fence",""+x);
                                LatLng firstPoint = new LatLng(listLatitudes.get(x),listLongitudes.get(x));
                                LatLng secondPoint = new LatLng(listLatitudes.get(x + 1),listLongitudes.get(x + 1));
                                polygonoptions.add(firstPoint,secondPoint);
                            }

                            // set the first and last point
                            LatLng firstPoint = new LatLng(listLatitudes.get(0),listLongitudes.get(0));
                            LatLng secondPoint = new LatLng(listLatitudes.get(listLatitudes.size() - 1),listLongitudes.get(listLatitudes.size() - 1));
                            polygonoptions.add(secondPoint,firstPoint);


                            if(fence.getSafety() == 1) {
                                polygonoptions.strokeColor(Color.GREEN);
                            } else {
                                polygonoptions.strokeColor(Color.RED);
                            }

                            Polygon mapPolygon = mMap.addPolygon(polygonoptions);

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(mapPolygon.getPoints().get(0))
                                    .title(fenceName);

                            final Marker marker = mMap.addMarker(markerOptions);

                            MapFence mapfence = new MapFence(
                                    fence,
                                    marker,
                                    mapPolygon,
                                    null
                            );

                            mapFences.add(mapfence);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void removeHistoryMarkersAndLines() {
        // loop through all history markers in the map and remove them
        for(Marker marker : historyLocationMarkers) {
            marker.remove();
        }

        // empty history marker list
        historyLocationMarkers.clear();

        // loop through all history lines in the map and remove them
        for(Polyline line: historyLocationLines) {
            line.remove();
        }

        // empty history marker list
        historyLocationLines.clear();
    }

    private void animateAndZoomToLocation(double Latitude, double Longitude){
        if(mMap == null) return;

        CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(
                Latitude,
                Longitude));
//        CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);

        if(childMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("Sample");
            childMarker = mMap.addMarker(markerOptions);
        }

        childMarker.setPosition(new LatLng(Latitude, Longitude));
        mMap.moveCamera(center);
//        mMap.animateCamera(zoom);
    }
	
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(20);
        mMap.animateCamera(zoom);
        mMap.setMyLocationEnabled(true);
    }
	
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        refreshFences();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
	}

    private void requestPermissionAccessLocation(){
        Log.d("yes","requestPermissionAccessLocation");
        int permissionCheck1 = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION }, 1234);
        }
    }

}


