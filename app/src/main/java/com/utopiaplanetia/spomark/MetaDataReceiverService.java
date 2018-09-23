package com.utopiaplanetia.spomark;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class MetaDataReceiverService extends Service {

    public static final String CLOSE_ACTION = "close";
    private static final int NOTIFICATION = 1;
    // Binder given to clients
    private final IBinder mBinder = new ServiceBinder();
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);
    MediaBroadcastReceiver mbr = null;
    @Nullable
    private NotificationManager mNotificationManager = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MetaDataReceiverService", "onDestroy");
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        unregisterReceiver(mbr);
    }

    private void showNotification() {
        mNotificationBuilder
                .setTicker(getText(R.string.service_connected))
                .setContentText(getText(R.string.service_connected));
        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION, mNotificationBuilder.build());
        }

    }

//    private void setupNotifications() { //called in onCreate()
//        if (mNotificationManager == null) {
//            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        }
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
//                0);
//        PendingIntent pendingCloseIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                        .setAction(CLOSE_ACTION),
//                0);
//        mNotificationBuilder
//                .setSmallIcon(R.drawable.ic_stat_name)
//                .setCategory(NotificationCompat.CATEGORY_SERVICE)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setContentTitle(getText(R.string.app_name))
//                .setWhen(System.currentTimeMillis())
//                .setContentIntent(pendingIntent)
//                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
//                        getString(R.string.actionexit), pendingCloseIntent)
//                .setOngoing(true);
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        this.mbr = new MediaBroadcastReceiver(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.spotify.music.playbackstatechanged");
        filter.addAction("com.spotify.music.metadatachanged"); //further more
        filter.addAction("com.spotify.music.queuechanged"); //further more

        registerReceiver(mbr, filter);
        Log.d("MetaDataReceiverService", "onBind");
        return mBinder;
    }

    public class ServiceBinder extends Binder {

        public MetaDataReceiverService getService() {
            return MetaDataReceiverService.this;
        }
    }
}
