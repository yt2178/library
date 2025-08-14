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


public class SelectMasechet extends AppCompatActivity {
    private static final String TOTAL_USER_DATA = "user_data.shinantam";
    private ListView masechetListView;//משתנה מסוג ListView שמייצג את המסכתות שמוצגת למשתמש.
    private List<String> masechetList;//רשימה (ArrayList) שמכילה את כל המסכתות שמוצגות למשתמש.
    private ArrayList<String> selectedMasechetList; // רשימה של המסכתות שנבחרו
    private CustomAdapterListMasechet adapter;// המתאם שלנו
    private FileManager m_fileManager; // אובייקט לניהול קבצים
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
                // אנימציה בעת חזרה לאחור
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        m_fileManager = new FileManager(this);
        masechetListView = findViewById(R.id.masechetListView);
        masechetListView.setFocusable(true);
        masechetListView.setFocusableInTouchMode(true);
        masechetListView.requestFocus();
        //קבלת רשימת המסכתות מפונקציה
        masechetList = MasechetData.getMasechetList();
        // יצירת רשימה (ריקה בינתיים) של המסכתות שנבחרו
        selectedMasechetList = new ArrayList<>();
        // קריאת המסכתות שנבחרו מתוך הקובץ
        updateSelectedMasechetFromFile();
        // יצירת המתאם (Adapter) שלנו שמציג את המסכתות ברשימה
        adapter = new CustomAdapterListMasechet(this, masechetList, selectedMasechetList);
        // הגדרת המתאם לרשימה
        masechetListView.setAdapter(adapter);
        // טיפול בלחיצה על פריט ברשימה
        masechetListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMasechet = masechetList.get(position);//קבלת מיקום הלחיצה ושמירתו במשתנה selectedMasechet
            if (selectedMasechetList.contains(selectedMasechet)) {//אם המסכת שנבחרה נמצאת בתוך רשימת המסכתות שנבחרו
                Toast.makeText(this, "המסכת כבר נבחרה!", Toast.LENGTH_SHORT).show();
            } else {//אם המסכת שנבחרה לא נמצאת בתוך רשימת המסכתות שנבחרו
                selectedMasechetList.add(selectedMasechet); // הוספת המסכת שנבחרה לרשימת המסכתות שנבחרו
                saveSelectedMasechetToFile(selectedMasechet);//שמירת המסכתות שנבחרו לקובץ
                HistoryUtils.logAction(SelectMasechet.this, "נבחרה מסכת " + selectedMasechet);
                adapter.notifyDataSetChanged();  // עדכון המתאם
            }
           //שליחה לפונקציה את המסכתות שנבחרה
            Intent resultIntent = new Intent();//העברת נתונים בין אקטיביטי
            resultIntent.putStringArrayListExtra("selected_masechet_list", selectedMasechetList);//העברת הנתון המפתח הרשימה הוא  ב -" " והערך אחרי הפסיק
            setResult(RESULT_OK, resultIntent);//סיום האקטיביטי והחזרת התוצאה
        });//נבדק
    }
    // קריאת המסכתות שנבחרו מתוך הקובץ
    private void updateSelectedMasechetFromFile() {
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            for (String line : lines) {
                // מחפשים את השורה שמתחילה ב-"מסכתות שנבחרו:"
                if (line.startsWith("מסכתות שנבחרו:")) {
                    String selectedMasechetLine = line.substring("מסכתות שנבחרו:".length()).trim();
                    // פיצול כל המסכתות שנבחרו לפי | ושומר במערך ללא הסמל |
                    String[] selectedMasechetArray = selectedMasechetLine.split("\\|");
                    for (String selected : selectedMasechetArray) {

                        String masechetName = selected.split("_")[0].trim();


                        if (!masechetName.isEmpty()) {
                            selectedMasechetList.add(masechetName);  // מוסיף רק אם המסכת לא ריקה
                        }
                    }
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בטעינת המסכתות שנבחרו!", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveSelectedMasechetToFile(String masechet) {
        try {
            // קריאת כל השורות בקובץ
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            // עובר על כל השורות בקובץ ומחפש את השורה שמתחילה ב-"מסכתות שנבחרו:"
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                // מחפשים את השורה שמתחילה ב-"מסכתות שנבחרו:"
                if (line.startsWith("מסכתות שנבחרו:")) {
                    // חיתוך כל המסכתות שנבחרו מתוך השורה
                    String selectedMasechetLine = line.substring("מסכתות שנבחרו:".length()).trim();
                    // אם אין מסכתות כבר בקובץ
                    if (selectedMasechetLine.isEmpty()) {
                        Toast.makeText(this, "בחירת המסכת פעם ראשונה!", Toast.LENGTH_SHORT).show();
                        lines.set(i, line + masechet + "_|");
                        break;//יציאה מהלולאה
                    }
                    //  פיצול כל המסכתות שנבחרו לפי | ושומר במערך ללא הסמל |
                    String[] selectedMasechetArray = selectedMasechetLine.split("\\|");
                    // בדוק אם המסכת כבר קיימת בקובץ (בלי הנקודה בסוף)
                    for (String selected : selectedMasechetArray) {
                        selected = selected.trim();  // חיתוך רווחים
                        if (selected.endsWith("_")) {
                            selected = selected.substring(0, selected.length() - 1);  // חיתוך הנקודה בסוף
                        }
                        if (selected.equals(masechet)) {
                            Toast.makeText(this, "כבר בחרת מסכת זאת!", Toast.LENGTH_SHORT).show();
                            return; // אם המסכת כבר קיימת, עצור את הפונקציה
                        }
                    }
                    // אם המסכת לא קיימת, הוסף אותה לקובץ
                    lines.set(i, line + masechet + "_|");
                    Toast.makeText(this, "המסכת נוספה בהצלחה!", Toast.LENGTH_SHORT).show();
                    break; // אחרי שמצאנו את השורה והוספנו את המסכת, יצאנו מהלולאה
                }
            }
            // שמור את הקובץ לאחר עדכון
            m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
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