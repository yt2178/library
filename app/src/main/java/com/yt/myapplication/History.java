package com.yt.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class History extends AppCompatActivity {

    private RecyclerView recyclerView;//רשימת ההיסטוריה
    private HistoryAdapter historyAdapter;//משתנה של האדפטר שמחבר בין נתוני ההיסטוריה לרשימה
    private List<HistoryItem> historyList = new ArrayList<>();
    private SharedPreferences sharedPreferences;//שמירת נתונים באופן מקומי
    private static final String HISTORY_DATA = "history_data";//שם הקובץ המקומי
    private static final String HISTORY_KEY = "history_key";//מפתח לרשימה שבקובץ המקומי
    private TextView emptyHistoryTextView;//TextView שמוצג אם ההיסטוריה ריקה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        // מציב את ה-Toolbar כ-ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//כפתור חזור
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // חזרה לאחור - סגירת האקטיביטי הנוכחי
                onBackPressed();
                // אנימציה בעת חזרה לאחור
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        // אתחול של SharedPreferences
        sharedPreferences = getSharedPreferences(HISTORY_DATA, MODE_PRIVATE);

        // אתחול RecyclerView
        recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setFocusable(true);
        recyclerView.setFocusableInTouchMode(true);
        recyclerView.requestFocus();

        // טעינת ההיסטוריה
        historyList = loadHistory(this);  // טוען את ההיסטוריה


        // אתחול TextView להיסטוריה ריקה
        emptyHistoryTextView = findViewById(R.id.emptyHistoryTextView);
        // אם הרשימה ריקה, הצג את ה-TextView
        updateEmptyView();
        // אתחול ה-Adapter


        historyAdapter = new HistoryAdapter(this, historyList);
        recyclerView.setAdapter(historyAdapter);
        // אתחול ה-TextView של השעה
        //TextView שמציג את השעה
        TextView timeTextView = findViewById(R.id.timeTextView);
        updateTime(timeTextView);

        // טעינת ההיסטוריה
        loadHistory(this);
        // כפתור למחיקת ההיסטוריה
        Button clearHistoryButton = findViewById(R.id.clearHistoryButton);
        clearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (historyList.isEmpty()) {
                    // אם ההיסטוריה ריקה, מציגים Toast
                    Toast.makeText(History.this, "אין היסטוריה למחוק!", Toast.LENGTH_SHORT).show();
                } else {
                    showClearHistoryDialog();  // הצגת הדיאלוג למחיקה
                }
            }
        });
        // טעינת ההיסטוריה
        loadHistory(this);
    }
    private void showClearHistoryDialog() {
        new AlertDialog.Builder(History.this)
                .setTitle("האם אתה בטוח?")
                .setMessage("האם אתה בטוח שברצונך למחוק את כל ההיסטוריה?")
                .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearHistory();  // מחיקת כל ההיסטוריה
                    }
                })
                .setNegativeButton("לא", null)
                .show();
    }
    private void clearHistory() {
        // שמירת רשימה ריקה ב-SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HISTORY_KEY, "");
        editor.apply();
        // עדכון ה-RecyclerView
        historyList.clear();
        historyAdapter.notifyDataSetChanged();
        // עדכון המצב של ההיסטוריה הריקה
        updateEmptyView();
        Toast.makeText(History.this, "ההיסטוריה נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
    }
    // פונקציה לעדכון מצב הצגת ההיסטוריה הריקה
    void updateEmptyView() {
        if (historyList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);  // מחביא את ה-RecyclerView
            emptyHistoryTextView.setVisibility(View.VISIBLE);  // מציג את ה-TextView עם ההודעה
        } else {
            recyclerView.setVisibility(View.VISIBLE);  // מציג את ה-RecyclerView
            emptyHistoryTextView.setVisibility(View.GONE);  // מחביא את ה-TextView
        }
    }
    private void updateTime(final TextView timeTextView) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                timeTextView.setText(currentTime);
                handler.postDelayed(this, 1000); // עדכון כל שנייה
            }
        }, 1000);
    }

    // שינוי כאן: הפכנו את הפונקציה לסטטית
    public static void logAction(Context context, String action) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(HISTORY_DATA, Context.MODE_PRIVATE);
        // יצירת תאריך ושעה עבור הפעולה
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateTime = sdf.format(new Date());

        // יצירת אובייקט HistoryItem עם הפעולה והזמן הנוכחי
        HistoryItem newItem = new HistoryItem(action, dateTime);

        // טעינת ההיסטוריה הנוכחית
        List<HistoryItem> historyList = loadHistory(context);

        // הוספת הפעולה לרשימה
        historyList.add(newItem);

        // שמירה ב-SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HISTORY_KEY, convertListToString(historyList));
        editor.apply();
    }

    // המרת רשימת HistoryItem למחרוזת לשמירה ב-SharedPreferences
    private static String convertListToString(List<HistoryItem> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (HistoryItem item : list) {
            stringBuilder.append(item.getAction()).append(" - ").append(item.getTimestamp()).append("\n");
        }
        return stringBuilder.toString();
    }
    // טעינת ההיסטוריה מ-SharedPreferences
    public static List<HistoryItem> loadHistory(Context context) {
        List<HistoryItem> historyList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(HISTORY_DATA, Context.MODE_PRIVATE);
        String historyString = sharedPreferences.getString(HISTORY_KEY, "");
        if (!historyString.isEmpty()) {
            String[] historyItems = historyString.split("\n");
            for (String historyItem : historyItems) {
                String[] parts = historyItem.split(" - ");
                if (parts.length == 2) {
                    historyList.add(new HistoryItem(parts[0], parts[1]));
                }
            }
        }
        return historyList;
    }

}
