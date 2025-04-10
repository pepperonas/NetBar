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

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Verwaltet die Erfassung und Speicherung von Netzwerkverkehrsdaten.
 */
public class NetworkTrafficManager {
    private static final String FILE_NAME = "network_traffic_data.dat";
    private static final long MAX_DATA_AGE_MS = TimeUnit.HOURS.toMillis(24); // 24 Stunden speichern
    private static final int MAX_DATA_POINTS = 1440; // Maximal 1440 Datenpunkte (1 pro Minute für 24h)
    
    private static NetworkTrafficManager instance;
    private final Context context;
    private final List<NetworkTrafficData> trafficDataList;
    
    private NetworkTrafficManager(Context context) {
        this.context = context.getApplicationContext();
        this.trafficDataList = loadTrafficData();
    }
    
    public static synchronized NetworkTrafficManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkTrafficManager(context);
        }
        return instance;
    }
    
    public void addTrafficData(long txBytes, long rxBytes) {
        long currentTime = System.currentTimeMillis();
        
        // Alte Daten entfernen
        long expiryTime = currentTime - MAX_DATA_AGE_MS;
        trafficDataList.removeIf(data -> data.getTimestamp() < expiryTime);
        
        // Neuen Datenpunkt hinzufügen
        trafficDataList.add(new NetworkTrafficData(currentTime, txBytes, rxBytes));
        
        // Begrenze die Anzahl der Datenpunkte
        while (trafficDataList.size() > MAX_DATA_POINTS) {
            trafficDataList.remove(0);
        }
        
        // Speichere die Daten
        saveTrafficData();
    }
    
    @NonNull
    public List<NetworkTrafficData> getTrafficData(long timeRangeMs) {
        long cutoffTime = System.currentTimeMillis() - timeRangeMs;
        List<NetworkTrafficData> filteredData = new ArrayList<>();
        
        for (NetworkTrafficData data : trafficDataList) {
            if (data.getTimestamp() >= cutoffTime) {
                filteredData.add(data);
            }
        }
        
        return filteredData;
    }
    
    public List<NetworkTrafficData> getAllTrafficData() {
        return new ArrayList<>(trafficDataList);
    }
    
    private void saveTrafficData() {
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(trafficDataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<NetworkTrafficData> loadTrafficData() {
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<NetworkTrafficData>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
    
    public void clearData() {
        trafficDataList.clear();
        saveTrafficData();
    }
}
