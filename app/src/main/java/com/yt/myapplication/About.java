package com.yt.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // מציב את ה-Toolbar כ-ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // קביעת כותרת במרכז
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // כפתור חזור
        // קביעת הכותרת דרך ה-TextView
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("אודות");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // חזרה לאחור - סגירת האקטיביטי הנוכחי
                onBackPressed();
                // אנימציה בעת חזרה לאחור
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            }
        });
        TextView emailTextView = findViewById(R.id.emailTextView);
        emailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent עם URI של דואר אלקטרוני
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "yt0508352872@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "אפליקציית ושננתם");  // הוספת נושא לדוא"ל
                startActivity(Intent.createChooser(intent, "בחר אפליקציה לשלוח"));
            }
        });
        Button DiaryChanges = findViewById(R.id.DiaryChanges);
        DiaryChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(About.this)
                        .setTitle("יומן שינויים")
                        .setMessage("גירסה 1.0.0 \n* עודכנו כל מספרי הדפים של כלל המסכתות למספר הדפים הנכון והמעודכן.")
                        .setPositiveButton("אישור", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setOnCancelListener(dialog -> finish())
                        .show();
            }
        });

    }
}
