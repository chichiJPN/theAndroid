package team.virtualnanny;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
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

                                progress.dismiss();


                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                finish();
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
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

                /*

                if(username.equals("guardian")) {
                    Toast.makeText(getApplicationContext(), "Going to guardian page!",
                            Toast.LENGTH_SHORT).show();
                    final Intent i = new Intent(MainActivity.this, Guardian_ChildProfileOverviewActivity.class);
                    startActivity(i);

                } else if(username.equals("child")){
                    final Intent i = new Intent(MainActivity.this, Child_ChildOverviewActivity.class);
                    startActivity(i);
                    Toast.makeText(getApplicationContext(), "Going to child page!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Username is " + username + ".\nEnter 'guardian' as username to go to guardian page.\n Enter 'child' as username to go to child page.",
                            Toast.LENGTH_SHORT).show();
                }

                */


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

        /*

        Button button = (Button) findViewById(R.id.btn_dbwrite);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("message");

                myRef.setValue("Hello, World!");

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        Toast.makeText(getApplicationContext(), "Value is: " + value,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });

            }
        });
        */
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
