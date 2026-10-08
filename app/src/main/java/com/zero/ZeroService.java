package com.zero;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ZeroService extends Service {

    private static final String CHANNEL_ID = "zero_service";

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel canal =
                new NotificationChannel(
                        CHANNEL_ID,
                        "Zero",
                        NotificationManager.IMPORTANCE_LOW
                );

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        manager.createNotificationChannel(canal);

        Notification notificacion =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("Zero está activo")
                        .setContentText("Di \"Zero\" para activarlo")
                        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                        .build();

        startForeground(1001, notificacion);
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
