package team.virtualnanny;


import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Main", "onAuthStateChanged:signed_in:" + user.getUid());
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
				EditText editText_firstName = (EditText) findViewById(R.id.editText_firstName);
				EditText editText_lastName = (EditText) findViewById(R.id.editText_lastName);
				EditText editText_email = (EditText) findViewById(R.id.editText_email);
				EditText editText_phone = (EditText) findViewById(R.id.editText_phone);
				RadioGroup radiogrp_Gender = (RadioGroup) findViewById(R.id.radioGrp_gender);
                int selectedId = radiogrp_Gender.getCheckedRadioButtonId();
                RadioButton radiobtn_gender = (RadioButton) findViewById(selectedId);


				EditText editText_username = (EditText) findViewById(R.id.editText_username);
				EditText editText_password = (EditText) findViewById(R.id.editText_password);
                RadioGroup radioGrp_role = (RadioGroup) findViewById(R.id.radioGrp_role);
                int roleselectedId = radioGrp_role.getCheckedRadioButtonId();
                RadioButton radiobtn_role = (RadioButton) findViewById(selectedId);

				CheckBox checkBox_terms = (CheckBox) findViewById(R.id.checkBox_terms);
//                Log.d("Main", "onAuthStateChanged:signed_in:" + user.getUid());




                String email = editText_email.getText().toString();
                String password = editText_password.getText().toString();
                String firstName = editText_firstName.getText().toString();
                String lastName = editText_lastName.getText().toString();
                String phone = editText_phone.getText().toString();
                String gender = radiobtn_gender.getText().toString();
                String username = editText_username.getText().toString();
                String role = radiobtn_role.getText().toString();

                Log.d("Register", email );

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

                                // ...
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
