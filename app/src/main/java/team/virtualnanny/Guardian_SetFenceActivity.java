package team.virtualnanny;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Guard;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class Guardian_SetFenceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;

    private List<MapFence> mapFences;
/*
    private List<Db_fence> existingFences;
    private List<Marker> existingMarkers;
    private List<Circle> existingCircles;
    private List<Polygon> existingPolygons;
*/

    private String currentUserID;
    private String inputFenceName;
    private int safetyID;
    private Marker lastMarkerClicked = null;

    private List<Circle> tempPoints;
    private List<Polyline> tempPolylines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.guardian_set_fence);
        setTitle("Set Geo fence");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

        initComponents();

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initComponents() {
        tempPoints = new ArrayList<Circle>();
        tempPolylines = new ArrayList<Polyline>();

        mapFences = new ArrayList<MapFence>();

        progress = new ProgressDialog(Guardian_SetFenceActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
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
                addFence();
                break;

            case R.id.action_delete:
                deleteFence();
                break;

            case R.id.action_search:
                searchFence();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addFence() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Guardian_SetFenceActivity.this);
        builder.setTitle("Choose fence type");

        LinearLayout layout = new LinearLayout(Guardian_SetFenceActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText editText_fenceName = new EditText(Guardian_SetFenceActivity.this);
        editText_fenceName .setInputType(InputType.TYPE_CLASS_TEXT);
        editText_fenceName.setHint("Fence Name");
        layout.addView(editText_fenceName);


        final RadioGroup rg = new RadioGroup(Guardian_SetFenceActivity.this); //create the RadioGroup
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
        builder.setPositiveButton("Circular", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputFenceName = editText_fenceName.getText().toString().trim();
                if(inputFenceName.isEmpty()) {
                    Toast.makeText(Guardian_SetFenceActivity.this, "Please do not leave fence Name empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                int checkedButton = rg.getCheckedRadioButtonId();
                View radioButton = rg.findViewById(checkedButton);
                safetyID = rg.indexOfChild(radioButton) + 1;

                if(safetyID == -1) {
                    Toast.makeText(Guardian_SetFenceActivity.this, "Please select fence safety",Toast.LENGTH_SHORT).show();
                }

                addCircularFence();
            }
        });

        builder.setNeutralButton("Polygon",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputFenceName = editText_fenceName.getText().toString().trim();
                if(inputFenceName.isEmpty()) {
                    Toast.makeText(Guardian_SetFenceActivity.this, "Please do not leave fence Name empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                inputFenceName = editText_fenceName.getText().toString().trim();
                if(inputFenceName.isEmpty()) {
                    Toast.makeText(Guardian_SetFenceActivity.this, "Please do not leave fence Name empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                int checkedButton = rg.getCheckedRadioButtonId();
                View radioButton = rg.findViewById(checkedButton);
                safetyID = rg.indexOfChild(radioButton) + 1;

                if(safetyID == -1) {
                    Toast.makeText(Guardian_SetFenceActivity.this, "Please select fence safety",Toast.LENGTH_SHORT).show();
                }

                addPolygonFence();
            }


        });
        builder.show();
    }

    private void deleteFence() {
        // check if there was a marker clicked
        if(!lastMarkerClicked.equals(null)) {

            for(int x = 0; x < mapFences.size();x++) {
                if(lastMarkerClicked.equals(mapFences.get(x).getMarker())) {
                    AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(Guardian_SetFenceActivity.this);
                    deleteBuilder.setTitle("Are you sure you want to delete Geofence '"+lastMarkerClicked.getTitle()+"'");

                    // Set up the buttons
                    final int finalX = x;
                    deleteBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String fenceName = mapFences.get(finalX).getMarker().getTitle();

                            mapFences.get(finalX).getMarker().remove();
                            if(mapFences.get(finalX).getDb_fence().getType().equals("Circle")) {
                                mapFences.get(finalX).getCircle().remove();
                            } else {
                                mapFences.get(finalX).getPolygon().remove();
                            }

                            mapFences.remove(finalX);

                            mDatabase.child("users").child(currentUserID).child("Fences").child(fenceName).removeValue();
                        }
                    });
                    deleteBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    deleteBuilder.show();

                    break;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please click on a marker and click on the delete button",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void searchFence() {
        AlertDialog.Builder searchBuilder = new AlertDialog.Builder(Guardian_SetFenceActivity.this);
        searchBuilder.setTitle("Please input the name of the geolocation:");

        final EditText input = new EditText(Guardian_SetFenceActivity.this);

        searchBuilder.setView(input);

        searchBuilder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fenceName = input.getText().toString();
                Log.d("Fence", fenceName);
                for(MapFence mapfence: mapFences) {
                    Marker marker = mapfence.getMarker();
                    if(marker.getTitle().equals(fenceName)) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 12.0f));
                        return;
                    }
                }

                Toast.makeText(getApplicationContext(), fenceName + " does not exist",
                        Toast.LENGTH_LONG).show();

            }
        });
        searchBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        searchBuilder.show();
    }

    private void addPolygonFence() {
        Toast.makeText(getApplicationContext(), "Click on points on the map to create your fence. Long click the map to add the shape.",
                Toast.LENGTH_LONG).show();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng newPoint) {

                // check first if point is in map
                if(isPointInPolygon(newPoint) || isPointInCircle(newPoint)) {
                    Toast.makeText(getApplicationContext(), "Point is in a fence.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // check if there are no points on the map then add a point
                if(tempPoints.size() == 0) {

					CircleOptions circleOption = new CircleOptions()
							.center(newPoint)
							.radius(1)
							.fillColor(Color.GREEN)
							.strokeColor(Color.GREEN)
							.zIndex(20);
					final Circle point = mMap.addCircle(circleOption);
					tempPoints.add(point);
					
				} else {

                    // get the position of the 2nd to the last point in the circle
					
                    LatLng lastPointPosition = tempPoints.get(tempPoints.size() - 1).getCenter();

                    // check if the new line will intersect with the previous lines
                    boolean line_intersects = false;

                    // loop through all the previous lines while comparing them with the new line
                    for(Polyline templine : tempPolylines) {
                        List<LatLng> points = templine.getPoints();
                        LatLng firstpoint = points.get(0);
                        LatLng secondpoint = points.get(1);

                        // if the lines interesect, set variable to true then break
                        if(line_intersects(lastPointPosition.latitude, lastPointPosition.longitude,
                                            newPoint.latitude, newPoint.longitude,
                                            firstpoint.latitude, firstpoint.longitude,
                                            secondpoint.latitude, secondpoint.longitude)){
                            line_intersects = true;
                            break;
                        }
                    }

                    if(line_intersects == true) {
                        Toast.makeText(getApplicationContext(), "Lines cannot interesect!",
                                Toast.LENGTH_SHORT).show();
                    } else {
						
						// add a new point to the map
						CircleOptions circleOption = new CircleOptions()
								.center(newPoint)
								.radius(1)
								.fillColor(Color.GREEN)
								.strokeColor(Color.GREEN)
								.zIndex(20);
						final Circle point = mMap.addCircle(circleOption);
						tempPoints.add(point);
						
                        PolylineOptions polylineoption = new PolylineOptions().add(lastPointPosition, newPoint)
                                .width(5)
                                .color(Color.GREEN);
                        Polyline line = mMap.addPolyline(polylineoption);
                        tempPolylines.add(line);
                    }
                }
            }
        });

        // add polygon to database if long click
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {

                // number of points need to be 2 or more
                if(tempPoints.size() > 1) {

                    // connect the last circle and the first circle
                    LatLng lastPointPosition = tempPoints.get(tempPoints.size() - 1).getCenter();
                    LatLng firstPointPosition = tempPoints.get(0).getCenter();
                    Log.d("last point", lastPointPosition.toString());
                    Log.d("first point", firstPointPosition.toString());
                    boolean line_intersects = false;

                    // loop through all the previous lines while comparing them with the new line
                    for(Polyline templine : tempPolylines) {
                        List<LatLng> points = templine.getPoints();

                        LatLng b_firstpoint = points.get(0);
                        LatLng b_secondpoint = points.get(1);

                        // if the lines interesect, set variable to true then break
                        if(line_intersects(lastPointPosition.latitude, lastPointPosition.longitude,
                                           firstPointPosition .latitude, firstPointPosition.longitude,
                                            b_firstpoint.latitude, b_firstpoint.longitude,
                                            b_secondpoint.latitude, b_secondpoint.longitude)){
                            line_intersects = true;
                            break;
                        }
                    }

                    // if the closing line does not intersect
                    if(line_intersects == false ){
                        // then add the last line to the list
                        PolylineOptions polylineoption = new PolylineOptions().add(lastPointPosition,firstPointPosition )
                                .width(5)
                                .color(Color.GREEN);
                        Polyline line = mMap.addPolyline(polylineoption);
                        tempPolylines.add(line);

                        PolygonOptions polygonoptions = new PolygonOptions();
                        HashMap<String, Double> pointsLatitude = new HashMap<String, Double>();
                        HashMap<String, Double> pointsLongitude = new HashMap<String, Double>();

                        int x = 0;
                        for(Polyline polyline: tempPolylines) {
                            List<LatLng> points = polyline.getPoints();
                            polygonoptions.add(points.get(0),points.get(1));

                            pointsLatitude.put(""+x,points.get(0).latitude);
                            pointsLongitude.put(""+x,points.get(0).longitude);
//                            listPoints.add(new Double[]{points.get(0).latitude,points.get(1).longitude});
                            x++;
                        }

                        if(safetyID == 1) {
                            polygonoptions.strokeColor(Color.GREEN);
                        } else {
                            polygonoptions.strokeColor(Color.RED);
                        }

                        Polygon mapPolygon = mMap.addPolygon(polygonoptions);

                        Db_fence fence = new Db_fence(0, safetyID,0,0,"Polygon");

                        mDatabase.child("users").child(currentUserID).child("Fences").child(inputFenceName).setValue(fence);
                        mDatabase.child("users").child(currentUserID).child("Fences").child(inputFenceName).child("points").child("latitude").setValue(pointsLatitude);
                        mDatabase.child("users").child(currentUserID).child("Fences").child(inputFenceName).child("points").child("longitude").setValue(pointsLongitude);

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(mapPolygon.getPoints().get(0))
                                .title(inputFenceName);

                        final Marker marker = mMap.addMarker(markerOptions);

                        MapFence mapfence = new MapFence(
                                fence,
                                marker,
                                mapPolygon,
                                null
                        );

                        mapFences.add(mapfence);

                        Log.d("Points",mapPolygon.getPoints().toString());

                        // removes the listener on map
                        mMap.setOnMapClickListener(null);
                        mMap.setOnMapLongClickListener(null);

                        for(Circle circle : tempPoints) { circle.remove(); }
                        for(Polyline thepolyline: tempPolylines) { thepolyline.remove();}

                        tempPoints.clear();
                        tempPolylines.clear();
                    } else {
                        Toast.makeText(getApplicationContext(), "Line intersects!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Click on points on the map to create your fence. Long click the map to add the shape.",
                            Toast.LENGTH_SHORT).show();
                }
                Log.d("asdas","I was long clicked");
            }
        });
    }

    private void addCircularFence() {
        Toast.makeText(getApplicationContext(), "Click on anywhere on the map to add the center of the fence",
                Toast.LENGTH_LONG).show();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if(isPointInPolygon(point) || isPointInCircle(point)) {
                    Toast.makeText(getApplicationContext(), "Point is in a fence.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(point)
                        .title(inputFenceName);

                final Marker marker = mMap.addMarker(markerOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(Guardian_SetFenceActivity.this);
                builder.setTitle("Add Fence");

                LinearLayout layout = new LinearLayout(Guardian_SetFenceActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                CircleOptions circleOptions = new CircleOptions()
                        .center(point)
                        .radius(0)
                        .zIndex(20);
                final Circle mapCircle = mMap.addCircle(circleOptions);

                if(safetyID == 1) {
                    mapCircle.setStrokeColor(Color.GREEN);
                } else {
                    mapCircle.setStrokeColor(Color.RED);
                }


                final SeekBar input_radius = new SeekBar(Guardian_SetFenceActivity.this);
                input_radius.setMax(1000);
                input_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Log.d("seekbar", ""+progress);
                        mapCircle.setRadius(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                //input_radius.setInputType(InputType.TYPE_CLASS_NUMBER);
                //input_radius.setHint("Input Radius");
                layout.addView(input_radius);

                builder.setView(layout);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final double radius = mapCircle.getRadius();
                        final double newMarkerLongitude = marker.getPosition().longitude;
                        final double newMarkerLatitude = marker.getPosition().latitude;
                        boolean flag = true;

                        // latitude is y
                        // longitude is x
                        // checks if fences will overlap
                        if(mapFences != null && !mapFences.isEmpty()) {
                            for (MapFence mapFence : mapFences) {
                                Db_fence fence = mapFence.getDb_fence();

                                double x2 = newMarkerLongitude;
                                double x1 = fence.getLongitude();
                                double y2 = newMarkerLatitude;
                                double y1 = fence.getLatitude();

                                Location loc1 = new Location("");
                                loc1.setLatitude(fence.getLatitude());
                                loc1.setLongitude(fence.getLongitude());

                                Location loc2 = new Location("");
                                loc2.setLatitude(newMarkerLatitude);
                                loc2.setLongitude(newMarkerLongitude);

                                float distance = loc1.distanceTo(loc2);
//                                        double distance = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
                                double radius1 = fence.getRadius();
                                double radius2 = mapCircle.getRadius();

                                Log.d("radius1",""+radius1);
                                Log.d("radius2",""+radius2);
                                Log.d("distance",""+distance);

                                // if the fences overlap
                                if (distance < radius1 + radius2) {
                                    Toast.makeText(Guardian_SetFenceActivity.this, "Circles are overlapping", Toast.LENGTH_SHORT).show();
                                    flag = false;
                                    break;
                                }
                            }
                        }

                        if(flag == false ) {
                            marker.remove();
                            mapCircle.remove();
                            return;
                        }

                        progress.show();
                        final DatabaseReference users = mDatabase.child("users");

                        users.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {



                                Map<String, Object> fenceProperties = new HashMap<String, Object>(); //
                                fenceProperties.put("radius", radius);
                                fenceProperties.put("safety", safetyID);
                                fenceProperties.put("type", "Circle");
                                fenceProperties.put("longitude",newMarkerLongitude);
                                fenceProperties.put("latitude",newMarkerLatitude);

                                users.child(currentUserID).child("Fences").child(inputFenceName).updateChildren(fenceProperties);
                                marker.setTitle(inputFenceName);
                                Toast.makeText(Guardian_SetFenceActivity.this, "Fence has been added.",Toast.LENGTH_SHORT).show();

                                MapFence mapFence = new MapFence(
                                        new Db_fence(radius,safetyID,newMarkerLongitude,newMarkerLatitude,"Circle"),
                                        marker,
                                        null,
                                        mapCircle
                                );
                                mapFences.add(mapFence);

                                progress.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                marker.remove();
                                mapCircle.remove();
                                progress.dismiss();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        marker.remove();
                        mapCircle.remove();
                        mMap.setOnMapClickListener(null);
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

                wmlp.gravity = Gravity.BOTTOM;
                dialog.show();

            }
        });

    }

    public boolean isPointInCircle(LatLng point) {
        float[] distance = new float[2];

        for(MapFence mapfence : mapFences) {
            if(mapfence.getDb_fence().getType().equals("Circle")){
                Circle circle = mapfence.getCircle();
                Location.distanceBetween( point.latitude, point.longitude,
                        circle.getCenter().latitude, circle.getCenter().longitude, distance);
                if( distance[0] <= circle.getRadius()  ){
                    return true;
                }
            }
        }
        return false;
    }

    // http://stackoverflow.com/questions/26014312/identify-if-point-is-in-the-polygon
    public boolean isPointInPolygon(LatLng point) {

        boolean flag = false;
        for(int x = 0 ;x < mapFences.size() - 1; x++) {
            if(mapFences.get(x).getDb_fence().getType().equals("Polygon")) {
                List<LatLng> vertices = mapFences.get(x).getPolygon().getPoints();
                int intersectCount = 0;

                for (int j = 0; j < vertices.size() - 1; j++) {
                    if (rayCastIntersect(point, vertices.get(j), vertices.get(j + 1))) {
                        intersectCount++;
                    }
                }
                // odd = inside, even = outside;
                flag = ((intersectCount % 2) == 1);
            }

            if(flag == true) {
                break;
            }
        }

        return flag;
    }

    public boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {

        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY)
                || (aX < pX && bX < pX)) {
            return false; // a and b can't both be above or below pt.y, and a or
            // b must be east of pt.x
        }

        double m = (aY - bY) / (aX - bX); // Rise over run
        double bee = (-aX) * m + aY; // y = mx + b
        double x = (pY - bee) / m; // algebra is neat!

        return x > pX;
    }

    public boolean line_intersects(double p0_x, double p0_y,
                                    double p1_x, double p1_y,
                                    double p2_x, double p2_y,
                                    double  p3_x,double  p3_y) {

        double s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;
        s1_y = p1_y - p0_y;
        s2_x = p3_x - p2_x;
        s2_y = p3_y - p2_y;

        double s, t;
        s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s > 0 && s < 1 && t > 0 && t < 1)
        {
            // Collision detected
            return true;
        }

        return false; // No collision
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
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                lastMarkerClicked = marker;
                return false;
            }
        });


        LatLng upCebu = new LatLng(10.3384, 123.9118);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(upCebu));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(upCebu, 12.0f));

        progress.show();
        mDatabase.child("users").child(currentUserID).child("Fences").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot fenceSnapshot : dataSnapshot.getChildren()) {
                        Db_fence fence = fenceSnapshot.getValue(Db_fence.class);

                        String fenceName = fenceSnapshot.getKey();
                        Log.d("Fence",fenceName);

                        if(fence.getType().equals("Circle")) {
                            double fenceLatitude = fence.getLatitude();
                            double fenceLongitude = fence.getLongitude();

                            LatLng newLatLng = new LatLng(fenceLatitude, fenceLongitude);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(newLatLng)
                                    .title(fenceName);

                            final Marker marker = mMap.addMarker(markerOptions);

                            CircleOptions circleOptions = new CircleOptions()
                                    .center(newLatLng)
                                    .strokeColor(fence.getSafety() == 1 ? Color.GREEN : Color.RED)
                                    .radius(fence.getRadius())
                                    .zIndex(20);
                            final Circle mapCircle = mMap.addCircle(circleOptions);

                            MapFence mapFence = new MapFence(
                                    fence,
                                    marker,
                                    null,
                                    mapCircle
                            );
                            mapFences.add(mapFence);

                        } else if(fence.getType().equals("Polygon")) {
                            DataSnapshot latitudeSnapshot = fenceSnapshot.child("points").child("latitude");
                            DataSnapshot longitudeSnapshot = fenceSnapshot.child("points").child("longitude");
                            List<Double> listLatitudes = new ArrayList<Double>();

                            List<Double> listLongitudes = new ArrayList<Double>();


                            for(DataSnapshot d_latitude : latitudeSnapshot.getChildren()) {
                                String latitude = d_latitude.getValue().toString();
                                listLatitudes.add(Double.parseDouble(latitude));

                            }
                            for(DataSnapshot d_longitude : longitudeSnapshot.getChildren()) {
                                String longitude = d_longitude.getValue().toString();
                                listLongitudes.add(Double.parseDouble(longitude));
                            }

                            PolygonOptions polygonoptions = new PolygonOptions();

                            for(int x = 0 ;x < listLatitudes.size() - 1; x++) {
                                LatLng firstPoint = new LatLng(listLatitudes.get(x),listLongitudes.get(x));
                                LatLng secondPoint = new LatLng(listLatitudes.get(x + 1),listLongitudes.get(x + 1));
                                polygonoptions.add(firstPoint,secondPoint);
                            }

                            // set the first and last point
                            LatLng firstPoint = new LatLng(listLatitudes.get(0),listLongitudes.get(0));
                            LatLng secondPoint = new LatLng(listLatitudes.get(listLatitudes.size() - 1),listLongitudes.get(listLatitudes.size() - 1));
                            polygonoptions.add(secondPoint,firstPoint);


                            if(fence.getSafety() == 1) {
                                polygonoptions.strokeColor(Color.GREEN);
                            } else {
                                polygonoptions.strokeColor(Color.RED);
                            }

                            Polygon mapPolygon = mMap.addPolygon(polygonoptions);

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(mapPolygon.getPoints().get(0))
                                    .title(fenceName);

                            final Marker marker = mMap.addMarker(markerOptions);

                            MapFence mapfence = new MapFence(
                                    fence,
                                    marker,
                                    mapPolygon,
                                    null
                            );

                            mapFences.add(mapfence);
                        }
                    }
                }
                progress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.dismiss();
            }
        });

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
