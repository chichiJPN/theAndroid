package team.virtualnanny;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // For our recurring task, we'll just display a message

        Log.d("Alarm receiver", "I was run");
        String alarmMessage = "";
        String alarmName = "";
        try {
            alarmMessage = intent.getStringExtra("message");
            alarmName = intent.getStringExtra("alarmName");
        }catch(Exception e) {
            Log.d("Receiver",e.getMessage());
            return;
        }
        Notification notification;
        notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.fence)
                .setContentTitle("Virtual Nanny")
                .setContentText(alarmMessage)
                .setOngoing(false)
                .build();
        Vibrator v;

        v=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        v.vibrate(1000);

        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notifyMgr.notify(1338,notification);
        Db_notification dbNotification = new Db_notification();
        dbNotification.setStatus("Not read");
        dbNotification.setTitle(alarmName + " Alarm notification");

        // update database
        dbNotification.setContent(alarmMessage);
        String timestamp = String.valueOf(System.currentTimeMillis());
        // add notification item to guardian's database notifications
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID).child("notifications").child(timestamp).setValue(dbNotification);

    }
}