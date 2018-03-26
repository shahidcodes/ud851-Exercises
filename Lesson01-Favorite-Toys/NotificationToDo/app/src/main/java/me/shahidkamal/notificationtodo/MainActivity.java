package me.shahidkamal.notificationtodo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int NOTIFICATION_ID = 0;
    private static final String PREF_NAME = "PREF_NOTIF";
    String channelId = "notificationTodo";
    String channelName = "notificationTodoChannel";
    String notification;
    NotificationManager notificationManager;
    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if ("clearNotification".equals(getIntent().getAction())){
            clearNotification();
            Log.d(TAG, "onCreate: cancel notification");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearNotification();
                EditText editText = findViewById(R.id.editText);
                String notif = editText.getText().toString();
                Log.d(TAG, "onClick: " + notif);
                saveNotification(notif);
                createNotification(getApplicationContext(), notif);
            }
        });

        tv = findViewById(R.id.notificationTextView);
        if (notification ==null){
            notification = getNotificationText();
        }
        tv.setText(notification);
    }

    private void clearNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void saveNotification(String notif) {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("notification", notif);
        editor.commit();
        tv.setText(pref.getString("notification", null));

    }


    private void createNotification(Context context, String text) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setContentTitle("Task Reminder")
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                        .addAction(R.drawable.ic_clear, "Clear", getClearPendingIntent())
                        .setContentText("Pending Tasks");
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(0, mBuilder.build());


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(){
        NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(mChannel);
    }



    public String getNotificationText() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return pref.getString("notification", null);
    }

    public PendingIntent getClearPendingIntent() {
        Intent intentAction = new Intent(this,ActionReceiver.class);

        //This is optional if you have more than one buttons and want to differentiate between two
        intentAction.putExtra("action","clear");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }
}
