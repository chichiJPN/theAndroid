package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Guardian_SetAlarmActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private String childID;
    private String currentUserID;

    // UI components
    private Switch switch_enable;
    private TimePicker timepicker;
    private TextView repeatSunday;
    private TextView repeatMonday ;
    private TextView repeatTuesday;
    private TextView repeatWednesday;
    private TextView repeatThursday;
    private TextView repeatFriday;
    private TextView repeatSaturday;

    // for debugging
    String TAG = "Guardian_SetAlarm";

    //values
    String HEX_COLOR_BLACK = "#000000";
    int INT_COLOR_BLACK = -16777216;
    String HEX_COLOR_RED = "#FF0000";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_set_alarm);
        setTitle("Set Alarm Schedule");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black


        // get value that was passed in intent
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                childID = null;
            } else {
                childID = extras.getString("userid");
            }
        } else {
            childID = (String) savedInstanceState.getSerializable("userid");
        }


        // set up progressbar
        progress = new ProgressDialog(Guardian_SetAlarmActivity.this);
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

        currentUserID = mAuth.getCurrentUser().getUid(); // gets the user ID of the logged in user

        switch_enable = (Switch) findViewById(R.id.switch_enable);
        timepicker = (TimePicker) findViewById(R.id.timepicker);
        repeatSunday = (TextView) findViewById(R.id.repeatSunday);
        repeatMonday = (TextView) findViewById(R.id.repeatMonday);
        repeatTuesday = (TextView) findViewById(R.id.repeatTuesday);
        repeatWednesday = (TextView) findViewById(R.id.repeatWednesday);
        repeatThursday = (TextView) findViewById(R.id.repeatThursday);
        repeatFriday = (TextView) findViewById(R.id.repeatFriday);
        repeatSaturday = (TextView) findViewById(R.id.repeatSaturday);

        // sets the dropdown spinner in the layout
        final Spinner spinnerFences = (Spinner) findViewById(R.id.spinner_fences);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFences.setAdapter(adapter);

        progress.show();

        // retrieves the fences made by the guardian
        mDatabase.child("users").child(currentUserID).child("Fences").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot datasnapshot : dataSnapshot.getChildren()) {
                        Db_fence fence = datasnapshot.getValue(Db_fence.class);

                        // alarms fetched should only be safe
                        if(fence.getSafety() == 1) {
                            String fenceName = datasnapshot.getKey();
                            adapter.add(fenceName);
                        }
                    }
                    adapter.notifyDataSetChanged(); // updates the spinner dropdown
                }
                progress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        final RadioGroup radiogrp_mode = (RadioGroup) findViewById(R.id.radioGrp_mode);

        radiogrp_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radiobtn_mode = (RadioButton) findViewById(checkedId);

                String mode = radiobtn_mode.getText().toString();
                String fenceName = spinnerFences.getSelectedItem().toString();

                getAlarmDetails(fenceName,mode);

            }
        });

        spinnerFences.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                int selectedId = radiogrp_mode.getCheckedRadioButtonId();
                if(selectedId == -1) {
                    Toast.makeText(Guardian_SetAlarmActivity.this, "Please select if leaving or entering.",Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton radiobtn_mode = (RadioButton) findViewById(selectedId);

                String mode = radiobtn_mode.getText().toString();
                String fenceName = parent.getItemAtPosition(position).toString();

                getAlarmDetails(fenceName,mode);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        Button btn_set = (Button) findViewById(R.id.btn_set);
        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fenceName = spinnerFences.getSelectedItem().toString();

                int selectedId = radiogrp_mode.getCheckedRadioButtonId();
                if(selectedId == -1) {
                    Toast.makeText(Guardian_SetAlarmActivity.this, "Please select if leaving or entering.",Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton radiobtn_mode = (RadioButton) findViewById(selectedId);

                String mode = radiobtn_mode.getText().toString();
                Db_alarm alarm = new Db_alarm(
                        switch_enable.isChecked(),
                        timepicker.getCurrentHour(),
                        timepicker.getCurrentMinute(),
                        repeatSunday.getCurrentTextColor() != INT_COLOR_BLACK,
                        repeatMonday.getCurrentTextColor() != INT_COLOR_BLACK,
                        repeatTuesday.getCurrentTextColor() != INT_COLOR_BLACK,
                        repeatWednesday.getCurrentTextColor() != INT_COLOR_BLACK,
                        repeatThursday.getCurrentTextColor() != INT_COLOR_BLACK,
                        repeatFriday.getCurrentTextColor() != INT_COLOR_BLACK,
                        repeatSaturday.getCurrentTextColor() != INT_COLOR_BLACK
                );

                final DatabaseReference users = mDatabase.child("users");
                users.child(childID).child("alarms").child(fenceName).child(mode).setValue(alarm);
                Toast.makeText(getApplicationContext(), "Alarm has been set",
                        Toast.LENGTH_SHORT).show();

            }
        });

        repeatSunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ""+repeatSunday.getCurrentTextColor());
                if(repeatSunday.getCurrentTextColor() == INT_COLOR_BLACK) { // current color is black
                    repeatSunday.setTextColor(Color.parseColor(HEX_COLOR_RED));
                } else {
                    repeatSunday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                }
                Log.d(TAG, "Sunday clicked");
            }
        });
        repeatMonday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatMonday.getCurrentTextColor() == INT_COLOR_BLACK) { // current color is black
                    repeatMonday.setTextColor(Color.parseColor(HEX_COLOR_RED));
                } else {
                    repeatMonday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                }
            }
        });
        repeatTuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatTuesday.getCurrentTextColor() == INT_COLOR_BLACK) { // current color is black
                    repeatTuesday.setTextColor(Color.parseColor(HEX_COLOR_RED));
                } else {
                    repeatTuesday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                }
            }
        });
        repeatWednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatWednesday.getCurrentTextColor() == INT_COLOR_BLACK) { // current color is black
                    repeatWednesday.setTextColor(Color.parseColor(HEX_COLOR_RED));
                } else {
                    repeatWednesday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                }
            }
        });
        repeatThursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatThursday.getCurrentTextColor() == INT_COLOR_BLACK) { // current color is black
                    repeatThursday.setTextColor(Color.parseColor(HEX_COLOR_RED));
                } else {
                    repeatThursday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                }
            }
        });
        repeatFriday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatFriday.getCurrentTextColor() == INT_COLOR_BLACK) { // current color is black
                    repeatFriday.setTextColor(Color.parseColor(HEX_COLOR_RED));
                } else {
                    repeatFriday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                }
            }
        });
        repeatSaturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatSaturday.getCurrentTextColor() == INT_COLOR_BLACK) { // current color is black
                    repeatSaturday.setTextColor(Color.parseColor(HEX_COLOR_RED));
                } else {
                    repeatSaturday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                }
            }
        });
    }

    public void getAlarmDetails(String fenceName, String mode) {
        progress.show();
        // retrieves from the database the fences made by the user
        mDatabase.child("users").child(childID).child("alarms").child(fenceName).child(mode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot alarmSnapshot) {

                if(alarmSnapshot.exists()) {
                    Db_alarm alarm = alarmSnapshot.getValue(Db_alarm.class);

                    switch_enable.setChecked(alarm.getEnable());
                    timepicker.setCurrentHour(alarm.getHour());
                    timepicker.setCurrentMinute(alarm.getMinute());
                    repeatSunday.setTextColor(alarm.getSunday() ? Color.parseColor(HEX_COLOR_RED) : Color.parseColor(HEX_COLOR_BLACK));
                    repeatMonday.setTextColor(alarm.getMonday() ? Color.parseColor(HEX_COLOR_RED) : Color.parseColor(HEX_COLOR_BLACK));
                    repeatTuesday.setTextColor(alarm.getTuesday() ? Color.parseColor(HEX_COLOR_RED) : Color.parseColor(HEX_COLOR_BLACK));
                    repeatWednesday.setTextColor(alarm.getWednesday() ? Color.parseColor(HEX_COLOR_RED) : Color.parseColor(HEX_COLOR_BLACK));
                    repeatThursday.setTextColor(alarm.getThursday() ? Color.parseColor(HEX_COLOR_RED) : Color.parseColor(HEX_COLOR_BLACK));
                    repeatFriday.setTextColor(alarm.getFriday() ? Color.parseColor(HEX_COLOR_RED) : Color.parseColor(HEX_COLOR_BLACK));
                    repeatSaturday.setTextColor(alarm.getSaturday() ? Color.parseColor(HEX_COLOR_RED) : Color.parseColor(HEX_COLOR_BLACK));
                } else {
                    switch_enable.setChecked(false);
                    repeatSunday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                    repeatMonday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                    repeatTuesday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                    repeatWednesday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                    repeatThursday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                    repeatFriday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                    repeatSaturday.setTextColor(Color.parseColor(HEX_COLOR_BLACK));
                }
                progress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
