package com.zebra.datawedge_background_scanning;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class ListeningService extends IntentService {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public ListeningService() {
        super("ListeningService");
    }

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getAction().equalsIgnoreCase(getResources().getString(R.string.dw_scan_action))) {
            String scanData = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));

            //  Note that the notification is unlikely to be seen since the scan is processed so quickly but this step is necessary on O+
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("DataWedge Background Scanning")
                    .setContentText("barcode processing")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);

            showToast("Scanned: " + scanData);
            TTS.initAndSpeak(getApplicationContext(), scanData);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "DataWedge Background Scanning",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}
