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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String gender;
    private String role;
    private String address;
    private ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progress = new ProgressDialog(RegisterAccountActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // check if user has registered
                    progress.show();

                    if(email != null) {
                        double lastLatitude = 10.2972; // UC coordinates
                        double lastLongitude = 123.8950; // UC coordinates
                        int numSteps = 0;
                        boolean enablePhone = false;
                        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        //2017-02-25

                        Db_user dbuser = new Db_user(firstName,lastName,email,phone,gender,role,address, enablePhone, lastLatitude, lastLongitude, numSteps,0,currentDate); //adds a new user to the database
                        mDatabase.child("users").child(user.getUid()).setValue(dbuser);
                        Db_limit dblimit = new Db_limit(false,24, false,false,false,false,false,false,false);
                        mDatabase.child("users").child(user.getUid()).child("limit").setValue(dblimit);
                    }

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

                                    progress.dismiss();

                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    finish();
                                    startActivity(intent);

                                    Log.d("Main", "onAuthStateChanged:signed_in: user id is " + dbuser.getRole());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                } else {
                    // User is signed out
                    Log.d("Main", "onAuthStateChanged:signed_out");
                }
            }
        };


        setContentView(R.layout.register_account);
        setTitle("Register a new account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

        Button btn_register = (Button) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.show();
				EditText editText_firstName = (EditText) findViewById(R.id.editText_firstName);
                EditText editText_lastName = (EditText) findViewById(R.id.editText_lastName);
                EditText editText_address = (EditText) findViewById(R.id.editText_address);
				EditText editText_email = (EditText) findViewById(R.id.editText_email);
				EditText editText_phone = (EditText) findViewById(R.id.editText_phone);
				RadioGroup radiogrp_Gender = (RadioGroup) findViewById(R.id.radioGrp_gender);
                int selectedId = radiogrp_Gender.getCheckedRadioButtonId();
                if(selectedId == -1) {
                    Toast.makeText(RegisterAccountActivity.this, "Please select a gender.",Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton radiobtn_gender = (RadioButton) findViewById(selectedId);


				EditText editText_username = (EditText) findViewById(R.id.editText_username);
				EditText editText_password = (EditText) findViewById(R.id.editText_password);
                RadioGroup radioGrp_role = (RadioGroup) findViewById(R.id.radioGrp_role);
                int roleselectedId = radioGrp_role.getCheckedRadioButtonId();
                if(roleselectedId == -1) {
                    Toast.makeText(RegisterAccountActivity.this, "Please select a role.",Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton radiobtn_role = (RadioButton) findViewById(roleselectedId);
				CheckBox checkBox_terms = (CheckBox) findViewById(R.id.checkBox_terms);
                email = editText_email.getText().toString().trim();
                password = editText_password.getText().toString().trim();
                firstName = editText_firstName.getText().toString().trim();
                lastName = editText_lastName.getText().toString().trim();
                phone = editText_phone.getText().toString().trim();
                gender = radiobtn_gender.getText().toString();
                role = radiobtn_role.getText().toString();
                address = editText_address.getText().toString().trim();
                if(!checkBox_terms.isChecked()) {
                    Toast.makeText(RegisterAccountActivity.this, "Please check terms and conditions.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(firstName.isEmpty() || lastName.isEmpty()) {
                    Toast.makeText(RegisterAccountActivity.this, "A name is empty.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(email.isEmpty()) {
                    Toast.makeText(RegisterAccountActivity.this, "Email is empty.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(phone.isEmpty()) {
                    Toast.makeText(RegisterAccountActivity.this, "Phone number is empty.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.isEmpty()) {
                    Toast.makeText(RegisterAccountActivity.this, "Password is empty.",Toast.LENGTH_SHORT).show();
                    return;
                } else if(password.length() < 8) {
                    Toast.makeText(RegisterAccountActivity.this, "Password needs to be 8 characters long.",Toast.LENGTH_SHORT).show();
                }

                if(address.isEmpty()) {
                    Toast.makeText(RegisterAccountActivity.this, "Address is empty.",Toast.LENGTH_SHORT).show();
                    return;
                }



                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterAccountActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("Register", "createUserWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {

                                    Toast.makeText(RegisterAccountActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                                progress.dismiss();
                            }
                        });

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
