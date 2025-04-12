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

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TrafficDetailActivity extends AppCompatActivity {
    private LineChart chart;
    private NetworkTrafficManager trafficManager;
    private long selectedTimeRange = TimeUnit.MINUTES.toMillis(30); // Standard: 30 Minuten

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_detail);

        trafficManager = NetworkTrafficManager.getInstance(this);

        // Chart initialisieren
        chart = findViewById(R.id.traffic_chart);
        setupChart();

        // Spinner für Zeitraumauswahl einrichten
        setupTimeRangeSpinner();

        // Daten initial laden
        updateChartData();
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setExtraBottomOffset(10f);
        chart.setExtraTopOffset(10f);
        chart.setExtraLeftOffset(10f);
        chart.setExtraRightOffset(10f);
        chart.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));

        // X-Achse konfigurieren
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(ContextCompat.getColor(this, R.color.grid_color));
        xAxis.setGridLineWidth(0.5f);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(getResources().getColor(android.R.color.white, null));
        xAxis.setTextSize(10f);
        xAxis.setLabelCount(5);
        xAxis.setAvoidFirstLastClipping(true);

        // Y-Achse konfigurieren
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(android.R.color.white, null));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.grid_color));
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        // Legende konfigurieren
        chart.getLegend().setTextColor(getResources().getColor(android.R.color.white, null));
        chart.getLegend().setTextSize(12f);
        chart.getLegend().setFormSize(8f);
        chart.getLegend().setXEntrySpace(10f);

        // Zusätzliche Anpassungen
        chart.setNoDataTextColor(getResources().getColor(android.R.color.white, null));
        chart.animateX(1000);
    }

    private void setupTimeRangeSpinner() {
        Spinner timeRangeSpinner = findViewById(R.id.time_range_spinner);

        // Zeitraumoptionen
        String[] timeRanges = new String[] {
                "Letzte 5 Minuten",
                "Letzte 15 Minuten",
                "Letzte 30 Minuten",
                "Letzte Stunde",
                "Letzte 3 Stunden",
                "Letzte 6 Stunden",
                "Letzte 12 Stunden",
                "Letzte 24 Stunden"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, timeRanges);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        timeRangeSpinner.setAdapter(adapter);

        // 30 Minuten als Standard auswählen
        timeRangeSpinner.setSelection(2);

        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // 5 min
                        selectedTimeRange = TimeUnit.MINUTES.toMillis(5);
                        break;
                    case 1: // 15 min
                        selectedTimeRange = TimeUnit.MINUTES.toMillis(15);
                        break;
                    case 2: // 30 min
                        selectedTimeRange = TimeUnit.MINUTES.toMillis(30);
                        break;
                    case 3: // 1 h
                        selectedTimeRange = TimeUnit.HOURS.toMillis(1);
                        break;
                    case 4: // 3 h
                        selectedTimeRange = TimeUnit.HOURS.toMillis(3);
                        break;
                    case 5: // 6 h
                        selectedTimeRange = TimeUnit.HOURS.toMillis(6);
                        break;
                    case 6: // 12 h
                        selectedTimeRange = TimeUnit.HOURS.toMillis(12);
                        break;
                    case 7: // 24 h
                        selectedTimeRange = TimeUnit.HOURS.toMillis(24);
                        break;
                }
                updateChartData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nicht benötigt
            }
        });
    }

    private void updateChartData() {
        List<NetworkTrafficData> data = trafficManager.getTrafficData(selectedTimeRange);

        if (data.isEmpty()) {
            // Keine Daten verfügbar
            TextView noDataText = findViewById(R.id.no_data_text);
            noDataText.setVisibility(View.VISIBLE);
            chart.setVisibility(View.GONE);
            return;
        }

        TextView noDataText = findViewById(R.id.no_data_text);
        noDataText.setVisibility(View.GONE);
        chart.setVisibility(View.VISIBLE);

        List<Entry> uploadEntries = new ArrayList<>();
        List<Entry> downloadEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (int i = 0; i < data.size(); i++) {
            NetworkTrafficData point = data.get(i);
            uploadEntries.add(new Entry(i, point.getTxBytes() / 1024f)); // KB/s
            downloadEntries.add(new Entry(i, point.getRxBytes() / 1024f)); // KB/s
            labels.add(sdf.format(point.getDate()));
        }

        // Einstellung für Datensatzgröße
        int dataPoints = data.size();
        boolean showDataPoints = dataPoints <= 30; // Zeige Datenpunkte nur bei wenigen Daten

        // Upload-Linie
        LineDataSet uploadDataSet = new LineDataSet(uploadEntries, "Upload (KB/s)");
        uploadDataSet.setColor(getResources().getColor(R.color.upload_color, null));
        uploadDataSet.setCircleColor(getResources().getColor(R.color.upload_color, null));
        uploadDataSet.setCircleRadius(2f);
        uploadDataSet.setDrawCircles(showDataPoints);
        uploadDataSet.setDrawCircleHole(false);
        uploadDataSet.setLineWidth(2f);
        uploadDataSet.setValueTextSize(9f);
        uploadDataSet.setDrawValues(false);
        uploadDataSet.setDrawFilled(true);
        uploadDataSet.setFillColor(getResources().getColor(R.color.upload_color_transparent, null));
        uploadDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Download-Linie
        LineDataSet downloadDataSet = new LineDataSet(downloadEntries, "Download (KB/s)");
        downloadDataSet.setColor(getResources().getColor(R.color.download_color, null));
        downloadDataSet.setCircleColor(getResources().getColor(R.color.download_color, null));
        downloadDataSet.setCircleRadius(2f);
        downloadDataSet.setDrawCircles(showDataPoints);
        downloadDataSet.setDrawCircleHole(false);
        downloadDataSet.setLineWidth(2f);
        downloadDataSet.setValueTextSize(9f);
        downloadDataSet.setDrawValues(false);
        downloadDataSet.setDrawFilled(true);
        downloadDataSet.setFillColor(getResources().getColor(R.color.download_color_transparent, null));
        downloadDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // X-Achsenbeschriftung
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Anpassen der angezeigten Labels abhängig von der Datenmenge
        int labelCount = Math.min(5, labels.size());
        if (labelCount == 0) labelCount = 1;
        chart.getXAxis().setLabelCount(labelCount, true);

        // Daten zum Chart hinzufügen
        LineData lineData = new LineData(uploadDataSet, downloadDataSet);
        chart.setData(lineData);

        // Chart aktualisieren
        chart.invalidate();
    }
}