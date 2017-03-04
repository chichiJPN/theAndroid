package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Child_ViewTasksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private String currentUserID;

    // UI components
    private ImageView prof_pic;
    private TextView pic_description;
    LinearLayout task_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_view_tasks);
        setTitle("Tasks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black
		
        progress = new ProgressDialog(Child_ViewTasksActivity.this);
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

        currentUserID = mAuth.getCurrentUser().getUid();
		
        prof_pic = (ImageView) findViewById(R.id.prof_pic);
        pic_description = (TextView) findViewById(R.id.pic_description);
        task_container = (LinearLayout) findViewById(R.id.task_container);

        refreshTaskList();
   }
    private void refreshTaskList() {
        progress.show();
        mDatabase.child("users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot childSnapshot) {
                Db_user child = childSnapshot.getValue(Db_user.class);

                prof_pic.setBackgroundResource(R.drawable.profile_child1);
                pic_description.setText(child.getFirstName() + "'s Tasks");

                if(childSnapshot.child("Tasks").exists()) {
                    task_container.removeAllViews();
                    DataSnapshot taskGroupSnapshot = childSnapshot.child("Tasks");

                    for(DataSnapshot taskSnapshot : taskGroupSnapshot.getChildren()) {
                        final String timestamp = taskSnapshot.getKey().toString();
                        final String details = taskSnapshot.child("details").getValue().toString();
                        final String taskName = taskSnapshot.child("name").getValue().toString();
                        int status = Integer.parseInt(taskSnapshot.child("status").getValue().toString());


                        // check if the task has been completed or not
                        if(status != 2) {
                            LinearLayout taskRow = new LinearLayout(Child_ViewTasksActivity.this);

                            // task status 1 = pending for approval by parent
                            if(status == 1) {
                                taskRow.setBackgroundResource(R.drawable.left_border_yellow);
                            } else if(status == 0){
                                taskRow.setBackgroundResource(R.drawable.left_border_black);
                            }
                            taskRow.setPadding(20, 20, 20, 20);
                            taskRow.setOrientation(LinearLayout.HORIZONTAL);
                            taskRow.setWeightSum(10);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.height = 150;
                            taskRow.setLayoutParams(params);
                            if(status != 1) {
                                taskRow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        popUp(timestamp, details, taskName);
                                    }
                                });
                            }

                            // creates the task name part
                            TextView tvTask = new TextView(Child_ViewTasksActivity.this);
                            Log.d("task name", taskName);
                            tvTask.setText(taskName);
                            tvTask.setTextColor(Color.BLACK);
                            tvTask.setGravity(Gravity.CENTER);
                            tvTask.setEms(10);
                            tvTask.setLayoutParams(new LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.MATCH_PARENT, 10.0f));
                            taskRow.addView(tvTask);

                            task_container.addView(taskRow);
                        }
                    }
                }
                progress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void popUp(final String timestamp, String details, final String taskName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Child_ViewTasksActivity.this);

        builder.setTitle(taskName + ": ");
        builder.setMessage(details);

        // Set up the buttons
        builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Child_ViewTasksActivity.this);
                builder.setTitle("Have you finished the task \""+ taskName + "\"?");

                // Set up the buttons
                builder.setPositiveButton("I finished this", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child("users").child(currentUserID).child("Tasks").child(timestamp).child("status").setValue(1);

                        Toast.makeText(getApplicationContext(), taskName + " has been updated",
                                Toast.LENGTH_SHORT).show();
                        refreshTaskList();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
