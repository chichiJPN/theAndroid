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
    private Switch switch_enable;
    private TimePicker timepicker;
    private TextView repeatSunday;
    private TextView repeatMonday ;
    private TextView repeatTuesday;
    private TextView repeatWednesday;
    private TextView repeatThursday;
    private TextView repeatFriday;
    private TextView repeatSaturday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_set_alarm);
        setTitle("Set Alarm Schedule");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

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
        // retrieves from the database the fences made by the user
        mDatabase.child("users").child(currentUserID).child("Fences").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot datasnapshot : dataSnapshot.getChildren()) {
                        Db_fence fence = datasnapshot.getValue(Db_fence.class);
                        String fenceName = datasnapshot.getKey();
                        adapter.add(fenceName);
                    }
                    adapter.notifyDataSetChanged();
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
                        repeatSunday.getCurrentTextColor() != -1,
                        repeatMonday.getCurrentTextColor() != -1,
                        repeatTuesday.getCurrentTextColor() != -1,
                        repeatWednesday.getCurrentTextColor() != -1,
                        repeatThursday.getCurrentTextColor() != -1,
                        repeatFriday.getCurrentTextColor() != -1,
                        repeatSaturday.getCurrentTextColor() != -1
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
                if(repeatSunday.getCurrentTextColor() == -1) { // current color is white
                    repeatSunday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatSunday.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
        repeatMonday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatMonday.getCurrentTextColor() == -1) { // current color is white
                    repeatMonday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatMonday.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
        repeatTuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatTuesday.getCurrentTextColor() == -1) { // current color is white
                    repeatTuesday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatTuesday.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
        repeatWednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatWednesday.getCurrentTextColor() == -1) { // current color is white
                    repeatWednesday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatWednesday.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
        repeatThursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatThursday.getCurrentTextColor() == -1) { // current color is white
                    repeatThursday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatThursday.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
        repeatFriday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatFriday.getCurrentTextColor() == -1) { // current color is white
                    repeatFriday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatFriday.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
        repeatSaturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatSaturday.getCurrentTextColor() == -1) { // current color is white
                    repeatSaturday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatSaturday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
    }

    public void getAlarmDetails(String fenceName, String mode) {

        // retrieves from the database the fences made by the user
        mDatabase.child("users").child(childID).child("alarms").child(fenceName).child(mode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    Db_alarm alarm = dataSnapshot.getValue(Db_alarm.class);

                    switch_enable.setChecked(alarm.getEnable());
                    timepicker.setCurrentHour(alarm.getHour());
                    timepicker.setCurrentMinute(alarm.getMinute());
                    repeatSunday.setTextColor(alarm.getSunday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                    repeatMonday.setTextColor(alarm.getMonday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                    repeatTuesday.setTextColor(alarm.getTuesday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                    repeatWednesday.setTextColor(alarm.getWednesday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                    repeatThursday.setTextColor(alarm.getThursday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                    repeatFriday.setTextColor(alarm.getFriday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                    repeatSaturday.setTextColor(alarm.getSaturday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                } else {
                    switch_enable.setChecked(false);
                    repeatSunday.setTextColor(Color.parseColor("#000000"));
                    repeatMonday.setTextColor(Color.parseColor("#000000"));
                    repeatTuesday.setTextColor(Color.parseColor("#000000"));
                    repeatWednesday.setTextColor(Color.parseColor("#000000"));
                    repeatThursday.setTextColor(Color.parseColor("#000000"));
                    repeatFriday.setTextColor(Color.parseColor("#000000"));
                    repeatSaturday.setTextColor(Color.parseColor("#000000"));
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
