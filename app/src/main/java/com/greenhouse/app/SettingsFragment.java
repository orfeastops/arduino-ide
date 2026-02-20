package com.greenhouse.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.greenhouse.app.models.Settings;

public class SettingsFragment extends Fragment {

    private EditText etWaterOn, etWaterOff, etWaterDuration;
    private EditText etTempOn, etTempOff, etHumOn, etHumOff;
    private EditText etLightOffStart, etLightOffEnd;
    private Button btnSaveSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        etWaterOn       = view.findViewById(R.id.et_water_on);
        etWaterOff      = view.findViewById(R.id.et_water_off);
        etWaterDuration = view.findViewById(R.id.et_water_duration);
        etTempOn        = view.findViewById(R.id.et_temp_on);
        etTempOff       = view.findViewById(R.id.et_temp_off);
        etHumOn         = view.findViewById(R.id.et_hum_on);
        etHumOff        = view.findViewById(R.id.et_hum_off);
        etLightOffStart = view.findViewById(R.id.et_light_off_start);
        etLightOffEnd   = view.findViewById(R.id.et_light_off_end);
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);

        // Load current settings from server
        fetchSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());

        return view;
    }

    private void fetchSettings() {
        ApiClient.get("/api/settings", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                Settings s = new Gson().fromJson(response, Settings.class);
                requireActivity().runOnUiThread(() -> {
                    etWaterOn.setText(s.water_on);
                    etWaterOff.setText(s.water_off);
                    etWaterDuration.setText(
                            String.valueOf(Integer.parseInt(s.water_duration) / 1000)
                    );
                    etTempOn.setText(s.temp_on);
                    etTempOff.setText(s.temp_off);
                    etHumOn.setText(s.hum_on);
                    etHumOff.setText(s.hum_off);
                    etLightOffStart.setText(s.light_off_start);
                    etLightOffEnd.setText(s.light_off_end);
                });
            }
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Could not load settings", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void saveSettings() {
        // Convert duration seconds to ms for server
        int durationMs = Integer.parseInt(etWaterDuration.getText().toString()) * 1000;

        sendSetting("water_on",        etWaterOn.getText().toString());
        sendSetting("water_off",       etWaterOff.getText().toString());
        sendSetting("water_duration",  String.valueOf(durationMs));
        sendSetting("temp_on",         etTempOn.getText().toString());
        sendSetting("temp_off",        etTempOff.getText().toString());
        sendSetting("hum_on",          etHumOn.getText().toString());
        sendSetting("hum_off",         etHumOff.getText().toString());
        sendSetting("light_off_start", etLightOffStart.getText().toString());
        sendSetting("light_off_end",   etLightOffEnd.getText().toString());

        Toast.makeText(getContext(), "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void sendSetting(String key, String value) {
        String json = "{\"key\":\"" + key + "\",\"value\":\"" + value + "\"}";
        ApiClient.post("/api/settings", json, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                // Silent success - toast shown once in saveSettings()
            }
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error saving " + key, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}