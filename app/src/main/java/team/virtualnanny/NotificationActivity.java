package team.virtualnanny;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NotificationActivity extends AppCompatActivity {

    public static final String TAG = "NotificationActivity";
    private ProgressDialog progress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String childID;
    private String currentUserID;

    // UI components
    LinearLayout notificationContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        setTitle("Notifications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black


        progress = new ProgressDialog(NotificationActivity.this);
        progress.setTitle("Getting notification");
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

        notificationContainer = (LinearLayout) findViewById(R.id.notification_container);

        currentUserID = mAuth.getCurrentUser().getUid();

        mDatabase.child("users").child(currentUserID).child("notifications").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot notificationsSnapshot) {
                if(notificationsSnapshot.getChildrenCount() == 0) {
                    Toast.makeText(getApplicationContext(), "No notifications found",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if(notificationsSnapshot.exists()) {


                    List<DataSnapshot> snapshotList= new ArrayList<DataSnapshot>();

                    //
                    for(DataSnapshot noti: notificationsSnapshot.getChildren() ) {
                        snapshotList.add(noti);
                    }

                    for(int x = snapshotList.size() - 1 ; x >= 0 ; x--) {
                        DataSnapshot noti = snapshotList.get(x);

                        final String timestamp = noti.getKey().toString();
                        final Db_notification notification = noti.getValue(Db_notification.class);
                        Log.d(TAG,notification.getTitle());

                        final LinearLayout notificationRow = new LinearLayout(NotificationActivity.this);
                        notificationRow.setPadding(30,20,30,20);
                        notificationRow.setOrientation(LinearLayout.HORIZONTAL);
                        notificationRow.setWeightSum(10);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                        params.height = 175;
                        notificationRow.setLayoutParams(params);
                        notificationRow.setBackgroundResource(R.drawable.bottom_border_black);

                        final TextView tvNotificationTitle = new TextView(getApplicationContext());
                        final TextView tvTime = new TextView(getApplicationContext());
//                        final TextView tvNotificationContent = new TextView(getApplicationContext());

                        notificationRow.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View view) {
                                   // set the notificaation value to "Read"
                                   mDatabase.child("users").child(currentUserID).child("notifications").child(timestamp).child("status").setValue("Read");
                                   tvNotificationTitle.setTypeface(null, Typeface.NORMAL);
                                   tvTime.setTypeface(null, Typeface.NORMAL);


                                   AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
                                   builder.setTitle(notification.getTitle());
                                   builder.setMessage(notification.getContent());

                                   builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           mDatabase.child("users").child(currentUserID).child("notifications").child(timestamp).removeValue();
                                           notificationContainer.removeView(notificationRow);
                                           dialog.cancel();
                                       }
                                   });

                                   // Set up the buttons
                                   builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           dialog.cancel();
                                       }
                                   });

                                   builder.show();
                               }
                        });

                        String notiTitle = notification.getTitle();

                        notiTitle = notiTitle.length() > 50 ? notiTitle.substring(0,50) + "..." : notiTitle;


                        tvNotificationTitle.setText(notiTitle);
                        tvNotificationTitle.setTextColor(Color.BLACK);
                        tvNotificationTitle.setGravity(Gravity.LEFT | Gravity.CENTER);
                        tvNotificationTitle.setEms(10);
                        tvNotificationTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.FILL_PARENT,
                                7.0f));


                        // convert timestamp from milliseconds to time
                        long Ltimestamp = Long.parseLong(timestamp);

                        long minute = (Ltimestamp/ (1000 * 60)) % 60;
                        long hour = ((Ltimestamp / (1000 * 60 * 60)) + 7) % 24;

                        String timeNotified = String.format("%02d:%02d %s", (hour > 11 ? hour - 11: hour), minute,(hour > 11 ? "PM": "AM"));

                        tvTime.setText(timeNotified);
                        tvTime.setTextColor(Color.BLACK);
                        tvTime.setGravity(Gravity.RIGHT | Gravity.CENTER);
                        tvTime.setEms(10);
                        tvTime.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.FILL_PARENT,
                                3.0f));

                        // Set notification text to bold if its not read
                        if(notification.getStatus().equals("Not read")) {
                            tvNotificationTitle.setTypeface(null, Typeface.BOLD);
                            tvTime.setTypeface(null, Typeface.BOLD);
                        }


                        notificationRow.addView(tvNotificationTitle);
                        notificationRow.addView(tvTime);
                        notificationContainer.addView(notificationRow);
                    }
                } else {
                    Log.d(TAG, "I dont exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
