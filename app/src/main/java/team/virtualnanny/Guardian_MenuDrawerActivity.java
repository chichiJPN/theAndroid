package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.key;


public class Guardian_MenuDrawerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    private String m_Text = "";
    private String currentUserID;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_menu_drawer);
        setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

        progress = new ProgressDialog(Guardian_MenuDrawerActivity.this);
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

        LinearLayout profileBar = (LinearLayout) findViewById(R.id.profileBar) ;

        profileBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Guardian_MenuDrawerActivity.this, Guardian_ProfileActivity.class);
                startActivity(i);
            }
        });

        ListView list;
        final String[] itemname ={
                "Notifications",
                "Add Child Account",
                "My parent ID",
                "Logout",
                "About Us"
        };

        Integer[] imgid={
                R.drawable.notifications,
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
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                final String Slecteditem = itemname[+position];
                /*
                *                 "Notifications",
                "Messages",
                "Add Child Account",
                "Logout",
                "About Us"
                * */
                switch(Slecteditem) {
                    case "Notifications":
                        Intent notiIntent = new Intent(Guardian_MenuDrawerActivity.this, NotificationActivity.class);
                        startActivity(notiIntent);
                        break;
                    case "Add Child Account":

                        AlertDialog.Builder builder = new AlertDialog.Builder(Guardian_MenuDrawerActivity.this);
                        builder.setTitle("Add a child");

                        LinearLayout layout = new LinearLayout(Guardian_MenuDrawerActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        final EditText input_childid = new EditText(Guardian_MenuDrawerActivity.this);

                        input_childid.setInputType(InputType.TYPE_CLASS_TEXT);
                        input_childid.setHint("Child Email");
                        input_childid.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                        layout.addView(input_childid);


                        builder.setView(layout);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final String childEmail  = input_childid.getText().toString().trim();
                                final DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");

                                if(childEmail.equals("")) {
                                    Toast.makeText(Guardian_MenuDrawerActivity.this, "Please do not leave the field empty",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                progress.show();
                                mDatabase.child("users").orderByChild("email").equalTo(childEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot childSnapshot) {

                                        if(childSnapshot.exists()) {

                                            for(DataSnapshot snapshot: childSnapshot.getChildren()) {
                                                childSnapshot= snapshot;
                                            }

                                            Log.d("user",childSnapshot.toString());
                                            Db_user user = childSnapshot.getValue(Db_user.class);
                                            final String childUID = childSnapshot.getKey();

                                            // check if user is a child
                                            if(user.getRole().equals("Child")) {

                                                users.child(currentUserID).child("children").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot children) {
                                                        // check if guardian has lesser than 3 children
                                                        if(children.getChildrenCount() < 3) { // if children are lesser than 3

                                                            String childKey = users.child(currentUserID).child("children").push().getKey(); // create a key
                                                            Map<String, Object> childUpdate = new HashMap<String, Object>(); //
                                                            childUpdate.put(childKey, childUID);

                                                            users.child(currentUserID).child("children").updateChildren(childUpdate);
                                                            Toast.makeText(Guardian_MenuDrawerActivity.this, "Child has been added.",Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            Toast.makeText(Guardian_MenuDrawerActivity.this, "Already have 3 children.",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {}
                                                });
                                            } else {
                                                Toast.makeText(Guardian_MenuDrawerActivity.this, "Person is not child.",Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(Guardian_MenuDrawerActivity.this, "Child does not exist.",Toast.LENGTH_SHORT).show();
                                        }

                                        progress.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d("error","error");
                                        Toast.makeText(Guardian_MenuDrawerActivity.this, "An error occurred.",Toast.LENGTH_SHORT).show();

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
                    case "My parent ID":
                        AlertDialog.Builder builderID = new AlertDialog.Builder(Guardian_MenuDrawerActivity.this);
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
                        final Intent i = new Intent(Guardian_MenuDrawerActivity.this, AboutUsActivity.class);
                        startActivity(i);
                        break;
                }
                Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.android_layout).setOnTouchListener(new OnSwipeTouchListener(Guardian_MenuDrawerActivity.this) {
            public void onSwipeTop() {}

            // left to right
            public void onSwipeRight() {
            }
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
