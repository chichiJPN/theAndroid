package team.virtualnanny;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;


public class Guardian_SetLimitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_set_limit);
        setTitle("Limit Screen Time");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black
        Button btn_set = (Button) findViewById(R.id.btn_set);
        final TextView repeatSunday = (TextView) findViewById(R.id.repeatSunday);
        final TextView repeatMonday = (TextView) findViewById(R.id.repeatMonday);
        final TextView repeatTuesday = (TextView) findViewById(R.id.repeatTuesday);
        final TextView repeatWednesday = (TextView) findViewById(R.id.repeatWednesday);
        final TextView repeatThursday = (TextView) findViewById(R.id.repeatThursday);
        final TextView repeatFriday = (TextView) findViewById(R.id.repeatFriday);
        final TextView repeatSaturday = (TextView) findViewById(R.id.repeatSaturday);


        repeatSunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatSunday.getCurrentTextColor() == -1) { // current color is white
                    repeatSunday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatSunday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatMonday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatMonday.getCurrentTextColor() == -1) { // current color is white
                    repeatMonday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatMonday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatTuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatTuesday.getCurrentTextColor() == -1) { // current color is white
                    repeatTuesday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatTuesday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatWednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatWednesday.getCurrentTextColor() == -1) { // current color is white
                    repeatWednesday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatWednesday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatThursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatThursday.getCurrentTextColor() == -1) { // current color is white
                    repeatThursday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatThursday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatFriday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatFriday.getCurrentTextColor() == -1) { // current color is white
                    repeatFriday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatFriday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
        repeatSaturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatSaturday.getCurrentTextColor() == -1) { // current color is white
                    repeatSaturday.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    repeatSaturday.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });


        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Set limit button pressed",
                        Toast.LENGTH_SHORT).show();
                Switch switch_enable = (Switch) findViewById(R.id.switch_enable);
                EditText editText_numHoursPerDay = (EditText) findViewById(R.id.editText_numHoursPerDay);
                TextView repeatSunday = (TextView) findViewById(R.id.repeatSunday);
                TextView repeatMonday = (TextView) findViewById(R.id.repeatMonday);
                TextView repeatTuesday = (TextView) findViewById(R.id.repeatTuesday);
                TextView repeatWednesday = (TextView) findViewById(R.id.repeatWednesday);
                TextView repeatThursday = (TextView) findViewById(R.id.repeatThursday);
                TextView repeatFriday = (TextView) findViewById(R.id.repeatFriday);
                TextView repeatSaturday = (TextView) findViewById(R.id.repeatSaturday);
                Button btn_set = (Button) findViewById(R.id.btn_set);


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
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
