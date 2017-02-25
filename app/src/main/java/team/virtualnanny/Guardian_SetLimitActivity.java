package team.virtualnanny;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Guardian_SetLimitActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String childID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_set_limit);
        setTitle("Limit Screen Time");
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

        progress = new ProgressDialog(Guardian_SetLimitActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                    startActivity(intent);
                }
            }
        };

        Button btn_set = (Button) findViewById(R.id.btn_set);
        final Switch switch_enable = (Switch) findViewById(R.id.switch_enable);
        final TextView editText_numHoursPerDay = (TextView) findViewById(R.id.editText_numHoursPerDay);
        final TextView repeatSunday = (TextView) findViewById(R.id.repeatSunday);
        final TextView repeatMonday = (TextView) findViewById(R.id.repeatMonday);
        final TextView repeatTuesday = (TextView) findViewById(R.id.repeatTuesday);
        final TextView repeatWednesday = (TextView) findViewById(R.id.repeatWednesday);
        final TextView repeatThursday = (TextView) findViewById(R.id.repeatThursday);
        final TextView repeatFriday = (TextView) findViewById(R.id.repeatFriday);
        final TextView repeatSaturday = (TextView) findViewById(R.id.repeatSaturday);

        progress.show();
        mDatabase.child("users").child(childID).child("limit").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Db_limit childsLimit = dataSnapshot.getValue(Db_limit.class);
                switch_enable.setChecked(childsLimit.getEnable());
                editText_numHoursPerDay.setText(""+childsLimit.getNumHours());
                repeatSunday.setTextColor(childsLimit.getSunday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                repeatMonday.setTextColor(childsLimit.getMonday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                repeatTuesday.setTextColor(childsLimit.getTuesday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                repeatWednesday.setTextColor(childsLimit.getWednesday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                repeatThursday.setTextColor(childsLimit.getThursday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                repeatFriday.setTextColor(childsLimit.getFriday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                repeatSaturday.setTextColor(childsLimit.getSaturday() ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
                progress.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        repeatSunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatSunday.getCurrentTextColor() == -1) { // current color is white
                    repeatSunday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatSunday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatMonday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatMonday.getCurrentTextColor() == -1) { // current color is white
                    repeatMonday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatMonday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatTuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatTuesday.getCurrentTextColor() == -1) { // current color is white
                    repeatTuesday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatTuesday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatWednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatWednesday.getCurrentTextColor() == -1) { // current color is white
                    repeatWednesday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatWednesday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatThursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatThursday.getCurrentTextColor() == -1) { // current color is white
                    repeatThursday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatThursday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatFriday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatFriday.getCurrentTextColor() == -1) { // current color is white
                    repeatFriday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatFriday.setTextColor(Color.parseColor("#FFFFFF"));
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


        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int numHours = Integer.parseInt(editText_numHoursPerDay.getText().toString().trim());

                if(numHours > 24) { numHours = 24; }
                else if(numHours < 1) { numHours = 1; }

                Db_limit limit = new Db_limit(
                        switch_enable.isChecked(),
                        numHours,
                        repeatSunday.getCurrentTextColor() != -1,
                        repeatMonday.getCurrentTextColor() != -1,
                        repeatTuesday.getCurrentTextColor() != -1,
                        repeatWednesday.getCurrentTextColor() != -1,
                        repeatThursday.getCurrentTextColor() != -1,
                        repeatFriday.getCurrentTextColor() != -1,
                        repeatSaturday.getCurrentTextColor() != -1
                );

                progress.show();

                final DatabaseReference users = mDatabase.child("users");
                users.child(childID).child("limit").setValue(limit);

                Toast.makeText(getApplicationContext(), "Limit has been set",
                        Toast.LENGTH_SHORT).show();
                progress.dismiss();

            }
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
