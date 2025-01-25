package com.yt.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

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

        // קבלת גישה ל- WebView
        WebView myWebView = findViewById(R.id.webView);
        myWebView.setFocusableInTouchMode(true);
        myWebView.requestFocus();

        myWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        myWebView.getSettings().setJavaScriptEnabled(true); // מאפשר ג'אווה סקריפט
        myWebView.getSettings().setAllowFileAccess(true); // מאפשר גישה לקבצים
        myWebView.getSettings().setLoadsImagesAutomatically(true); // טוען תמונות באופן אוטומטי
        myWebView.getSettings().setDomStorageEnabled(true); // מאפשר אחסון מקומי בדפדפן
        // קביעת הרקע של WebView להיות שקוף
        myWebView.setBackgroundColor(0x00000000);  // רקע שקוף (ARGB)
        // טעינת קובץ ה-HTML מתוך תיקיית assets
        myWebView.loadUrl("file:///android_asset/about.html");
    }
}
