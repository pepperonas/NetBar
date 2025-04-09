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

/**
 * @author Martin Pfeffer
 * <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 * @see <a href="https://celox.io">https://celox.io</a>
 */

import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.net.TrafficStats;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class NetworkTileService extends TileService {
    private static final int UPDATE_INTERVAL = 1000; // 1 second

    private Handler handler;
    private Runnable updateRunnable;
    private long lastTxBytes = 0;
    private long lastRxBytes = 0;
    private boolean isMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateTrafficInfo();
                if (isMonitoring) {
                    handler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        };
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        // Initialize the tile with current state
        Tile tile = getQsTile();
        if (tile != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            isMonitoring = prefs.getBoolean("service_running", false);

            if (isMonitoring) {
                tile.setState(Tile.STATE_ACTIVE);
                startMonitoring();
            } else {
                tile.setState(Tile.STATE_INACTIVE);
            }

            tile.updateTile();
        }
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        stopMonitoring();
    }

    @Override
    public void onClick() {
        super.onClick();

        Tile tile = getQsTile();
        if (tile != null) {
            isMonitoring = !isMonitoring;

            if (isMonitoring) {
                tile.setState(Tile.STATE_ACTIVE);
                startMonitoring();
            } else {
                tile.setState(Tile.STATE_INACTIVE);
                stopMonitoring();
            }

            // Save the state
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean("service_running", isMonitoring);
            editor.apply();

            tile.updateTile();
        }
    }

    private void startMonitoring() {
        if (!isMonitoring) {
            isMonitoring = true;
            lastTxBytes = TrafficStats.getTotalTxBytes();
            lastRxBytes = TrafficStats.getTotalRxBytes();
            handler.post(updateRunnable);
        }
    }

    private void stopMonitoring() {
        isMonitoring = false;
        handler.removeCallbacks(updateRunnable);

        // Reset the tile's label
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setLabel(getString(R.string.app_name));
            tile.updateTile();
        }
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

        String tileLabel = formatTrafficInfo(txDiff, rxDiff, showUp, showDown);

        Tile tile = getQsTile();
        if (tile != null) {
            tile.setLabel(tileLabel);
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_network));
            tile.updateTile();
        }
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