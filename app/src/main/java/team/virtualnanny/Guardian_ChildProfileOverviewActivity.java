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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.security.Guard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Guardian_ChildProfileOverviewActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private String currentlySelectedUserID = null;
    private String currentUserNumber = null;
    private Marker childMarker;
    private List<Db_fence> existingFences;
    private List<Marker> existingMarkers;
    private List<Circle> existingCircles;
    private ChildLocationUpdate ChildLocationThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_child_profile_overview);

        existingFences = new ArrayList<Db_fence>();
        existingMarkers = new ArrayList<Marker>();
        existingCircles = new ArrayList<Circle>();

        progress = new ProgressDialog(Guardian_ChildProfileOverviewActivity.this);
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
        Intent intent = new Intent(getApplicationContext(), Guardian_Service.class);
        startService(intent);

        final TextView tv_name = (TextView) findViewById(R.id.textview_name);
        final TextView tv_address = (TextView) findViewById(R.id.textview_address);
        final TextView textview_steps = (TextView) findViewById(R.id.textview_steps);

        final LinearLayout panel_header = (LinearLayout) findViewById(R.id.panel_header);

        final DatabaseReference users = mDatabase.child("users");
        final String currentUserID = mAuth.getCurrentUser().getUid();
        users.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Fences").exists()) {
                    DataSnapshot Fences = dataSnapshot.child("Fences");
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


                if(dataSnapshot.child("children").exists()) {
                    progress.show();

                    for (DataSnapshot snapshot : dataSnapshot.child("children").getChildren()) {

                        final String childID = snapshot.getValue().toString();
                        users.child(childID).child("Parent").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot parent) {
                                if(parent.exists() && parent.getValue().toString().equals(currentUserID)) {
                                    Log.d("User id is ",childID);
                                    ImageView ii = new ImageView(Guardian_ChildProfileOverviewActivity.this);
                                    ii.setBackgroundResource(R.drawable.profile_child1);
                                    ii.setTag(childID);
                                    ii.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            currentlySelectedUserID = (String)v.getTag();
                                            progress.show();
                                            users.child(currentlySelectedUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Db_user user = dataSnapshot.getValue(Db_user.class);
                                                    currentUserNumber = user.getPhone();

                                                    String firstName = user.getFirstName();
                                                    String lastName = user.getLastName();
                                                    String address = user.getAddress();

                                                    int numSteps = user.getNumSteps();

                                                    tv_name.setText(firstName + " "+ lastName);
                                                    tv_address.setText(address);
                                                    textview_steps.setText("" +numSteps);
                                                    animateAndZoomToLocation(user.getLastLatitude(), user.getLastLongitude());
                                                    childMarker.setTitle(firstName + "" + lastName);

                                                    progress.dismiss();

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {}
                                            });
                                        }
                                    });
                                    panel_header.addView(ii);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                    progress.dismiss();
                } else {
                    Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "No Children",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        ImageButton btn_phone = (ImageButton) findViewById(R.id.btn_phone);
        ImageButton btn_message = (ImageButton) findViewById(R.id.btn_message);
        ImageButton btn_fence = (ImageButton) findViewById(R.id.btn_fence);
        ImageButton btn_limit = (ImageButton) findViewById(R.id.btn_limit);
        ImageButton btn_alarm = (ImageButton) findViewById(R.id.btn_alarm);
        ImageButton btn_dashboard = (ImageButton) findViewById(R.id.btn_dashboard);
        ImageButton btn_history = (ImageButton) findViewById(R.id.btn_history);
        ImageButton btn_task = (ImageButton) findViewById(R.id.btn_task);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.RelativeLayout1);

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
            Toast.makeText(getApplicationContext(), "History Button Clicked!",
                    Toast.LENGTH_SHORT).show();
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
            startActivity(i);
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        layout.setOnTouchListener(new OnSwipeTouchListener(Guardian_ChildProfileOverviewActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "top", Toast.LENGTH_SHORT).show();
            }

            // left to right
            public void onSwipeRight() {
                Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_MenuDrawerActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.left2right_enter, R.anim.left2right_exit);
            }

            public void onSwipeLeft() {
                Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "left", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeBottom() {
                Toast.makeText(Guardian_ChildProfileOverviewActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void animateAndZoomToLocation(double Latitude, double Longitude){
        if(mMap == null) return;

        CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(
                Latitude,
                Longitude));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);

        if(childMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("Sample");
            childMarker = mMap.addMarker(markerOptions);
        }

        childMarker.setPosition(new LatLng(Latitude, Longitude));
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        mMap.setMyLocationEnabled(true);

    }

    private void requestPermissionAccessLocation(){
        Log.d("yes","requestPermissionAccessLocation");
        int permissionCheck1 = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION }, 1234);
        }
    }

    class ChildLocationUpdate extends Thread{
        volatile boolean work = true;
        LatLng latLng;
        public ChildLocationUpdate() {
        }

        @Override
        public void run() {
            while (work){
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //latLng = new LatLng(lat, lng);
            }
        }
    }


}


