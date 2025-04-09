/*
 * Copyright (C) 2025 Martin Pfeffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.celox.netbar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

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
        // Erstelle Intent zum Öffnen der MainActivity beim Tippen auf die Benachrichtigung
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Startmeldung in der Statusleiste
        Notification notification = createNotification("↑ 0 B/s | ↓ 0 B/s");
        startForeground(NOTIFICATION_ID, notification);

        // Starte das regelmäßige Update
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
            channel.setShowBadge(false); // Keine Badge-Anzeige auf dem App-Icon
            channel.enableLights(false); // Keine LED-Benachrichtigung
            channel.enableVibration(false); // Keine Vibration
            channel.setSound(null, null); // Kein Sound

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String content) {
        // Erstelle eine Benachrichtigung, die als Statusleisten-Indikator dient
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_network)
                .setContentTitle("Network Traffic")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(
                        createCustomNotificationView(content)
                )
                // Entfernt den Zeitstempel aus der Benachrichtigung
                .setShowWhen(false)
                // Verhindert Abwischen der Benachrichtigung
                .setOngoing(true);

        return builder.build();
    }

    private RemoteViews createCustomNotificationView(String content) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.status_bar_view);
        remoteViews.setTextViewText(R.id.status_text, content);
        return remoteViews;
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

        // Formatiere die Netzwerkverkehrsinformationen
        String statusText = formatTrafficInfo(txDiff, rxDiff, showUp, showDown);

        // Aktualisiere die Benachrichtigung
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification(statusText));
    }

    private String formatTrafficInfo(long txBytes, long rxBytes, boolean showUp, boolean showDown) {
        StringBuilder sb = new StringBuilder();

        if (showUp) {
            sb.append("↑ ").append(formatBytesCompact(txBytes)).append("/s");
        }

        if (showUp && showDown) {
            sb.append(" | ");
        }

        if (showDown) {
            sb.append("↓ ").append(formatBytesCompact(rxBytes)).append("/s");
        }

        return sb.toString();
    }

    // Kompaktere Formatierung für die Statusleiste
    private String formatBytesCompact(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.0fK", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1fM", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1fG", bytes / (1024.0 * 1024 * 1024));
        }
    }

    // Original-Formatierungsmethode für vollständige Darstellung
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