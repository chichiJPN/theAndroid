package team.virtualnanny;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class Child_ChildOverviewActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private List<Db_fence> existingFences;
    private List<Marker> existingMarkers;
    private List<Circle> existingCircles;
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_overview);
        LinearLayout ll = (LinearLayout) findViewById(R.id.panel_header);

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
		
		
        ImageView ii = new ImageView(this);
        ii.setBackgroundResource(R.drawable.profile_child1);
        ll.addView(ii);

        TextView tv_name_ = (TextView) findViewById(R.id.textview_name);
        TextView tv_address_ = (TextView) findViewById(R.id.textview_address);
        ImageButton btn_phone = (ImageButton) findViewById(R.id.btn_phone);
        ImageButton btn_message = (ImageButton) findViewById(R.id.btn_message);
        ImageButton btn_fence = (ImageButton) findViewById(R.id.btn_fence);
        ImageButton btn_alarm = (ImageButton) findViewById(R.id.btn_alarm);
        ImageButton btn_dashboard = (ImageButton) findViewById(R.id.btn_dashboard);
        ImageButton btn_task = (ImageButton) findViewById(R.id.btn_task);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.RelativeLayout1);

        requestPermissionAccessLocation();
        Intent intent = new Intent(getApplicationContext(), Child_Service.class);
        startService(intent);

        btn_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri number = Uri.parse("tel:123456789");
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            }
        });

        btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "Message Button Clicked!",
                        Toast.LENGTH_SHORT).show();
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", "09228076111");
            smsIntent.putExtra("sms_body","Body of Message");
            startActivity(smsIntent);
            }
        });

        btn_fence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final Intent i = new Intent(Child_ChildOverviewActivity.this, Child_ViewFenceActivity.class);
            startActivity(i);
            }
        });
        btn_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Child_ChildOverviewActivity.this, Child_ViewAlarmActivity.class);
                startActivity(i);
            }
        });
        btn_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final Intent i = new Intent(Child_ChildOverviewActivity.this, Child_ViewTasksActivity.class);
            startActivity(i);
            }
        });
        btn_dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final Intent i = new Intent(Child_ChildOverviewActivity.this, Child_DashboardActivity.class);
            startActivity(i);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        layout.setOnTouchListener(new OnSwipeTouchListener(Child_ChildOverviewActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(Child_ChildOverviewActivity.this, "top", Toast.LENGTH_SHORT).show();
            }

            // left to right
            public void onSwipeRight() {
                Intent i = new Intent(Child_ChildOverviewActivity.this, Child_MenuDrawerActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.left2right_enter, R.anim.left2right_exit);
            }
            public void onSwipeLeft() {
                Toast.makeText(Child_ChildOverviewActivity.this, "left", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeBottom() {
                Toast.makeText(Child_ChildOverviewActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }
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
        requestPermissionAccessLocation();

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


