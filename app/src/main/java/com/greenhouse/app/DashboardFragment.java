package com.greenhouse.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.greenhouse.app.models.SensorData;

public class DashboardFragment extends Fragment {

    private TextView tvTemperature, tvHumidity, tvLight, tvFan;
    private TextView tvMoisture1, tvMoisture2, tvMoisture3;
    private TextView tvPump1, tvPump2, tvPump3;
    private TextView tvLastUpdated, tvStatus;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 30000; // 30 seconds

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvTemperature  = view.findViewById(R.id.tv_temperature);
        tvHumidity     = view.findViewById(R.id.tv_humidity);
        tvLight        = view.findViewById(R.id.tv_light);
        tvFan          = view.findViewById(R.id.tv_fan);
        tvMoisture1    = view.findViewById(R.id.tv_moisture1);
        tvMoisture2    = view.findViewById(R.id.tv_moisture2);
        tvMoisture3    = view.findViewById(R.id.tv_moisture3);
        tvPump1        = view.findViewById(R.id.tv_pump1);
        tvPump2        = view.findViewById(R.id.tv_pump2);
        tvPump3        = view.findViewById(R.id.tv_pump3);
        tvLastUpdated  = view.findViewById(R.id.tv_last_updated);
        tvStatus       = view.findViewById(R.id.tv_status);

        // Fetch immediately then every 30 seconds
        fetchData();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchData();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        handler.postDelayed(refreshRunnable, REFRESH_INTERVAL);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);
    }

    private void fetchData() {
        ApiClient.get("/api/data/latest", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                SensorData data = new Gson().fromJson(response, SensorData.class);
                requireActivity().runOnUiThread(() -> updateUI(data));
            }
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("● OFFLINE");
                    tvStatus.setTextColor(0xFFFF5722);
                });
            }
        });
    }

    private void updateUI(SensorData data) {
        tvStatus.setText("● LIVE");
        tvStatus.setTextColor(0xFF4CAF50);

        tvTemperature.setText(String.format("%.1f°C", data.temperature));
        tvHumidity.setText(String.format("%.0f%%", data.humidity));

        // Light
        tvLight.setText(data.light == 1 ? "ON" : "OFF");
        tvLight.setTextColor(data.light == 1 ? 0xFF4CAF50 : 0xFFFF5722);

        // Fan
        tvFan.setText(data.fan == 1 ? "ON" : "OFF");
        tvFan.setTextColor(data.fan == 1 ? 0xFF4CAF50 : 0xFFFF5722);

        // Moisture colors — red if dry, green if ok
        updateMoisture(tvMoisture1, data.moisture1);
        updateMoisture(tvMoisture2, data.moisture2);
        updateMoisture(tvMoisture3, data.moisture3);

        // Pump status
        tvPump1.setText("Pump: " + (data.pump1 == 1 ? "ON 💧" : "OFF"));
        tvPump2.setText("Pump: " + (data.pump2 == 1 ? "ON 💧" : "OFF"));
        tvPump3.setText("Pump: " + (data.pump3 == 1 ? "ON 💧" : "OFF"));

        // Last updated
        tvLastUpdated.setText("Last updated: " + data.timestamp);
    }

    private void updateMoisture(TextView tv, float moisture) {
        tv.setText(String.format("%.0f%%", moisture));
        if (moisture < 35) {
            tv.setTextColor(0xFFFF5722); // red - dry
        } else if (moisture < 60) {
            tv.setTextColor(0xFFFFAB00); // orange - ok
        } else {
            tv.setTextColor(0xFF4CAF50); // green - good
        }
    }
}