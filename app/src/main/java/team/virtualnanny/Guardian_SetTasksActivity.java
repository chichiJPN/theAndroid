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
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.radius;


public class Guardian_SetTasksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private String childID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_set_tasks);
        setTitle("Tasks");
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
        progress = new ProgressDialog(Guardian_SetTasksActivity.this);
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

		ImageView profPic = (ImageView) findViewById(R.id.profPic);
        profPic.setBackgroundResource(R.drawable.profile_child1);

		ImageView btn_add_task = (ImageView) findViewById(R.id.btn_add_task);
		TextView btn_add_task_text = (TextView) findViewById(R.id.btn_add_task_text);
        refreshTaskList();


        btn_add_task.setOnClickListener(new addTask());
        btn_add_task_text.setOnClickListener(new addTask());


    }

    private void refreshTaskList() {
        progress.show();
        final TextView ownerDescription = (TextView) findViewById(R.id.ownerDescription);
        DatabaseReference users = mDatabase.child("users");
        users.child(childID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot child) {

                ownerDescription.setText(child.child("firstName").getValue().toString() + "'s tasks");

                DataSnapshot tasks = child.child("Tasks");
                if(tasks.exists()) {
                    final LinearLayout tasksContainer = (LinearLayout) findViewById(R.id.tasks_container);
                    tasksContainer.removeAllViews();

                    for(DataSnapshot task : tasks.getChildren()) {
                        final String timestamp = task.getKey();
                        String taskName = task.child("name").getValue().toString();
                        final String details = task.child("details").getValue().toString();
                        String status = task.child("status").getValue().toString();

                        LinearLayout taskRow = new LinearLayout(Guardian_SetTasksActivity.this);
                        taskRow.setPadding(0,20,0,20);
                        taskRow.setOrientation(LinearLayout.HORIZONTAL);
                        taskRow.setWeightSum(13);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.height = 100;
                        taskRow.setLayoutParams(params);

                        TextView tvTaskName = new TextView(getApplicationContext());
                        Log.d("task name", taskName);
                        tvTaskName.setText(taskName);
                        tvTaskName.setTextColor(Color.BLACK);
                        tvTaskName.setGravity(Gravity.CENTER);
                        tvTaskName.setEms(10);
                        tvTaskName.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT, 7.0f));


                        ImageView imageTaskDetails = new ImageView(getApplicationContext());
                        imageTaskDetails.setBackgroundResource(R.drawable.edit_blue_64x64);

                        imageTaskDetails.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
                        imageTaskDetails.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(Guardian_SetTasksActivity.this, details,Toast.LENGTH_SHORT).show();
                            }
                        });

                        ImageView imageTaskEdit = new ImageView(getApplicationContext());
                        imageTaskEdit.setBackgroundResource(R.drawable.edit_blue_64x64);
                        imageTaskEdit.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
                        imageTaskEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(Guardian_SetTasksActivity.this, "Edit button clicked",Toast.LENGTH_SHORT).show();

                            }
                        });
                        ImageView imageTaskDelete = new ImageView(getApplicationContext());
                        imageTaskDelete.setBackgroundResource(R.drawable.trash_blue_32x32);
                        imageTaskDelete.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
                        imageTaskDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DatabaseReference users = mDatabase.child("users");
                                users.child(childID).child("Tasks").child(timestamp).removeValue();
                                refreshTaskList();
                            }
                        });


                        taskRow.addView(tvTaskName);
                        taskRow.addView(imageTaskDetails);
                        taskRow.addView(imageTaskEdit);
                        taskRow.addView(imageTaskDelete);
                        tasksContainer.addView(taskRow);
                    }
                }
                progress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class addTask implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Guardian_SetTasksActivity.this);
            builder.setTitle("Add Task");

            LinearLayout layout = new LinearLayout(Guardian_SetTasksActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText input_taskName = new EditText(Guardian_SetTasksActivity.this);
            input_taskName.setInputType(InputType.TYPE_CLASS_TEXT);
            input_taskName.setHint("Task Name");
            layout.addView(input_taskName);
            final EditText input_taskDetails = new EditText(Guardian_SetTasksActivity.this);
            input_taskDetails.setInputType(InputType.TYPE_CLASS_TEXT);
            input_taskDetails.setHint("Task Details");
            layout.addView(input_taskDetails);

            builder.setView(layout);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String taskName = input_taskName.getText().toString().trim();
                    String taskDetails = input_taskDetails.getText().toString().trim();

                    Map<String, Object> task = new HashMap<String, Object>(); //
                    task.put("name", taskName);
                    task.put("details", taskDetails);
                    task.put("status", 0);

                    String timestamp = ""+System.currentTimeMillis();
                    DatabaseReference users = mDatabase.child("users");
                    users.child(childID).child("Tasks").child(timestamp).updateChildren(task);
                    Toast.makeText(Guardian_SetTasksActivity.this, "Task has been added.",Toast.LENGTH_SHORT).show();
                    refreshTaskList();
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
