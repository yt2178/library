package com.yt.myapplication;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WidgetCompactConfigActivity extends AppCompatActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_compact_widget_config);

        Intent intent = getIntent();
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        RadioGroup radioGroup = findViewById(R.id.radio_group_compact_content);
        SeekBar textSizeSeekBar = findViewById(R.id.seekbar_compact_text_size);
        SeekBar transparencySeekBar = findViewById(R.id.seekbar_compact_transparency);
        Button saveButton = findViewById(R.id.btn_save);

        SharedPreferences prefs = getSharedPreferences("compact_widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
        radioGroup.check(prefs.getInt("content_id", R.id.radio_hebrew_date));
        textSizeSeekBar.setProgress(prefs.getInt("text_size", 14));
        transparencySeekBar.setProgress(prefs.getInt("transparency", 128));

        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("compact_widget_prefs_" + appWidgetId, Context.MODE_PRIVATE).edit();
            editor.putInt("content_id", radioGroup.getCheckedRadioButtonId());
            editor.putInt("text_size", textSizeSeekBar.getProgress());
            editor.putInt("transparency", transparencySeekBar.getProgress());
            editor.apply();

            WidgetCompactProvider.updateAppWidget(this, AppWidgetManager.getInstance(this), appWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        });
    }
}