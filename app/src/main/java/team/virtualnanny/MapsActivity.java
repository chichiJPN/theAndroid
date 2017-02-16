package team.virtualnanny;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static double StepSize = 0.6;
    public final static String PREFS_FILE_NAME = "MyAppPrefs";
    public final static String LAT_LNG_FILE_NAME = "Coords.txt";
    private final int PERMISSIONS_ID = 1234;
    private GoogleMap mMap;
    private Button btnStartCounting;
    private Button btnStopCounting;
    private Button btnShowFile;
    private TextView countStepsTextView;
    private Handler refreshStepsHandler;
    private RefreshThread refreshThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		Log.d("yes","onCreate");
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestPermissionAccessLocation();
    }

    @Override
    protected void onDestroy() {
		Log.d("yes","onDestroy");

        super.onDestroy();
        if(refreshThread != null){
            refreshThread.work = false;
        }
    }

    private void startWork(){
		Log.d("yes","StartWork");
		
        initUI();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private void initUI(){
		Log.d("yes","initUI");
		
        btnStartCounting = (Button) findViewById(R.id.btnStartCounting);
        btnStopCounting = (Button) findViewById(R.id.btnStopCounting);
        btnShowFile = (Button) findViewById(R.id.btnShowFile);
        countStepsTextView = (TextView) findViewById(R.id.countStepsTextView);
        btnStartCounting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCounting();
            }
        });
        btnStopCounting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopCounting();
            }
        });
        btnShowFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        refreshStepCount();
        refreshStepsHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                refreshStepCount();
                return true;
            }
        });
        startRefreshingThread();
        SharedPreferences sp = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        boolean isCounting = sp.getBoolean("state", false);
        if(isCounting){
            btnStartCounting.setEnabled(false);
            btnStopCounting.setEnabled(true);
        }else {
            btnStartCounting.setEnabled(true);
            btnStopCounting.setEnabled(false);
        }
    }
    private void refreshStepCount(){
		Log.d("yes","refreshStepCount");
		
        animateAndZoomToMyLocation();
        SharedPreferences sp = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        double stepCount = Double.parseDouble(sp.getString("distance", "0"))/StepSize;
        try {
            countStepsTextView.setText( "Steps " + (int)stepCount);
        }catch (Exception e){}

    }
    private void animateAndZoomToMyLocation(){
		Log.d("yes","animateAndZoomToMyLocation");
		
        if(mMap == null)
            return;
        Location location = mMap.getMyLocation();
        if(location == null)
            return;
        CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(
                location.getLatitude(),
                location.getLongitude()));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);
        Toast.makeText(this, "Latitude:"+location.getLatitude()+ "  Longitude:"+location.getLongitude(), Toast.LENGTH_SHORT).show();

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
    }
    private void startCounting(){
		Log.d("yes","startCounting");
		
        SharedPreferences sp = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        boolean isCounting = sp.getBoolean("state", false);
        if(isCounting){
            Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
            return;
        }
        MainService.deleteFile();
        sp.edit().putBoolean("state", true).commit();
        if(mMap != null ){
            mMap.clear();
        }
        try {
            countStepsTextView.setText("Num Steps" + " 0");
        }catch (Exception e){}
        Intent intent = new Intent(getApplicationContext(), MainService.class);
        startService(intent);
        startRefreshingThread();
        btnStartCounting.setEnabled(false);
        btnStopCounting.setEnabled(true);
    }
    private void startRefreshingThread(){
		Log.d("yes","startRefreshingThread");
		
        if(refreshThread != null && refreshThread.isAlive()){
            refreshThread.work = false;
        }
        refreshThread = new RefreshThread();
        refreshThread.start();
    }
    private void stopRefreshingThread(){
		Log.d("yes","stopRefreshingThread");

        if(refreshThread != null && refreshThread.isAlive()){
            refreshThread.work = false;
        }
        refreshThread = null;
    }
    private void stopCounting(){
		Log.d("yes","stopCounting");
		
        SharedPreferences sp = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        boolean isCounting = sp.getBoolean("state", false);
        if(!isCounting){
            Toast.makeText(this, "Stopped counting", Toast.LENGTH_SHORT).show();
            return;
        }
        sp.edit().putBoolean("state", false).commit();
        sp.edit().remove("lat")
                .remove("lng")
                .remove("newValue")
                .remove("distance").commit();
        Intent intent = new Intent(getApplicationContext(), MainService.class);
        stopService(intent);
        stopRefreshingThread();
        if(mMap != null ){
            mMap.clear();
        }
        btnStopCounting.setEnabled(false);
        btnStartCounting.setEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
		Log.d("yes","onMapReady");
        mMap = googleMap;
        mMap.setTrafficEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionAccessLocation();
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                animateAndZoomToMyLocation();
            }
        });
    }

    private void requestPermissionAccessLocation(){
		Log.d("yes","requestPermissionAccessLocation");
        int permissionCheck1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED && permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,}, PERMISSIONS_ID);
            return;
        }else {
            startWork();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		Log.d("yes","onRequestPermissionResult");
        switch (requestCode) {
            case PERMISSIONS_ID: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startWork();
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("alert")
                            .setMessage("need_access_location_and_write_external_storage_permission")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setCancelable(false)
                            .create();
                    dialog.show();
                }
                return;
            }
        }
    }

    class RefreshThread extends Thread{
        volatile boolean work = true;
        LatLng latLng;
        public RefreshThread() {
        }

        @Override
        public void run() {
            while (work){
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SharedPreferences sp = getSharedPreferences(MapsActivity.PREFS_FILE_NAME, MODE_PRIVATE);
                if(!sp.getBoolean("newValue", false)){
                    continue;
                }
                sp.edit().putBoolean("newValue", false).commit();
                Double lat = Double.valueOf(sp.getString("lat", "-1000"));
                Double lng = Double.valueOf(sp.getString("lng", "-1000"));
                if(latLng!= null && lat > -999 && lng > -999 && latLng.latitude != lat && latLng.longitude != lng){
                    refreshStepsHandler.sendEmptyMessage(0);
                }
                latLng = new LatLng(lat, lng);
            }
        }
    }

}
