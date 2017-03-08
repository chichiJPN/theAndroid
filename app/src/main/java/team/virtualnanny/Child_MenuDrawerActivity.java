package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class Child_MenuDrawerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private String parentID = null;
    private String parentNumber = null;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_menu_drawer);
        setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                parentID = null;
                parentNumber = null;
            } else {
                parentID = extras.getString("parentid");
                parentNumber = extras.getString("parentnumber");
            }
        } else {
            parentID = (String) savedInstanceState.getSerializable("parentid");
            parentNumber = (String) savedInstanceState.getSerializable("parentnumber");
        }

        progress = new ProgressDialog(Child_MenuDrawerActivity.this);
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

        ImageView profile_child = (ImageView) findViewById(R.id.profile_child);
        LinearLayout profileBar = (LinearLayout) findViewById(R.id.profileBar) ;

        profileBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Child_MenuDrawerActivity.this, Child_ProfileActivity.class);
                startActivity(i);
            }
        });

        ListView list;
        final String[] itemname ={
                "Notifications",
                "Send SOS",
                "Add Parent Account",
                "My ID",
                "Logout",
                "About Us"
        };

        Integer[] imgid={
                R.drawable.notifications,
                R.drawable.message,
                R.drawable.add_user,
                R.drawable.add_user,
                R.drawable.logout,
                R.drawable.about_us,
        };

        CustomListAdapter adapter=new CustomListAdapter(this, itemname, imgid);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                String Slecteditem = itemname[+position];

                switch(Slecteditem) {
                    case "Notifications":
                        Intent notiIntent = new Intent(Child_MenuDrawerActivity.this, NotificationActivity.class);
                        startActivity(notiIntent);
                        break;
                    case "Send SOS":
                        // check first if child has a parent before sending SOS
                        if(parentID == null || parentNumber == null || parentNumber.equals("")) {
                            Toast.makeText(Child_MenuDrawerActivity.this, "Please add a parent first.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AlertDialog.Builder sosBuilder = new AlertDialog.Builder(Child_MenuDrawerActivity.this);
                        sosBuilder.setTitle("Are you sure you want to send an SOS to your parent?");

                        // Set up the buttons
                        sosBuilder.setPositiveButton("I am in trouble!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                FirebaseDatabase.getInstance().getReference().child("users").child(parentID).child("SOS").setValue(true);
                                Log.d("ChildMenu", parentNumber);
                                SmsManager smsManager = SmsManager.getDefault(); // uses the default sim in your phone
                               smsManager.sendTextMessage(parentNumber,null,"Help! I am in trouble!",null,null);

                            }
                        });
                        sosBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        sosBuilder.show();

                        break;
                    case "Add Parent Account":
                        AlertDialog.Builder builder = new AlertDialog.Builder(Child_MenuDrawerActivity.this);
                        builder.setTitle("Add a child");

                        LinearLayout layout = new LinearLayout(Child_MenuDrawerActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        final EditText input_parentid = new EditText(Child_MenuDrawerActivity.this);

                        input_parentid.setInputType(InputType.TYPE_CLASS_TEXT);
                        input_parentid.setHint("Parent Email");
                        input_parentid.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                        layout.addView(input_parentid);

                        builder.setView(layout);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final String parentEmail  = input_parentid.getText().toString().trim();
                                final DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");

                                if(parentEmail == null || parentEmail.equals("")) {
                                    Toast.makeText(Child_MenuDrawerActivity.this, "Please do not leave the field empty",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                progress.show();
                                mDatabase.child("users").orderByChild("email").equalTo(parentEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot parentSnapshot) {
                                        if(parentSnapshot.exists()) {
                                            Db_user user = parentSnapshot.getValue(Db_user.class);
                                            // check if user is a child
                                            if(user.getRole().equals("Parent")) {

                                                    Map<String, Object> parentUpdate = new HashMap<String, Object>(); //
                                                    parentUpdate.put("Parent", parentID);
                                                    users.child(currentUserID).updateChildren(parentUpdate);
                                                    Toast.makeText(Child_MenuDrawerActivity.this, "Parent has been added.",Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(Child_MenuDrawerActivity.this, "Person is not a parent.",Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(Child_MenuDrawerActivity.this, "Parent does not exist.",Toast.LENGTH_SHORT).show();
                                        }

                                        progress.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d("error","error");
                                        Toast.makeText(Child_MenuDrawerActivity.this, "An error occurred.",Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();

                        break;

                    case "My ID":
                        AlertDialog.Builder builderID = new AlertDialog.Builder(Child_MenuDrawerActivity.this);
                        builderID.setTitle("My ID");
                        builderID.setMessage(currentUserID);

                        builderID.setPositiveButton("Send as a message", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                                smsIntent.setType("vnd.android-dir/mms-sms");
                                smsIntent.putExtra("address", "");
                                smsIntent.putExtra("sms_body", currentUserID);
                                startActivity(smsIntent);
                            }
                        });

                        builderID.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builderID.show();
                        break;
                    case "Logout":
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                        startActivity(intent);
                        break;
                    case "About Us":
                        final Intent i = new Intent(Child_MenuDrawerActivity.this, AboutUsActivity.class);
                        startActivity(i);
                        break;
                }
            }
        });
        
		findViewById(R.id.android_layout).setOnTouchListener(new OnSwipeTouchListener(Child_MenuDrawerActivity.this) {
            public void onSwipeTop() {
            }

            public void onSwipeRight() {}
			
            public void onSwipeLeft() {
                onBackPressed();
                overridePendingTransition(R.anim.right2left_enter, R.anim.right2left_exit);
            }
			
            public void onSwipeBottom() {
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
