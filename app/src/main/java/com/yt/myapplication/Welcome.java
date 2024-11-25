package com.yt.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // דחיית מעבר לקטיביטי הבא ב-3 שניות
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // יצירת Intent לעבור לקטיביטי הבא
                Intent intent = new Intent(Welcome.this, MainActivity.class); // שים את שם הקטיביטי הבא במקום NextActivity
                startActivity(intent);
                finish(); // לסיים את הקטיביטי הנוכחי כדי שלא יחזור אליו
            }
        }, 3000); // 3000 מילישניות = 3 שניות
    }
}