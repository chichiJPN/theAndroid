package team.virtualnanny;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Guard;
import java.util.Timer;
import java.util.TimerTask;

public class Guardian_ChildProfileOverviewActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    GPSTracker gps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_child_profile_overview);

        LinearLayout ll = (LinearLayout) findViewById(R.id.panel_header);

        ImageView ii = new ImageView(this);
        ii.setBackgroundResource(R.drawable.profile_child1);
        ll.addView(ii);

        ImageView ii2 = new ImageView(this);
        ii2.setBackgroundResource(R.drawable.profile_child2);
        ll.addView(ii2);

        ImageView ii3 = new ImageView(this);
        ii3.setBackgroundResource(R.drawable.profile_child3);
        ll.addView(ii3);

        TextView tv_name_ = (TextView) findViewById(R.id.textview_name);
        TextView tv_address_ = (TextView) findViewById(R.id.textview_address);
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
                smsIntent.putExtra("sms_body", "Body of Message");
                startActivity(smsIntent);
            }
        });
        btn_fence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_SetFenceActivity.class);
                startActivity(i);
            }
        });
        btn_limit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_SetLimitActivity.class);
                startActivity(i);
            }
        });
        btn_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_SetAlarmActivity.class);
                startActivity(i);
            }
        });
        btn_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Guardian_ChildProfileOverviewActivity.this, Guardian_SetTasksActivity.class);
                startActivity(i);
            }
        });
        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "History Button Clicked!",
                        Toast.LENGTH_SHORT).show();
            }
        });
        btn_dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        gps = new GPSTracker(Guardian_ChildProfileOverviewActivity.this);

        // check if GPS enabled
        Log.d("Can get location", ""+gps.canGetLocation());

        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
/*
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
*/
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        //          LatLng sydney = new LatLng(-34, 151);
        //           mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

        public void onConnected(Bundle connectionHint) {

        /*
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Toast.makeText(getApplicationContext(), "Latitude: " + mLastLocation.getLatitude() + " | Longitude: " + mLastLocation.getLongitude(),
                    Toast.LENGTH_LONG).show();
            Log.d("Location", "Latitude: " + mLastLocation.getLatitude() + " | Longitude: " + mLastLocation.getLongitude());

        }else{
            Log.d("Location", "Location is null");
        }
        */
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}


