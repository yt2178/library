package com.yt.myapplication;

import android.os.Bundle;
import android.view.View;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class History extends AppCompatActivity {

    private Handler handler; // הוספת משתנה מסוג Handler
    private TextView  timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // חזרה לאחור - סגירת האקטיביטי הנוכחי
                onBackPressed();
                // אנימציה בעת חזרה לאחור
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        // אתחול ה-Handler
        handler = new Handler();
        // אתחול ה-TextView שמציג את השעה
        timeTextView = findViewById(R.id.timeTextView);
        // התחלת העדכון המתוזמן של השעה
        updateTime();

        // כל 1000 מילישניות (שנייה) העדכון יתבצע שוב
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // עדכון כל שנייה
            }
        }, 1000);
    }
    // פונקציה לעדכון השעה
    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); // פורמט השעה
        String currentTime = sdf.format(new Date()); // השעה הנוכחית
        timeTextView.setText(currentTime); // הצגת השעה ב-TextView
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // הסרת כל הקריאות המתוזמנות אם האקטיביטי נהרסה
    }
}
