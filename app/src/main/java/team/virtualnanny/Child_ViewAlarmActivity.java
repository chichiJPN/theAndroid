package team.virtualnanny;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class Child_ViewAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_view_alarm);
        setTitle("Alarm Schedules");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black
		
        ImageView prof_pic = (ImageView) findViewById(R.id.prof_pic);
        TextView prof_description = (TextView) findViewById(R.id.prof_description);
        ProgressBar reward_progressbar = (ProgressBar) findViewById(R.id.reward_progressbar);
        ProgressBar consequence_progressbar = (ProgressBar) findViewById(R.id.consequence_progressbar);
        LinearLayout reminder_container = (LinearLayout) findViewById(R.id.reminder_container);
        LinearLayout tasks_container = (LinearLayout) findViewById(R.id.tasks_container);
        LinearLayout steps_taken_container = (LinearLayout) findViewById(R.id.steps_taken_container);

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
