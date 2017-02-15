package team.virtualnanny;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class Child_MenuDrawerActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_menu_drawer);
        setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enables back button on the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000)); // sets the actions bar as black

		
		
		ImageView profile_child = (ImageView) findViewById(R.id.profile_child);

		
        LinearLayout profileBar = (LinearLayout) findViewById(R.id.profileBar) ;

        profileBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Child_MenuDrawerActivity.this, Child_ProfileActivity.class);
                startActivity(i);
            }
        });

        ListView list;
        final String[] itemname ={
                "Notifications",
                "Messages",
                "Add Parent Account",
                "Logout",
                "About Us"
        };

        Integer[] imgid={
                R.drawable.notifications,
                R.drawable.message,
                R.drawable.add_user,
                R.drawable.logout,
                R.drawable.about_us,
        };

        CustomListAdapter adapter=new CustomListAdapter(this, itemname, imgid);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                String Slecteditem = itemname[+position];
                /*
                *                 "Notifications",
                "Messages",
                "Add Child Account",
                "Logout",
                "About Us"
                * */
                switch(Slecteditem) {
                    case "Notifications":
                        break;
                    case "Messages":
                        break;
                    case "Add Child Account":
                        break;
                    case "Logout":
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                        startActivity(intent);
                        break;
                    case "About Us":
                        final Intent i = new Intent(Child_MenuDrawerActivity.this, AboutUsActivity.class);
                        startActivity(i);
                        break;
                }
                Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();
            }
        });
        
		findViewById(R.id.android_layout).setOnTouchListener(new OnSwipeTouchListener(Child_MenuDrawerActivity.this) {
            public void onSwipeTop() {
            }

            public void onSwipeRight() {}
			
            public void onSwipeLeft() {
                onBackPressed();
                overridePendingTransition(R.anim.right2left_enter, R.anim.right2left_exit);
            }
			
            public void onSwipeBottom() {
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
