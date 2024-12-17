package com.yt.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
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
    private ListView masechetListView;//משתנה מסוג ListView שמייצג את הרשימה שמוצגת למשתמש.
    private ArrayList<String> masechetList;//רשימה (ArrayList) שמכילה את כל המסכתות שמוצגות למשתמש.
    // רשימה לאחסון המסכתות שנבחרו
    private ArrayList<String> selectedMasechetList = new ArrayList<>();

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

        // יצירת רשימת המסכתות
        masechetList = new ArrayList<>();
        masechetList.add("ברכות");
        masechetList.add("שבת");
        masechetList.add("עירובין");
        masechetList.add("פסחים");
        masechetList.add("שקלים");
        masechetList.add("יומא");
        masechetList.add("סוכה");
        masechetList.add("ביצה");
        masechetList.add("ראש השנה");
        masechetList.add("תענית");
        masechetList.add("מגילה");
        masechetList.add("מועד קטן");
        masechetList.add("חגיגה");
        masechetList.add("יבמות");
        masechetList.add("כתובות");
        masechetList.add("נדרים");
        masechetList.add("נזיר");
        masechetList.add("סוטה");
        masechetList.add("גיטין");
        masechetList.add("קידושין");
        masechetList.add("בבא קמא");
        masechetList.add("בבא מציעא");
        masechetList.add("בבא בתרא");
        masechetList.add("סנהדרין");
        masechetList.add("מכות");
        masechetList.add("שבועות");
        masechetList.add("עבודה זרה");
        masechetList.add("הוריות");
        masechetList.add("זבחים");
        masechetList.add("מנחות");
        masechetList.add("חולין");
        masechetList.add("בכורות");
        masechetList.add("ערכין");
        masechetList.add("תמורה");
        masechetList.add("כריתות");
        masechetList.add("מעילה");
        masechetList.add("קינים");
        masechetList.add("תמיד");
        masechetList.add("מידות");
        masechetList.add("נידה");

        // יצירת מתאם (Adapter) להצגת המסכתות ברשימה
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, masechetList);

        // הגדרת המתאם לרשימה
        masechetListView.setAdapter(adapter);

        // רשימה של המסכתות שנבחרו
        ArrayList<String> selectedMasechetList = new ArrayList<>();

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
            }

           //שליחה לפונקציה את המסכתות שנבחרה
            Intent resultIntent = new Intent();//העברת נתונים בין אקטיביטי
            resultIntent.putStringArrayListExtra("selected_masechet_list", selectedMasechetList);//העברת הנתון המפתח הרשימה הוא  ב -" " והערך אחרי הפסיק
            setResult(RESULT_OK, resultIntent);//סיום האקטיביטי והחזרת התוצאה
        });

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
                    String[] selectedMasechetArray = selectedMasechetLine.split(",");
                    for (String selected : selectedMasechetArray) {
                        if (selected.trim().equals(masechet)) {
                            masechetAlreadySelected = true;
                            break;
                        }
                    }
                    // אם המסכת לא קיימת, הוסף אותה
                    if (!masechetAlreadySelected) {
                        if (line.endsWith(",")) {
                            lines.set(i, line + " " + masechet + ",");  // הוסף את המסכת אחרי פסיק
                        }
                    }
                    masechetLineFound = true;
                    break;
                }
            }

            // אם לא נמצאה שורה של "מסכתות שנבחרו:", הוסף שורה חדשה
            if (!masechetLineFound) {
                lines.add("מסכתות שנבחרו: " + masechet + ",");
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