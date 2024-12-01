package com.yt.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Welcome extends AppCompatActivity {
    private static final String TOTAL_USER_DATA_NAME = "user_data.shinantam";
    private static final String USERNAME_PREFIX = "שם משתמש: ";
    // הצהרת משתנה textViewUserName
    private TextView textViewUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        textViewUserName = findViewById(R.id.textViewUserName); // קבלת גישה לתיבת הטקסט

        checkUserNameAndCreateFileIfNeeded();
        
        // דחיית המעבר לאקטיביטי הבא ב-3 שניות
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // יצירת Intent לעבור לאקטיביטי הבא
                Intent intent = new Intent(Welcome.this, MainActivity.class); // שים את שם הקטיביטי הבא במקום NextActivity
                startActivity(intent);
                finish(); // לסיים את הקטיביטי הנוכחי כדי שלא יחזור אליו
            }
        }, 2000); // 3000 מילישניות = 3 שניות
    }
    @SuppressLint("SetTextI18n")
    private void checkUserNameAndCreateFileIfNeeded() {
        FileManager fileManager = new FileManager(this);
        try {
            File file = new File(getFilesDir(), TOTAL_USER_DATA_NAME);
            if (!file.exists()) {
                // אם הקובץ לא קיים, יציג רק בטקסט
                textViewUserName.setText("ברוך הבא, בחור יקר!");
            } else {
                // אם הקובץ קיים, נקרא את תוכן הקובץ
                List<String> lines = fileManager.readFileLines(TOTAL_USER_DATA_NAME);
                if (lines.isEmpty()) {
                    textViewUserName.setText("ברוך הבא, בחור יקר!");
                } else {
                    // קריאת שם המשתמש מהשורה הראשונה
                    for (String line : lines) {
                        if (line.startsWith(USERNAME_PREFIX)) {
                            String userName = line.substring(USERNAME_PREFIX.length()).trim();
                            textViewUserName.setText("ברוך הבא, " + userName + "!");
                            break;
                        }
                    }
                }
            }
        }catch (IOException e) {
            Toast.makeText(Welcome.this, "שגיאה בגישה לקובץ נתוני המשתמש", Toast.LENGTH_SHORT).show();
        }
    }
}