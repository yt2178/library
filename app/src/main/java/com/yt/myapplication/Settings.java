package com.yt.myapplication;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Settings extends AppCompatActivity {
    private static final String TAG = "Settings";
    private static final String BACKUP_FOLDER = "TalmudBackup";
    private static final String TOTAL_USER_DATA_NAME = "user_data.shinantam";
    private FileManager m_fileManager;

    private boolean isFileExist(String fileName) {
        String[] files = m_fileManager.getInternalFileList();
        return Arrays.asList(files).contains(fileName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        m_fileManager = new FileManager(this);

        // מציב את ה-Toolbar כ-ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//כפתור חזור
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // חזרה לאחור - סגירת האקטיביטי הנוכחי
                onBackPressed();
            }
        });

        // הגדרת פעולות הכפתורים
        findViewById(R.id.backupButton).setOnClickListener(v -> backupData());
        findViewById(R.id.restoreButton).setOnClickListener(v -> showRestoreDialog());

        // בדיקה אם האפליקציה נפתחה מקובץ .shinantam
        Uri data = getIntent().getData();
        if (data != null && data.getPath() != null && data.getPath().endsWith(".shinantam")) {
            Log.d(TAG, "Opening file: " + data.getPath());
            // הצגת דיאלוג האם לשחזר את הנתונים
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("שחזור נתונים");
            builder.setMessage("האם ברצונך לשחזר את הנתונים מקובץ הגיבוי?");
            builder.setPositiveButton("כן", (dialog, which) -> {
                File backupFile = new File(data.getPath());
                restoreData(backupFile);
            });
            builder.setNegativeButton("לא", null);
            builder.show();
        }
    }

    private void backupData() {
        try {
            // יצירת תיקיית הגיבוי
            File backupDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), BACKUP_FOLDER);
            if (!backupDir.exists()) {
                boolean created = backupDir.mkdirs();
                Log.d(TAG, "Backup directory created: " + created);
            }

            // יצירת שם קובץ עם תאריך ושעה
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File backupFile = new File(backupDir, "talmud_backup_" + timeStamp + ".shinantam");
            Log.d(TAG, "Backup file path: " + backupFile.getAbsolutePath());

            // העתקת הקובץ המקורי לקובץ הגיבוי
            String content = m_fileManager.readInternalFile(TOTAL_USER_DATA_NAME);
            FileOutputStream outputStream = new FileOutputStream(backupFile);
            outputStream.write(content.getBytes());
            outputStream.close();

            Toast.makeText(this, "הגיבוי הושלם בהצלחה!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Backup error: " + e.getMessage(), e);
            Toast.makeText(this, "שגיאה בביצוע הגיבוי: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showRestoreDialog() {
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), BACKUP_FOLDER);
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".shinantam"));

        if (backupFiles == null || backupFiles.length == 0) {
            Toast.makeText(this, "לא נמצאו קבצי גיבוי", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] backupNames = new String[backupFiles.length];
        for (int i = 0; i < backupFiles.length; i++) {
            backupNames[i] = backupFiles[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר קובץ גיבוי לשחזור");
        builder.setItems(backupNames, (dialog, which) -> {
            File selectedBackup = backupFiles[which];
            restoreData(selectedBackup);
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void restoreData(File backupFile) {
        try {
            Log.d(TAG, "Starting restore from: " + backupFile.getAbsolutePath());

            // קריאת הקובץ והעתקתו לקובץ הפנימי
            FileInputStream inputStream = new FileInputStream(backupFile);
            byte[] data = new byte[(int) backupFile.length()];
            inputStream.read(data);
            inputStream.close();

            String content = new String(data);
            m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, content, false);

            Toast.makeText(this, "השחזור הושלם בהצלחה!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Restore error: " + e.getMessage(), e);
            Toast.makeText(this, "שגיאה בשחזור: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}