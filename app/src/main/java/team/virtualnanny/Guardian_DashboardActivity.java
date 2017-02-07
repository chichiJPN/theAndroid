package team.virtualnanny;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class Guardian_DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_dashboard);
        setTitle("Performance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black
        ImageView profpic = (ImageView) findViewById(R.id.profpic);
        TextView profpic_description = (TextView) findViewById(R.id.profpic_description);
        ProgressBar progressbar_consequence = (ProgressBar) findViewById(R.id.progressbar_consequence);
        ProgressBar progressbar_reward = (ProgressBar) findViewById(R.id.progressbar_reward);
        LinearLayout remind_container = (LinearLayout) findViewById(R.id.remind_container);
        LinearLayout tasks_container = (LinearLayout) findViewById(R.id.tasks_container);
        LinearLayout steps_taken_container = (LinearLayout) findViewById(R.id.steps_taken_container);
		
		
        ImageView btn_add_assignment = (ImageView) findViewById(R.id.btn_add_assignment);

        btn_add_assignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Guardian_DashboardActivity.this, Guardian_AddAssignmentActivity.class);
                startActivity(i);
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
