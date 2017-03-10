package team.virtualnanny;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Guard;
import java.util.HashMap;
import java.util.Map;


public class Guardian_EditProfileActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String currentUserID;

    private EditText editText_firstName;
    private EditText editText_lastName;
    private EditText editText_address;
    private EditText editText_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_edit_profile);
        setTitle("Edit User Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black


        initComponents();

        Button btn_savechanges = (Button) findViewById(R.id.btn_savechanges);
        btn_savechanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = editText_firstName.getText().toString();
                String lastName = editText_lastName.getText().toString();
                String address = editText_address.getText().toString();
                String phoneNumber = editText_phone.getText().toString();

                if(firstName.equals("")) {
                    Toast.makeText(getApplicationContext(), "First Name must not be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(lastName.equals("")) {
                    Toast.makeText(getApplicationContext(), "Last Name must not be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(address.equals("")) {
                    Toast.makeText(getApplicationContext(), "Address must not be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(phoneNumber.equals("")) {
                    Toast.makeText(getApplicationContext(), "Phone number must not be empty",
                            Toast.LENGTH_SHORT).show();
                    return;

                    // check if phone number has only numbers using regular expression
                }else if(!phoneNumber.matches("[0-9]+")) {
                    Toast.makeText(Guardian_EditProfileActivity.this, "Phone number should only contain numbers.",Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> newProfile= new HashMap<String, Object>();
                newProfile.put("phone",phoneNumber);
                newProfile.put("address",address);
                newProfile.put("firstName",firstName);
                newProfile.put("lastName",lastName);

                mDatabase.child("users").child(currentUserID).updateChildren(newProfile);
                Toast.makeText(Guardian_EditProfileActivity.this, "Profile has been editted.",
                        Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }

    private void initComponents() {
        progress = new ProgressDialog(Guardian_EditProfileActivity.this);
        progress.setTitle("Loading");
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
        currentUserID = mAuth.getCurrentUser().getUid();

        editText_firstName = (EditText) findViewById(R.id.editText_firstName);
        editText_lastName = (EditText) findViewById(R.id.editText_lastName);
        editText_address = (EditText) findViewById(R.id.editText_address);
        editText_phone = (EditText) findViewById(R.id.editText_phone);

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
