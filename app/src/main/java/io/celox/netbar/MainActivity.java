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

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private CheckBox showUpCheckBox;
    private CheckBox showDownCheckBox;
    private Button startButton;
    private Button stopButton;
    private SharedPreferences prefs;

    // ActivityResultLauncher for notification permission request
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startService();
                } else {
                    Toast.makeText(this, "Notification permission is required to display network stats", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        showUpCheckBox = findViewById(R.id.show_up_checkbox);
        showDownCheckBox = findViewById(R.id.show_down_checkbox);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);

        // Initialize checkboxes from saved preferences
        showUpCheckBox.setChecked(prefs.getBoolean("show_up", true));
        showDownCheckBox.setChecked(prefs.getBoolean("show_down", true));

        showUpCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreferences();
            updateService();
        });

        showDownCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreferences();
            updateService();
        });

        startButton.setOnClickListener(v -> checkAndRequestPermissions());

        stopButton.setOnClickListener(v -> stopService());
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            startService();
        }
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("show_up", showUpCheckBox.isChecked());
        editor.putBoolean("show_down", showDownCheckBox.isChecked());
        editor.apply();
    }

    private void updateService() {
        // Restart service if it's running
        if (isServiceRunning()) {
            stopService();
            checkAndRequestPermissions();
        }
    }

    private boolean isServiceRunning() {
        // Simple check - in a real app you might want to use ActivityManager
        return prefs.getBoolean("service_running", false);
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, NetworkTrafficService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("service_running", true);
        editor.apply();
    }

    private void stopService() {
        Intent serviceIntent = new Intent(this, NetworkTrafficService.class);
        stopService(serviceIntent);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("service_running", false);
        editor.apply();
    }
}