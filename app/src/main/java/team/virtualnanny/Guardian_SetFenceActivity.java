package team.virtualnanny;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

public class Guardian_SetFenceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.guardian_set_fence);
        setTitle("Set Geo fence");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.geofence, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_add:
                Toast.makeText(getApplicationContext(), "Click on anywhere on the map to add the center of the fence",
                        Toast.LENGTH_LONG).show();

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {


                        Location location = new Location("Test");
                        location.setLatitude(point.latitude);
                        location.setLongitude(point.longitude);
                        location.setTime(new Date().getTime()); //Set time as current Date

                        //Convert Location to LatLng
                        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(newLatLng)
                                .title(newLatLng.toString());

                        Log.d("coordinates", "latitude" + point.latitude + "longitude" + point.longitude);

                        mMap.addMarker(markerOptions);
                        AlertDialog.Builder builder = new AlertDialog.Builder(Guardian_SetFenceActivity.this);
                        builder.setTitle("Add Fence");

                        LinearLayout layout = new LinearLayout(Guardian_SetFenceActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        final EditText input_fenceName = new EditText(Guardian_SetFenceActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input_fenceName.setInputType(InputType.TYPE_CLASS_TEXT);
                        input_fenceName.setHint("Fence Name");
                        layout.addView(input_fenceName);

                        final EditText input_radius = new EditText(Guardian_SetFenceActivity.this);
                        input_radius.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input_radius.setHint("Input Radius");
                        layout.addView(input_radius);

                        RadioGroup rg = new RadioGroup(Guardian_SetFenceActivity.this); //create the RadioGroup
                        rg.setOrientation(RadioGroup.HORIZONTAL);//or RadioGroup.VERTICAL

                        final RadioButton rb_safezone = new RadioButton(Guardian_SetFenceActivity.this);
                        rb_safezone.setText(R.string.fence_safezone);
                        rg.addView(rb_safezone);

                        final RadioButton rb_dangerzone = new RadioButton(Guardian_SetFenceActivity.this);
                        rb_dangerzone.setText(R.string.fence_dangerzone);
                        rg.addView(rb_dangerzone);


                        layout.addView(rg);
                        builder.setView(layout);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m_Text = input_fenceName.getText().toString();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog dialog = builder.create();
                        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

                        wmlp.gravity = Gravity.BOTTOM;
                        dialog.show();

                    }
                });
                break;
            case R.id.action_delete:
                Toast.makeText(getApplicationContext(), "Delete Button Clicked!",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_search:
                Toast.makeText(getApplicationContext(), "Search Button Clicked!",
                    Toast.LENGTH_SHORT).show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

}
