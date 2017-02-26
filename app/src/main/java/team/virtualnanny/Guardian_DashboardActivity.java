package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Guardian_DashboardActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String childID;
    private LinearLayout remind_container;
    private LinearLayout tasks_container;
    private LinearLayout steps_taken_container;
    private TextView profpic_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_dashboard);
        setTitle("Performance");
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

        progress = new ProgressDialog(Guardian_DashboardActivity.this);
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
        ImageView btn_add_assignment = (ImageView) findViewById(R.id.btn_add_assignment);

        btn_add_assignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Guardian_DashboardActivity.this, Guardian_AddAssignmentActivity.class);
                i.putExtra("userid",childID);
                startActivity(i);
            }
        });
				

        ImageView profpic = (ImageView) findViewById(R.id.profpic);
        profpic_description = (TextView) findViewById(R.id.profpic_description);
        ProgressBar progressbar_consequence = (ProgressBar) findViewById(R.id.progressbar_consequence);
        ProgressBar progressbar_reward = (ProgressBar) findViewById(R.id.progressbar_reward);
        remind_container = (LinearLayout) findViewById(R.id.remind_container);
        tasks_container = (LinearLayout) findViewById(R.id.tasks_container);
        steps_taken_container = (LinearLayout) findViewById(R.id.steps_taken_container);

        profpic.setBackgroundResource(R.drawable.profile_child1);
    }
    private void setSteps(DataSnapshot child) {
        if (!child.child("assignments").child("Steps").exists()) {
            return;
        }

        DataSnapshot assignments = child.child("assignments");

        final Db_assignment steps = assignments.child("Steps").getValue(Db_assignment.class);
        final int numStepsToday = (Integer.parseInt(child.child("numStepsToday").getValue().toString())) + 1 ;

        LinearLayout Row = new LinearLayout(Guardian_DashboardActivity.this);
        Row.setBackgroundResource(R.drawable.performance_reminder_border);
        Row.setPadding(20, 20, 20, 20);
        Row.setOrientation(LinearLayout.HORIZONTAL);
        Row.setWeightSum(15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = 100;
        Row.setLayoutParams(params);
        Row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Num Steps", " is " + steps.getNumCompletionForReward());
                popUpDelete("Steps",numStepsToday + " Steps",steps.getReward(),steps.getConsequence());
            }
        });

        // creates the task name part
        TextView tvSteps = new TextView(Guardian_DashboardActivity.this);
        tvSteps.setText("" + steps.getNumCompletionForReward() + " steps per day");
        tvSteps.setTextColor(Color.BLACK);
        tvSteps.setGravity(Gravity.CENTER);
        tvSteps.setEms(10);
        tvSteps.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 7.0f));

        int completionsForReward = steps.getNumCompletionForReward();

        ProgressBar progressbar = new ProgressBar(Guardian_DashboardActivity.this,null, android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 5.0f));
        progressbar.setMax((int)completionsForReward);

        progressbar.setProgress(numStepsToday);

        TextView tvPercentage = new TextView(Guardian_DashboardActivity.this);
        tvPercentage.setText("" + (numStepsToday/completionsForReward * 100.0) + "%");
        tvPercentage.setTextColor(Color.BLACK);
        tvPercentage.setGravity(Gravity.RIGHT);
        tvPercentage.setEms(10);
        tvPercentage.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 3.0f));

        Row.addView(tvSteps);
        Row.addView(progressbar);
        Row.addView(tvPercentage);

        steps_taken_container.addView(Row);
    }

    public void setReminders(DataSnapshot assignments) {

        if (!assignments.child("Reminders").exists()) {
            // if there are no reminders then return
            return;
        }


        DataSnapshot reminders = assignments.child("Reminders");

        for (DataSnapshot reminderSnapshot : reminders.getChildren()) {
            final String reminderName = reminderSnapshot.getKey();
            final Db_assignment reminder = reminderSnapshot.getValue(Db_assignment.class);
            Log.d("Reminder", reminderName);

            LinearLayout reminderRow = new LinearLayout(Guardian_DashboardActivity.this);
            reminderRow.setBackgroundResource(R.drawable.performance_reminder_border);
            reminderRow.setPadding(20, 20, 20, 20);
            reminderRow.setOrientation(LinearLayout.HORIZONTAL);
            reminderRow.setWeightSum(10);
            reminderRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpDelete("Reminders",reminderName, reminder.getReward(), reminder.getConsequence());
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.height = 100;
            reminderRow.setLayoutParams(params);


            // creates the reminder name part
            TextView tvReminder = new TextView(Guardian_DashboardActivity.this);
            Log.d("task name", reminderName);
            tvReminder.setText(reminderName);
            tvReminder.setTextColor(Color.BLACK);
            tvReminder.setGravity(Gravity.CENTER);
            tvReminder.setEms(10);
            tvReminder.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 5.0f));

            int hour = reminder.getStartHour();
            int minute = reminder.getStartMinute();

            // creates the time part of the reminder
            TextView tvTime = new TextView(Guardian_DashboardActivity.this);
            tvTime.setTextColor(Color.BLACK);
            tvTime.setGravity(Gravity.CENTER);

            tvTime.setText("" + (hour % 12) + ":" + (minute < 9 ? "0" : "") + minute + (hour > 11 ? "PM" : "AM"));
            tvTime.setEms(10);
            tvTime.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 5.0f));

            reminderRow.addView(tvReminder);
            reminderRow.addView(tvTime);
            remind_container.addView(reminderRow);
        }
    }

    public void setTasks(DataSnapshot assignments) {
        if (!assignments.child("Tasks").exists()) {
            return;
        }

        DataSnapshot tasks = assignments.child("Tasks");

        for (DataSnapshot taskSnapshot : tasks.getChildren()) {
            final String taskName = taskSnapshot.getKey();
            final Db_assignment task = taskSnapshot.getValue(Db_assignment.class);
            Log.d("Task Name", taskName);

            LinearLayout taskRow = new LinearLayout(Guardian_DashboardActivity.this);
            taskRow.setBackgroundResource(R.drawable.performance_reminder_border);
            taskRow.setPadding(20, 20, 20, 20);
            taskRow.setOrientation(LinearLayout.HORIZONTAL);
            taskRow.setWeightSum(15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.height = 100;
            taskRow.setLayoutParams(params);
            taskRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpDelete("Tasks",taskName, task.getReward(),task.getConsequence());
                }
            });

            // creates the task name part
            TextView tvTask = new TextView(Guardian_DashboardActivity.this);
            Log.d("task name", taskName);
            tvTask.setText(taskName);
            tvTask.setTextColor(Color.BLACK);
            tvTask.setGravity(Gravity.CENTER);
            tvTask.setEms(10);
            tvTask.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 7.0f));

            double completionsForReward = task.getNumCompletionForReward();
            double numCompletions = task.getNumCompletion();

            ProgressBar progressbar = new ProgressBar(Guardian_DashboardActivity.this,null, android.R.attr.progressBarStyleHorizontal);
            progressbar.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 5.0f));
            progressbar.setMax((int)completionsForReward);
            progressbar.setProgress(task.getNumCompletion());

            TextView tvPercentage = new TextView(Guardian_DashboardActivity.this);
            tvPercentage.setText("" + (numCompletions/completionsForReward * 100.0) + "%");
            tvPercentage.setTextColor(Color.BLACK);
            tvPercentage.setGravity(Gravity.RIGHT);
            tvPercentage.setEms(10);
            tvPercentage.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 3.0f));


            taskRow.addView(tvTask);
            taskRow.addView(progressbar);
            taskRow.addView(tvPercentage);

            tasks_container.addView(taskRow);
        }
    }

    public void popUpDelete(final String assignmentType, final String name, String reward, String Consequence) {
        Log.d(assignmentType, name);
        AlertDialog.Builder builder = new AlertDialog.Builder(Guardian_DashboardActivity.this);

        builder.setTitle(assignmentType + ": "+name);
        builder.setMessage("Reward:" + reward +"\nConsequence:" + Consequence);

        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDatabase.child("users").child(childID).child("assignments").child(assignmentType).child(name).removeValue();
                Toast.makeText(getApplicationContext(), name + " has been deleted",
                        Toast.LENGTH_SHORT).show();
                confirmDelete();
            }

            private void confirmDelete() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Guardian_DashboardActivity.this);
                builder.setTitle("Are you sure you want to delete "+ name + "?");

                // Set up the buttons
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child("users").child(childID).child("assignments").child(assignmentType).child(name).removeValue();
                        Toast.makeText(getApplicationContext(), name + " has been deleted",
                                Toast.LENGTH_SHORT).show();
                        refreshList();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void refreshList() {
        progress.show();
        mDatabase.child("users").child(childID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot child) {
                profpic_description.setText(child.child("firstName").getValue().toString() + "'s performance");

                remind_container.removeAllViews();
                tasks_container.removeAllViews();
                steps_taken_container.removeAllViews();

                DataSnapshot assignments = child.child("assignments");

                setReminders(assignments);
                setTasks(assignments);
                setSteps(child);
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
        refreshList();

        Log.d("Onstart", "onstart");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
	}
}
