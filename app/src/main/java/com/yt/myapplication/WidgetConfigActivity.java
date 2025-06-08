package com.yt.myapplication;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;

public class WidgetConfigActivity extends AppCompatActivity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private boolean isUpdate = false;
    private Spinner spinnerCities;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_widget_config);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        if (!AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())) {
            isUpdate = true;
        }

        SwitchMaterial switchGregorian = findViewById(R.id.switch_gregorian);
        SwitchMaterial switchParasha = findViewById(R.id.switch_parasha);
         spinnerCities = findViewById(R.id.spinner_cities);
        SeekBar seekbarTransparency = findViewById(R.id.seekbar_transparency);
        Button saveButton = findViewById(R.id.btn_save);

        List<String> cityNames = CityData.getCityNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCities.setAdapter(adapter);

        if (isUpdate) {
            saveButton.setText("שמור שינויים");
            loadPreferences(switchGregorian, switchParasha, spinnerCities, seekbarTransparency);
        }

        saveButton.setOnClickListener(v -> {
            saveAndFinish(switchGregorian, switchParasha, spinnerCities, seekbarTransparency);
        });

        // ==========================================================
        // ==== קוד חדש לקביעת גודל החלון באופן ידני ====
        // ==========================================================
        Window window = getWindow();
        if (window != null) {
            // קבלת מידות המסך
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            // הגדרת רוחב החלון ל-90% מרוחב המסך
            int width = (int) (displayMetrics.widthPixels * 0.90);

            // הגדרת גובה החלון לפי התוכן (אבל לא יותר מ-80% מגובה המסך)
            int height = WindowManager.LayoutParams.WRAP_CONTENT;

            // החלת המידות החדשות על החלון
            window.setLayout(width, height);
            window.setGravity(Gravity.CENTER); // ממקם את הדיאלוג במרכז
        }
        // ==========================================================
    }

    private void loadPreferences(SwitchMaterial sGregorian, SwitchMaterial sParasha, Spinner spinner, SeekBar seekbar) {
        SharedPreferences prefs = getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
        sGregorian.setChecked(prefs.getBoolean("show_gregorian", true));
        sParasha.setChecked(prefs.getBoolean("show_parasha", true));
        seekbar.setProgress(prefs.getInt("transparency", 128));
        String savedCity = prefs.getString("city_name", "ירושלים");
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(savedCity);
        spinner.setSelection(position);
    }

    private void saveAndFinish(SwitchMaterial sGregorian, SwitchMaterial sParasha, Spinner spinner, SeekBar seekbar) {
        String selectedCity = spinner.getSelectedItem().toString();
        SharedPreferences.Editor prefs = getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE).edit();
        prefs.putBoolean("show_gregorian", sGregorian.isChecked());
        prefs.putBoolean("show_parasha", sParasha.isChecked());
        prefs.putInt("transparency", seekbar.getProgress());
        prefs.putString("city_name", selectedCity);
        prefs.apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        HebrewDateWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);

        if (!isUpdate) {
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
        }
        finish();
    }
}