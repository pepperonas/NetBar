package io.celox.netbar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NetworkTrafficService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "network_monitor_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int UPDATE_INTERVAL = 1000; // 1 second

    private Handler handler;
    private Runnable updateRunnable;
    private long lastTxBytes = 0;
    private long lastRxBytes = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateTrafficInfo();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification("Starting..."));
        handler.post(updateRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Network Traffic Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows network traffic information");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_network)
                .setContentTitle("Network Traffic")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        return builder.build();
    }

    private void updateTrafficInfo() {
        long currentTxBytes = TrafficStats.getTotalTxBytes();
        long currentRxBytes = TrafficStats.getTotalRxBytes();

        long txDiff = currentTxBytes - lastTxBytes;
        long rxDiff = currentRxBytes - lastRxBytes;

        lastTxBytes = currentTxBytes;
        lastRxBytes = currentRxBytes;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showUp = prefs.getBoolean("show_up", true);
        boolean showDown = prefs.getBoolean("show_down", true);

        String notification = formatTrafficInfo(txDiff, rxDiff, showUp, showDown);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification(notification));
    }

    private String formatTrafficInfo(long txBytes, long rxBytes, boolean showUp, boolean showDown) {
        StringBuilder sb = new StringBuilder();

        if (showUp) {
            sb.append("↑ ").append(formatBytes(txBytes)).append("/s");
        }

        if (showUp && showDown) {
            sb.append(" | ");
        }

        if (showDown) {
            sb.append("↓ ").append(formatBytes(rxBytes)).append("/s");
        }

        return sb.toString();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}