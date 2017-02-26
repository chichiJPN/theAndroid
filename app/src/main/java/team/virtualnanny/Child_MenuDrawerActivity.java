package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_menu_drawer);
        setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

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
                "Messages",
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
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                String Slecteditem = itemname[+position];
                /*
                *                 "Notifications",
                "Messages",
                "Add Child Account",
                "Logout",
                "About Us"
                * */
                switch(Slecteditem) {
                    case "Notifications":
                        break;
                    case "Send SOS":
                        break;
                    case "Add Parent Account":
                        AlertDialog.Builder builder = new AlertDialog.Builder(Child_MenuDrawerActivity.this);
                        builder.setTitle("Add a child");

                        LinearLayout layout = new LinearLayout(Child_MenuDrawerActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        final EditText input_parentid = new EditText(Child_MenuDrawerActivity.this);

                        input_parentid.setInputType(InputType.TYPE_CLASS_TEXT);
                        input_parentid.setHint("Parent ID");
                        input_parentid.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                        layout.addView(input_parentid);

                        builder.setView(layout);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final String parentID  = input_parentid.getText().toString().trim();
                                final DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");
                                progress.show();
                                //check if parent ID exists
                                users.child(parentID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {
                                            Db_user User = dataSnapshot.getValue(Db_user.class);
                                            Log.d("Role",User.getRole());
                                            if(User.getRole().equals("Parent")) {
                                                final String currentUserID = mAuth.getCurrentUser().getUid();

                                                Map<String, Object> parentUpdate = new HashMap<String, Object>(); //
                                                parentUpdate.put("Parent", parentID);
                                                users.child(currentUserID).updateChildren(parentUpdate);

                                                Toast.makeText(Child_MenuDrawerActivity.this, dataSnapshot.child("firstName").getValue() + " is now your parent.",Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Child_MenuDrawerActivity.this, "Person is not a parent.",Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(Child_MenuDrawerActivity.this, "Parent does not exist.",Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                });
                                progress.dismiss();

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
                        LinearLayout layoutID = new LinearLayout(Child_MenuDrawerActivity.this);
                        layoutID.setOrientation(LinearLayout.VERTICAL);
                        final TextView myID = new TextView(Child_MenuDrawerActivity.this);

                        myID.setText(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        myID.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                        myID.setPadding(0,50,0,30);
                        layoutID.addView(myID);

                        builderID.setView(layoutID);

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
                Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();
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
