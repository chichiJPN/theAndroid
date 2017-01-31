package team.virtualnanny;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

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
                // ...
            }
        };

        Log.d("Main", "asdasdasdas");


        setContentView(R.layout.login);

        Button btnLogin = (Button) findViewById(R.id.btn_login); // gets button from layout xml
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText input_username = (EditText) findViewById(R.id.editText_username); // gets the field by ID from layout xml
                EditText input_password = (EditText) findViewById(R.id.editText_password);

                String username = input_username.getText().toString(); // gets the text from the input field and converts to string
                String password = input_password.getText().toString();

                // user pass verification here
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("user/potato");

        myRef.setValue("Hello, World!");
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
