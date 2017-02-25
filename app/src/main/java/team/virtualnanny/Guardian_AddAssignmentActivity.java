package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Guardian_AddAssignmentActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String childID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_add_assignment);
        setTitle("Add Assignments");
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

        progress = new ProgressDialog(Guardian_AddAssignmentActivity.this);
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

        String[] arraySpinner = new String[] {
                "Reminders", "Tasks", "Steps"
        };


        final TextView TextView_assignmentName = (TextView) findViewById(R.id.TextView_assignmentName);
        final EditText editText_assignmentName = (EditText) findViewById(R.id.editText_assignmentName);
        final TextView textView_timeStart = (TextView) findViewById(R.id.textView_timeStart);
        final TimePicker timepicker_timeStart = (TimePicker) findViewById(R.id.timepicker_timeStart);
        final TextView TextView_startDate= (TextView) findViewById(R.id.TextView_startDate);
        final DatePicker datePicker_startDate = (DatePicker) findViewById(R.id.datePicker_startDate);
        final TextView TextView_endDate= (TextView) findViewById(R.id.TextView_endDate);
        final DatePicker datePicker_endDate = (DatePicker) findViewById(R.id.datePicker_endDate);
        final EditText editText_numCompletionsForReward = (EditText) findViewById(R.id.editText_NumCompletionForReward);
        final EditText editText_consequence = (EditText) findViewById(R.id.editText_consequence);
        final EditText editText_reward = (EditText) findViewById(R.id.editText_reward);
        final TextView textView_NumCompletionForReward= (TextView) findViewById(R.id.textView_NumCompletionForReward);




        final Spinner spinnerAssignmentOption = (Spinner) findViewById(R.id.spinner_assignmentOption);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        spinnerAssignmentOption.setAdapter(adapter);
        spinnerAssignmentOption.setPadding(0,30,0,30);

        spinnerAssignmentOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String assignmentType = parent.getItemAtPosition(position).toString();

                switch(assignmentType) {
                    case "Reminders":
						// do not show num completion box
                        TextView_assignmentName.setVisibility(View.VISIBLE);
                        editText_assignmentName.setVisibility(View.VISIBLE);
                        textView_NumCompletionForReward.setText("Number of completions for reward");
                        textView_NumCompletionForReward.setVisibility(View.GONE);
                        editText_numCompletionsForReward.setVisibility(View.GONE);
                        textView_timeStart.setVisibility(View.VISIBLE);
                        timepicker_timeStart.setVisibility(View.VISIBLE);
                        TextView_startDate.setVisibility(View.VISIBLE);
                        datePicker_startDate.setVisibility(View.VISIBLE);
                        TextView_endDate.setVisibility(View.VISIBLE);
                        datePicker_endDate.setVisibility(View.VISIBLE);
                        break;
                    case "Tasks":
                        TextView_assignmentName.setVisibility(View.VISIBLE);
                        editText_assignmentName.setVisibility(View.VISIBLE);
                        textView_NumCompletionForReward.setText("Number of completions for reward");
                        textView_NumCompletionForReward.setVisibility(View.VISIBLE);
                        editText_numCompletionsForReward.setVisibility(View.VISIBLE);
                        textView_timeStart.setVisibility(View.GONE);
                        timepicker_timeStart.setVisibility(View.GONE);
                        TextView_startDate.setVisibility(View.VISIBLE);
                        datePicker_startDate.setVisibility(View.VISIBLE);
                        TextView_endDate.setVisibility(View.VISIBLE);
                        datePicker_endDate.setVisibility(View.VISIBLE);
						// show num completion box
                        break;
                    case "Steps":
                        TextView_assignmentName.setVisibility(View.GONE);
                        editText_assignmentName.setVisibility(View.GONE);
                        textView_NumCompletionForReward.setText("Number of steps needed for reward");
                        textView_NumCompletionForReward.setVisibility(View.VISIBLE);
                        editText_numCompletionsForReward.setVisibility(View.VISIBLE);
                        textView_timeStart.setVisibility(View.GONE);
                        timepicker_timeStart.setVisibility(View.GONE);
                        TextView_startDate.setVisibility(View.GONE);
                        datePicker_startDate.setVisibility(View.GONE);
                        TextView_endDate.setVisibility(View.GONE);
                        datePicker_endDate.setVisibility(View.GONE);

                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btn_set = (Button) findViewById(R.id.btn_set);

        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String assignmentName = editText_assignmentName.getText().toString().trim();
                String assignmentType = spinnerAssignmentOption.getSelectedItem().toString();
                int startHour = timepicker_timeStart.getCurrentHour();
                int startMinute = timepicker_timeStart.getCurrentMinute();
                int startMonth = datePicker_startDate.getMonth();
                int startDay = datePicker_startDate.getDayOfMonth();
                int endMonth = datePicker_endDate.getMonth();
                int endDay = datePicker_endDate.getDayOfMonth();
                String consequence = editText_consequence.getText().toString().trim();
                String reward = editText_reward.getText().toString().trim();


                switch(assignmentType) {
                    case "Reminders":
                        if(assignmentName.equals("")) {
                            Toast.makeText(Guardian_AddAssignmentActivity.this, "Please enter a name",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Db_assignment reminder = new Db_assignment(
                                "Set",
                                startHour,
                                startMinute,
                                startMonth,
                                startDay,
                                endMonth,
                                endDay,
                                0,
                                0,
                                consequence,
                                reward
                        );

                        mDatabase.child("users")
                                .child(childID)
                                .child("assignments")
                                .child(assignmentType)
                                .child(assignmentName)
                                .setValue(reminder);
                        Toast.makeText(Guardian_AddAssignmentActivity.this, "Reminder has been added.",Toast.LENGTH_SHORT).show();
                        onBackPressed();

                        break;
                    case "Tasks":
                        if(assignmentName.equals("")) {
                            Toast.makeText(Guardian_AddAssignmentActivity.this, "Please enter a name",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int numCompletionsForReward = Integer.parseInt(editText_numCompletionsForReward.getText().toString());

                        Db_assignment task = new Db_assignment(
                                "Set",
                                startHour,
                                startMinute,
                                startMonth,
                                startDay,
                                endMonth,
                                endDay,
                                0,
                                numCompletionsForReward,
                                consequence,
                                reward
                        );

                        mDatabase.child("users")
                                .child(childID)
                                .child("assignments")
                                .child(assignmentType)
                                .child(assignmentName)
                                .setValue(task);
                        Toast.makeText(Guardian_AddAssignmentActivity.this, "Task has been added.",Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        break;
                    case "Steps":
                        int numStepsForReward = Integer.parseInt(editText_numCompletionsForReward.getText().toString());
                        Db_assignment steps = new Db_assignment(
                                "Set",
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                numStepsForReward,
                                consequence,
                                reward
                        );
                        mDatabase.child("users")
                                .child(childID)
                                .child("assignments")
                                .child(assignmentType)
                                .setValue(steps);
                        Toast.makeText(Guardian_AddAssignmentActivity.this, "Steps have been updated.",Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        break;
                }

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
