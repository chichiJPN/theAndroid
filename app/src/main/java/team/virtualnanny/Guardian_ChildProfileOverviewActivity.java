package team.virtualnanny;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class Guardian_ChildProfileOverviewActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

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
            smsIntent.putExtra("sms_body","Body of Message");
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
/*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
    }

}


