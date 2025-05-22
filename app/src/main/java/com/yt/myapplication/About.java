package com.yt.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    new AlertDialog.Builder(About.this)
                            .setTitle("יומן שינויים")
                            .setMessage(Html.fromHtml(readChangelogFromAssets(), Html.FROM_HTML_MODE_LEGACY))

                            .setPositiveButton("אישור", (dialog, which) -> dialog.dismiss())
                            .setOnCancelListener(dialog -> finish())
                            .show();
                }

            }
        });

    }
    private String readChangelogFromAssets() {
        try {
            InputStream is = getAssets().open("changelog.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "לא ניתן לטעון את יומן השינויים.";
        }
    }

}
