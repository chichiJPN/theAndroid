package team.virtualnanny;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
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

public class Child_ViewFenceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String m_Text = "";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private String parentID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                parentID = null;
            } else {
                parentID = extras.getString("parentid");
            }
        } else {
            parentID = (String) savedInstanceState.getSerializable("parentid");
        }

        setContentView(R.layout.child_view_fence);
        setTitle("GEOZONES");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

        progress = new ProgressDialog(Child_ViewFenceActivity.this);
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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
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
        requestPermissionAccessLocation();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(14);
        mMap.animateCamera(zoom);
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshotCurrentUser) {
                Db_user currentUser = snapshotCurrentUser.getValue(Db_user.class);
                CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(currentUser.getLastLatitude(),currentUser.getLastLatitude()));
                mMap.moveCamera(center);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        progress.show();
        mDatabase.child("users").child(parentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot parentSnapshot) {
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

//                        existingMarkers.add(marker);
//                        existingCircles.add(mapCircle);
//                        existingFences.add(fence);
                    }

                }
                progress.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.dismiss();
            }
        });
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
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
	}
}
