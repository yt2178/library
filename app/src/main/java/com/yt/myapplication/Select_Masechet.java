package com.yt.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Select_Masechet extends AppCompatActivity {
    private static final String TOTAL_USER_DATA_NAME = "user_data.shinantam";
    private ListView masechetListView;//משתנה מסוג ListView שמייצג את המסכתות שמוצגת למשתמש.
    private List<String> masechetList;//רשימה (ArrayList) שמכילה את כל המסכתות שמוצגות למשתמש.
    private ArrayList<String> selectedMasechetList; // רשימה של המסכתות שנבחרו
    private CustomAdapterListMasechet adapter;// המתאם שלנו

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_masechet);

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
        masechetListView = findViewById(R.id.masechetListView);
        masechetListView.setFocusable(true);
        masechetListView.setFocusableInTouchMode(true);
        masechetListView.requestFocus();
        //קבלת רשימת המסכתות מפונקציה
        masechetList = MasechetData.getMasechetList();
        // יצירת רשימה (ריקה בינתיים) של המסכתות שנבחרו
        selectedMasechetList = new ArrayList<>();
        // קריאת המסכתות שנבחרו מתוך הקובץ
        loadSelectedMasechetFromFile();

        // יצירת המתאם (Adapter) שלנו שמציג את המסכתות ברשימה
        adapter = new CustomAdapterListMasechet(this, masechetList, selectedMasechetList);

        // הגדרת המתאם לרשימה
        masechetListView.setAdapter(adapter);

        // טיפול בלחיצה על פריט ברשימה
        masechetListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMasechet = masechetList.get(position);//קבלת מיקום הלחיצה ושמירתו במשתנה

            // בדיקה אם המסכת כבר נבחרה
            if (selectedMasechetList.contains(selectedMasechet)) {
                Toast.makeText(this, "המסכת כבר נבחרה!", Toast.LENGTH_SHORT).show();
            } else {
                // הוספת המסכת שנבחרה לרשימה
                selectedMasechetList.add(selectedMasechet);
                // שליחה ל-Intent עם כל המסכתות שנבחרו
                saveSelectedMasechetToFile(selectedMasechet);
                // עדכון המתאם
                adapter.notifyDataSetChanged();
            }

           //שליחה לפונקציה את המסכתות שנבחרה
            Intent resultIntent = new Intent();//העברת נתונים בין אקטיביטי
            resultIntent.putStringArrayListExtra("selected_masechet_list", selectedMasechetList);//העברת הנתון המפתח הרשימה הוא  ב -" " והערך אחרי הפסיק
            setResult(RESULT_OK, resultIntent);//סיום האקטיביטי והחזרת התוצאה
        });

    }
    // קריאת המסכתות שנבחרו מתוך הקובץ
    private void loadSelectedMasechetFromFile() {
        try {
            FileManager fileManager = new FileManager(this);
            List<String> lines = fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            for (String line : lines) {
                if (line.startsWith("מסכתות שנבחרו:")) {
                    String selectedMasechetLine = line.substring("מסכתות שנבחרו:".length()).trim();
                    String[] selectedMasechetArray = selectedMasechetLine.split("|");
                    for (String selected : selectedMasechetArray) {
                        selectedMasechetList.add(selected.trim());
                    }
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בטעינת המסכתות שנבחרו!", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveSelectedMasechetToFile(String masechet) {
        try {
            // ניהול הקובץ
            FileManager fileManager = new FileManager(this);
            List<String> lines = fileManager.readFileLines(TOTAL_USER_DATA_NAME);

            boolean masechetLineFound = false;
            boolean masechetAlreadySelected = false;

            // בדיקה אם המסכת כבר נבחרה בעבר
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("מסכתות שנבחרו:")) {
                    // אם המסכת נמצאת ברשימה, אל תוסיף אותה שוב
                    String selectedMasechetLine = line.substring("מסכתות שנבחרו:".length()).trim();
                    String[] selectedMasechetArray = selectedMasechetLine.split("|");
                    for (String selected : selectedMasechetArray) {
                        if (selected.trim().equals(masechet)) {
                            masechetAlreadySelected = true;
                            break;
                        }
                    }
                    // אם המסכת לא קיימת, הוסף אותה
                    if (!masechetAlreadySelected) {
                        if (line.endsWith("|")) {
                            lines.set(i, line + " " + masechet + "|");  // הוסף את המסכת אחרי פסיק
                        }
                    }
                    masechetLineFound = true;
                    break;
                }
            }

            // אם לא נמצאה שורה של "מסכתות שנבחרו:", הוסף שורה חדשה
            if (!masechetLineFound) {
                lines.add("מסכתות שנבחרו: " + masechet + "|");
            }

            // אם המסכת כבר נבחרה, הצג הודעת שגיאה
            if (masechetAlreadySelected) {
                Toast.makeText(this, "כבר בחרת מסכת זאת!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "המסכת נוספה בהצלחה!", Toast.LENGTH_SHORT).show();
                // אם לא נבחרה, שמור את השינויים בקובץ
                fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, String.join("\n", lines), false);
            }

        } catch (IOException e) {
            // אם יש שגיאה בקריאת או כתיבת הקובץ, הצג הודעת שגיאה
            Toast.makeText(this, "שגיאה בשמירת המסכת לקובץ!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event);
        }

        int currentPosition = masechetListView.getSelectedItemPosition();

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (currentPosition > 0) {
                    masechetListView.setSelection(currentPosition - 1);
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (currentPosition < masechetListView.getCount() - 1) {
                    masechetListView.setSelection(currentPosition + 1);
                }
                return true;

            case KeyEvent.KEYCODE_ENTER:
                String selectedMasechet = (String) masechetListView.getItemAtPosition(currentPosition);
                if (selectedMasechet != null) {
                    saveSelectedMasechetToFile(selectedMasechet);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selected_masechet", selectedMasechet);
                    setResult(RESULT_OK, resultIntent);
                }
                return true;

            default:
                return super.dispatchKeyEvent(event);
        }
    }
}