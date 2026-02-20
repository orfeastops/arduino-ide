package com.greenhouse.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.greenhouse.app.models.Settings;

public class ControlsFragment extends Fragment {

    private SwitchCompat switchLight, switchFan, switchPump1, switchPump2, switchPump3;
    private TextView tvLightStatus, tvFanStatus, tvPump1Status, tvPump2Status, tvPump3Status;
    private boolean isUpdatingFromState = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find all views
        switchLight  = view.findViewById(R.id.switch_light);
        switchFan    = view.findViewById(R.id.switch_fan);
        switchPump1  = view.findViewById(R.id.switch_pump1);
        switchPump2  = view.findViewById(R.id.switch_pump2);
        switchPump3  = view.findViewById(R.id.switch_pump3);
        tvLightStatus = view.findViewById(R.id.tv_light_status);
        tvFanStatus   = view.findViewById(R.id.tv_fan_status);
        tvPump1Status = view.findViewById(R.id.tv_pump1_status);
        tvPump2Status = view.findViewById(R.id.tv_pump2_status);
        tvPump3Status = view.findViewById(R.id.tv_pump3_status);

        // Set tags to link switches and status views for error handling
        switchLight.setTag("light_enabled");
        switchFan.setTag("fan_enabled");
        switchPump1.setTag("pump1_enabled");
        switchPump2.setTag("pump2_enabled");
        switchPump3.setTag("pump3_enabled");

        // Fetch the initial state from the server before enabling user interaction
        fetchCurrentState();
    }

    private void fetchCurrentState() {
        ApiClient.get("/api/settings", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                Settings s = new Gson().fromJson(response, Settings.class);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (getContext() == null) return;
                    isUpdatingFromState = true; // Prevent listeners from firing during initial setup

                    updateSwitchAndStatus(switchLight, tvLightStatus, s.light_enabled);
                    updateSwitchAndStatus(switchFan, tvFanStatus, s.fan_enabled);
                    updateSwitchAndStatus(switchPump1, tvPump1Status, s.pump1_enabled);
                    updateSwitchAndStatus(switchPump2, tvPump2Status, s.pump2_enabled);
                    updateSwitchAndStatus(switchPump3, tvPump3Status, s.pump3_enabled);

                    isUpdatingFromState = false; // Re-enable listeners
                    attachListeners();
                });
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), getString(R.string.could_not_load_state, error), Toast.LENGTH_LONG).show();
                    attachListeners(); // Attach listeners even on error to allow manual control
                });
            }
        });
    }

    // Helper to set the initial state of a switch and its corresponding label
    private void updateSwitchAndStatus(SwitchCompat sw, TextView tv, String enabled) {
        boolean isEnabled = "1".equals(enabled);
        sw.setChecked(isEnabled);
        updateStatusLabel(tv, isEnabled);
    }

    // Attach listeners only after the initial state is loaded
    private void attachListeners() {
        switchLight.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!isUpdatingFromState) sendSetting("light_enabled", isChecked, tvLightStatus);
        });
        switchFan.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!isUpdatingFromState) sendSetting("fan_enabled", isChecked, tvFanStatus);
        });
        switchPump1.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!isUpdatingFromState) sendSetting("pump1_enabled", isChecked, tvPump1Status);
        });
        switchPump2.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!isUpdatingFromState) sendSetting("pump2_enabled", isChecked, tvPump2Status);
        });
        switchPump3.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!isUpdatingFromState) sendSetting("pump3_enabled", isChecked, tvPump3Status);
        });
    }

    private void sendSetting(String key, boolean value, TextView statusView) {
        // *** THE CRITICAL FIX ***
        // The server expects a number (1 or 0), not a string in quotes (e.g., "1").
        String json = "{\"key\":\"" + key + "\",\"value\":" + (value ? 1 : 0) + "}";
        ApiClient.post("/api/settings", json, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (getContext() == null) return;
                    updateStatusLabel(statusView, value);
                    Toast.makeText(getContext(), R.string.updated_message, Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (getContext() == null) return;
                    // Show a detailed error message from the server
                    Toast.makeText(getContext(), getString(R.string.error_updating_control_details, key, error), Toast.LENGTH_LONG).show();

                    // IMPORTANT: Revert the switch to its actual state since the API call failed
                    isUpdatingFromState = true;
                    SwitchCompat aSwitch = statusView.getRootView().findViewWithTag(key);
                    if (aSwitch != null) {
                        aSwitch.setChecked(!value);
                    }
                    isUpdatingFromState = false;
                });
            }
        });
    }

    // Single, reliable function to update the status label's text and color
    private void updateStatusLabel(TextView statusView, boolean isEnabled) {
        if (getContext() == null) return;
        statusView.setText(isEnabled ? R.string.status_operational : R.string.status_disabled);
        statusView.setTextColor(ContextCompat.getColor(getContext(), isEnabled ? R.color.green : R.color.red));
    }
}
