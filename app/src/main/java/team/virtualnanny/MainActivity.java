package team.virtualnanny;

import android.*;
import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progress = new ProgressDialog(MainActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        mAuth = FirebaseAuth.getInstance();

        checkAppPermissions();


/*
        // this part is for adding and removing values in the database. Please ignoere
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usersSnapshot) {
//                Map<String, Object> fenceProperties = new HashMap<String, Object>(); //
//                fenceProperties.put("SOS", false);
                for(DataSnapshot userSnapshot: usersSnapshot.getChildren()) {
                    String userKey = userSnapshot.getKey().toString();
                    Log.d("Main","Setting " + userKey);
                    if(userSnapshot.child("Fences").exists()) {
                        DataSnapshot FencesSnapshot = userSnapshot.child("Fences");
                        for(DataSnapshot fence: FencesSnapshot.getChildren()) {
                            String fenceName = fence.getKey().toString();
                            FirebaseDatabase.getInstance().getReference().child("users").child(userKey).child("Fences").child(fenceName).child("points").setValue(list);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
*/

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    progress.show();

                    FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Db_user dbuser = dataSnapshot.getValue(Db_user.class);
                                Intent intent;
                                if(dbuser.getRole().equals("Parent")) {
                                    intent = new Intent(getApplicationContext(), Guardian_ChildProfileOverviewActivity.class);
                                } else {
                                    intent = new Intent(getApplicationContext(), Child_ChildOverviewActivity.class);
                                }

                                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                                Map<String, Object> lastLogin = new HashMap<String, Object>(); //
                                lastLogin.put("lastLogin", currentDate);
                                FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).updateChildren(lastLogin);

                                progress.dismiss();


                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                finish();
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d("MainActivity",databaseError.getMessage());
                                Toast.makeText(getApplicationContext(), "An error occurred. Please try again",
                                        Toast.LENGTH_SHORT).show();
                                progress.dismiss();
                            }
                        });
                } else {
                    // User is signed out
                    Log.d("Main", "onAuthStateChanged:signed_out");
                }
            }
        };

        setContentView(R.layout.login);

        Button btnLogin = (Button) findViewById(R.id.btn_login); // gets button from layout xml
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                progress.show();

                EditText input_username = (EditText) findViewById(R.id.editText_username); // gets the field by ID from layout xml
                EditText input_password = (EditText) findViewById(R.id.editText_password);

                String email = input_username.getText().toString(); // gets the text from the input field and converts to string
                String password = input_password.getText().toString();

                mAuth.signInWithEmailAndPassword(email , password)
                              .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w("Login", "signInWithEmail", task.getException());
                                    Toast.makeText(MainActivity.this, "Login failed. Please ensure correct username and password",
                                            Toast.LENGTH_SHORT).show();
                                    progress.dismiss();
                                }
                            }
                        });
            }
        });

        TextView btnNoAccount = (TextView) findViewById(R.id.btn_noAccount);
        btnNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(MainActivity.this, RegisterAccountActivity.class);
                startActivity(i);

                Toast.makeText(getApplicationContext(), "No Account button clicked!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        TextView btnForgotPassword = (TextView) findViewById(R.id.textView_forgotPassword);
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(i);

                Toast.makeText(getApplicationContext(), "No Account button clicked!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAppPermissions() {
        int permissionCheck1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int permissionCheck2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck3 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED ||
            permissionCheck2 != PackageManager.PERMISSION_GRANTED ||
            permissionCheck3 != PackageManager.PERMISSION_GRANTED
            ) {

            String[] permissionsToAsk = new String[] {
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permissionsToAsk, 1234);
        }
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
