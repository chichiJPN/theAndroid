package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;


public class Child_ViewAlarmActivity extends AppCompatActivity {

    private static final String TAG = "Child_ViewALarm";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private String currentUserID;

    // fields in the UI
    private ImageView prof_pic;
    private TextView prof_description;
    private LinearLayout alarm_container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_view_alarm);
        setTitle("Alarm Schedules");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black
		
        progress = new ProgressDialog(Child_ViewAlarmActivity.this);
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

		initUI();
        setListenersAndData();
    }

    private void setListenersAndData() {

        prof_pic.setBackgroundResource(R.drawable.profile_child1);

        mDatabase.child("users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot childSnapshot) {
                Db_user child = childSnapshot.getValue(Db_user.class);
                prof_description.setText(child.getFirstName() + "'s Performance");
                alarm_container.removeAllViews();

                if(childSnapshot.child("alarms").exists()) {
                    Log.d(TAG, "alarms exist");
                    DataSnapshot alarmGroupSnapshot = childSnapshot.child("alarms");

                    for(DataSnapshot alarmSnapshot: alarmGroupSnapshot.getChildren()) {
                        String alarmName = alarmSnapshot.getKey().toString();

                        if(alarmSnapshot.child("Entering").exists()) {
                            Log.d(TAG, "Entering" + alarmName);
                            checkAndAlarmToList(alarmName,"Enter", alarmSnapshot.child("Entering"));
                        }

                        if(alarmSnapshot.child("Leaving").exists()) {
                            Log.d(TAG, "Entering");
                            checkAndAlarmToList(alarmName,"Leave", alarmSnapshot.child("Leaving"));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void checkAndAlarmToList(String alarmName,String alarmType, DataSnapshot alarmSnapshot) {
        Db_alarm alarm = alarmSnapshot.getValue(Db_alarm.class);

        // check if the alarm is enabled
        if(alarm.getEnable() == false) {
            return;
        }
        // check if the alarm is enabled for this day
        Calendar calendar = Calendar.getInstance();

        switch(calendar.get(calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                if(alarm.getSunday() == false) { return; }
                break;
            case Calendar.MONDAY:
                if(alarm.getMonday() == false) { return; }
                break;
            case Calendar.TUESDAY:
                if(alarm.getTuesday() == false) { return; }
                break;
            case Calendar.WEDNESDAY:
                if(alarm.getWednesday() == false) { return; }
                break;
            case Calendar.THURSDAY:
                if(alarm.getThursday() == false) { return; }
                break;
            case Calendar.FRIDAY:
                if(alarm.getFriday() == false) { return; }
                break;
            case Calendar.SATURDAY:
                if(alarm.getSaturday() == false) { return; }
                break;
        }

        LinearLayout alarmRow = new LinearLayout(Child_ViewAlarmActivity.this);
        alarmRow.setPadding(0,20,0,20);
        alarmRow.setOrientation(LinearLayout.HORIZONTAL);
        alarmRow.setWeightSum(10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = 100;
        alarmRow.setLayoutParams(params);


        TextView tvalarmDescription = new TextView(getApplicationContext());
        tvalarmDescription.setText(alarmType + " " + alarmName); // example : Leave School
        tvalarmDescription.setTextColor(Color.BLACK);
        tvalarmDescription.setGravity(Gravity.LEFT  );
        tvalarmDescription.setEms(10);
        tvalarmDescription.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 7.0f));

        String hour = "" + (alarm.getHour() > 12 ? alarm.getHour() - 12 : alarm.getHour());
        String period = (alarm.getHour() > 12 ? "PM" : "AM");

        TextView tvTime = new TextView(getApplicationContext());
        tvTime.setText(hour + ":" + alarm.getMinute() + period); // example : 3:05PM
        tvTime.setTextColor(Color.BLACK);
        tvTime.setGravity(Gravity.CENTER);
        tvTime.setEms(10);
        tvTime.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 3.0f));

        alarmRow.addView(tvalarmDescription);
        alarmRow.addView(tvTime);
        alarm_container.addView(alarmRow);
    }


    private void initUI() {
        currentUserID = mAuth.getCurrentUser().getUid();
        prof_pic = (ImageView) findViewById(R.id.prof_pic);
        prof_description = (TextView) findViewById(R.id.prof_description);
        alarm_container = (LinearLayout) findViewById(R.id.alarm_container);
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
