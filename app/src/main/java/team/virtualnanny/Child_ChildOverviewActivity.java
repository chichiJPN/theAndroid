package team.virtualnanny;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Child_ChildOverviewActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
//    private MyLocationListener myLocationListener;
//    private LocationManager myManager;
//    private Location lastLocation;

    private ValueEventListener valueEventListener;
    private ProgressDialog progress;
    private String mParentID = null;
    private String mParentNumber = null;

    private List<Db_fence> existingFences;
    private List<Marker> existingMarkers;
    private List<Circle> existingCircles;
    private boolean runflag = true;


    // UI components
    private TextView tv_name;
    private TextView tv_address;
    private TextView textview_steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_overview);
        LinearLayout ll = (LinearLayout) findViewById(R.id.panel_header);

        existingFences = new ArrayList<Db_fence>();
        existingMarkers = new ArrayList<Marker>();
        existingCircles = new ArrayList<Circle>();


        progress = new ProgressDialog(Child_ChildOverviewActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
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
        Intent intent = new Intent(getApplicationContext(), Child_Service.class);
        startService(intent);


        tv_name = (TextView) findViewById(R.id.textview_name);
        tv_address = (TextView) findViewById(R.id.textview_address);
        textview_steps = (TextView) findViewById(R.id.textview_steps);
        ImageButton btn_phone = (ImageButton) findViewById(R.id.btn_phone);
        ImageButton btn_message = (ImageButton) findViewById(R.id.btn_message);
        ImageButton btn_fence = (ImageButton) findViewById(R.id.btn_fence);
        ImageButton btn_alarm = (ImageButton) findViewById(R.id.btn_alarm);
        ImageButton btn_dashboard = (ImageButton) findViewById(R.id.btn_dashboard);
        ImageButton btn_task = (ImageButton) findViewById(R.id.btn_task);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.RelativeLayout1);

        btn_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mParentID == null) {
                Toast.makeText(Child_ChildOverviewActivity.this, "Please register a parent and have the parent register you",Toast.LENGTH_SHORT).show();
                return;
            }

            Uri number = Uri.parse("tel:" + mParentNumber);
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(callIntent);
            }
        });

        btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mParentID == null) {
                    Toast.makeText(Child_ChildOverviewActivity.this, "Please register a parent and have the parent register you",Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", mParentNumber);
                smsIntent.putExtra("sms_body","Body of Message");
                startActivity(smsIntent);
            }
        });

        btn_fence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mParentID == null) {
                Toast.makeText(Child_ChildOverviewActivity.this, "Please register a parent and have the parent register you",Toast.LENGTH_SHORT).show();
                return;
            }

            final Intent i = new Intent(Child_ChildOverviewActivity.this, Child_ViewFenceActivity.class);
            i.putExtra("parentid",mParentID);
            startActivity(i);
            }
        });

        btn_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mParentID == null) {
                Toast.makeText(Child_ChildOverviewActivity.this, "Please register a parent and have the parent register you",Toast.LENGTH_SHORT).show();
                return;
            }
            final Intent i = new Intent(Child_ChildOverviewActivity.this, Child_ViewAlarmActivity.class);
            i.putExtra("parentid",mParentID);
            startActivity(i);
            }
        });

        btn_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mParentID == null) {
                Toast.makeText(Child_ChildOverviewActivity.this, "Please register a parent and have the parent register you",Toast.LENGTH_SHORT).show();
                return;
            }
            final Intent i = new Intent(Child_ChildOverviewActivity.this, Child_ViewTasksActivity.class);
            i.putExtra("parentid",mParentID);
            startActivity(i);
            }
        });

        btn_dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mParentID == null) {
                Toast.makeText(Child_ChildOverviewActivity.this, "Please register a parent and have the parent register you",Toast.LENGTH_SHORT).show();
                return;
            }
            final Intent i = new Intent(Child_ChildOverviewActivity.this, Child_DashboardActivity.class);
            i.putExtra("parentid",mParentID);
            startActivity(i);
            }
        });

        // add touchscreen swipe capabilities
        layout.setOnTouchListener(new OnSwipeTouchListener(Child_ChildOverviewActivity.this) {
            public void onSwipeTop() {}

            // user swipes from left to right
            public void onSwipeRight() {
                Intent i = new Intent(Child_ChildOverviewActivity.this, Child_MenuDrawerActivity.class);
                i.putExtra("parentid",mParentID);
                i.putExtra("parentnumber",mParentNumber);

                startActivity(i);
                overridePendingTransition(R.anim.left2right_enter, R.anim.left2right_exit);
            }

            public void onSwipeLeft() {}
            public void onSwipeBottom() {}
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void setPageData() {

        final DatabaseReference users = mDatabase.child("users");
        final String currentUserID = mAuth.getCurrentUser().getUid();

        users.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshotCurrentUser) {
                Db_user currentUser = snapshotCurrentUser.getValue(Db_user.class);
                tv_name.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                tv_address.setText(currentUser.getAddress());
                textview_steps.setText("" + currentUser.getNumSteps());

                // checks if the child has a parent
                if(snapshotCurrentUser.child("Parent").exists()) {
                    final String parentID = snapshotCurrentUser.child("Parent").getValue().toString();

                    // this part is to verify if the parent added the child in his account
                    users.child(parentID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot parentSnapshot) {

                            // check if the parent has children
                            if(parentSnapshot.child("children").exists()) {
                                // loop through all the parent's children
                                for(DataSnapshot child: parentSnapshot.child("children").getChildren()) {

                                    // checks if the parent has a child with the current user's id
                                    if(child.getValue().toString().equals(currentUserID)) {

                                        mParentID = parentID; // assign the parent as the child's parent
                                        mParentNumber = parentSnapshot.child("phone").getValue().toString();

                                        // checks if the parent has set any fences
                                        if(parentSnapshot.child("Fences").exists()) {
                                            DataSnapshot Fences = parentSnapshot.child("Fences");
                                            for(DataSnapshot snapshotFence : Fences.getChildren()) {
                                                Db_fence fence = snapshotFence.getValue(Db_fence.class);

                                                String fenceName = snapshotFence.getKey();
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

                                                existingMarkers.add(marker);
                                                existingCircles.add(mapCircle);
                                                existingFences.add(fence);
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        users.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshotCurrentUser) {
                Log.d("Child overview", "I was run");
                Db_user currentUser = snapshotCurrentUser.getValue(Db_user.class);
                animateAndZoomToLocation(currentUser.getLastLatitude(),currentUser.getLastLongitude());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);
        setPageData();
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(20);
        mMap.animateCamera(zoom);
    }

    private void requestPermissionAccessLocation(){
        Log.d("yes","requestPermissionAccessLocation");
        int permissionCheck1 = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION }, 1234);
        }
    }
	
    @Override
    public void onStart() {
        super.onStart();
        Log.d("On start","start");
        runflag = true;
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        runflag = false;

	}


    private void animateAndZoomToLocation(double Latitude, double Longitude){
        if(mMap == null) return;

        CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(
                Latitude,
                Longitude));
        mMap.moveCamera(center);
//        CameraUpdate zoom=CameraUpdateFactory.zoomTo(20);
//        mMap.animateCamera(zoom);
    }

}


