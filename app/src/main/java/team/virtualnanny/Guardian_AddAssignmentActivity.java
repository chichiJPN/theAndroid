package team.virtualnanny;


import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;


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

        String[] arraySpinner = new String[] {
                "Reminder", "Task", "Steps"
        };

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
                    case "Reminder":
                        break;
                    case "Task":
                        break;
                    case "Steps":
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final EditText editText_assignmentName = (EditText) findViewById(R.id.editText_assignmentName);
        final TimePicker timepicker_timeStart = (TimePicker) findViewById(R.id.timepicker_timeStart);
        final DatePicker datePicker_startDate = (DatePicker) findViewById(R.id.datePicker_startDate);
        final DatePicker datePicker_endDate = (DatePicker) findViewById(R.id.datePicker_endDate);
        final EditText editText_numCompletionsForReward = (EditText) findViewById(R.id.editText_numCompletionsForReward);
        final EditText editText_consequence = (EditText) findViewById(R.id.editText_consequence);
        final EditText editText_reward = (EditText) findViewById(R.id.editText_reward);

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

                    case "Reminder":
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

                        mDatabase.child(childID)
                                .child("assignments")
                                .child(assignmentType)
                                .child(assignmentName)
                                .setValue(reminder);
                        Toast.makeText(Guardian_AddAssignmentActivity.this, "Reminder has been added.",Toast.LENGTH_SHORT).show();
                        break;
                    case "Task":

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
                                0,
                                consequence,
                                reward
                        );

                        mDatabase.child(childID)
                                .child("assignments")
                                .child(assignmentType)
                                .child(assignmentName)
                                .setValue(task);
                        Toast.makeText(Guardian_AddAssignmentActivity.this, "Task has been added.",Toast.LENGTH_SHORT).show();
                        break;
                    case "Steps":
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
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
