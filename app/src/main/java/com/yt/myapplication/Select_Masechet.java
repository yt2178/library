package com.yt.myapplication;

import android.os.Bundle;

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
    private ListView masechetListView;
    private List<String> masechetList;
    private List<String> selectedMasechetList;  // רשימה חדשה למסכתות שנבחרו

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
        masechetList = new ArrayList<>();
        selectedMasechetList = new ArrayList<>(); // אתחול הרשימה שנבחרה

        // הוספת כל המסכתות לרשימה
        addMasechetList();

        // קריאת רשימת המסכתות שנבחרו מהקובץ
        loadSelectedMasechetFromFile();

        // הצגת הרשימה בעזרת Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, masechetList);
        masechetListView.setAdapter(adapter);

        // טופס טיפול בבחירת מסכת
        masechetListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMasechet = masechetList.get(position); // שמור את המסכת שנבחרה

            // בדיקה אם המסכת כבר נבחרה
            if (selectedMasechetList.contains(selectedMasechet)) {
                Toast.makeText(Select_Masechet.this, "המסכת " + selectedMasechet + " כבר נבחרה!", Toast.LENGTH_SHORT).show();
            } else {
                // הוספת Toast על המסכת שנבחרה
                Toast.makeText(Select_Masechet.this, "הוספת מסכת " + selectedMasechet, Toast.LENGTH_SHORT).show();

                // הוספת המסכת שנבחרה לרשימה של המסכתות שנבחרו
                selectedMasechetList.add(selectedMasechet);

                // שמירת המסכת שנבחרה בקובץ
                saveSelectedMasechetToFile(selectedMasechet);
            }
        });
    }
    private void addMasechetList() {
        // הוספת כל המסכתות לרשימה
        //סדר זרעים
        masechetList.add("ברכות");
        //סדר מועד
        masechetList.add("שבת");masechetList.add("עירובין");masechetList.add("פסחים");masechetList.add("ראש השנה");masechetList.add("יומא");masechetList.add("סוכה");masechetList.add("ביצה");masechetList.add("תענית");masechetList.add("מגילה");masechetList.add("מועד קטן");masechetList.add("חגיגה");
        //סדר נשים
        masechetList.add("יבמות");masechetList.add("כתובות");masechetList.add("נדרים");masechetList.add("נזיר");masechetList.add("סוטה");masechetList.add("גיטין");masechetList.add("קידושין");
        //סדר נזיקין
        masechetList.add("בבא קמא");masechetList.add("בבא מציעא");masechetList.add("בבא בתרא");masechetList.add("סנהדרין");masechetList.add("מכות");masechetList.add("שבועות");masechetList.add("עבודה זרה");masechetList.add("הוריות");
        //סדר קודשים
        masechetList.add("זבחים");masechetList.add("מנחות");masechetList.add("חולין");masechetList.add("בכורות");masechetList.add("ערכין");masechetList.add("תמורה");masechetList.add("כריתות");masechetList.add("מעילה");masechetList.add("תמיד");
        //סדר טהרות
        masechetList.add("נדה");


        // הצגת הרשימה בעזרת Adapter
        //ArrayAdapter: זוהי מחלקה שמחברת בין רשימה של נתונים (כמו רשימה של שמות מסכתות) לבין רכיב תצוגה (כמו ListView).
        //אנחנו משתמשים ב-android.R.layout.simple_list_item_1 כדי להציג את כל שם של מסכת בשורה אחת.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, masechetList);
       //אנו מחברים את ה-Adapter ל-ListView כדי שהוא יציג את הרשימה של המסכתות.
        masechetListView.setAdapter(adapter);

        masechetListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMasechet = masechetList.get(position); // שמור את המסכת שנבחרה
            // הוספת Toast על המסכת שנבחרה
            Toast.makeText(Select_Masechet.this, "הוספת מסכת " + selectedMasechet, Toast.LENGTH_SHORT).show();

            // שמירת המסכת שנבחרה בקובץ
            saveSelectedMasechetToFile(selectedMasechet);

        });

    }
    private void loadSelectedMasechetFromFile() {
        try {
            FileManager fileManager = new FileManager(this);
            List<String> lines = fileManager.readFileLines(TOTAL_USER_DATA_NAME);

            for (String line : lines) {
                // מחפשים את השורה שמתחילה ב-"מסכתות שנבחרו:"
                if (line.startsWith("מסכתות שנבחרו:")) {
                    // חיתוך המידע אחרי "מסכתות שנבחרו:"
                    String masechetData = line.substring("מסכתות שנבחרו:".length()).trim();

                    // אם יש פסיק בסוף, נוודא שאין אותו
                    if (masechetData.endsWith(",")) {
                        masechetData = masechetData.substring(0, masechetData.length() - 1).trim();
                    }

                    // חיתוך המידע לפי פסיקים
                    String[] masechetArray = masechetData.split(",");

                    // הוספת כל המסכתות לרשימה
                    for (String masechet : masechetArray) {
                        selectedMasechetList.add(masechet.trim());
                    }
                    break; // מצאנו את השורה, אין צורך להמשיך לחפש
                }
            }

        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בקריאת הקובץ!", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveSelectedMasechetToFile(String masechet) {
        try {
            // קריאה לקובץ שבו נשמרות המסכתות
            FileManager fileManager = new FileManager(this);
            List<String> lines = fileManager.readFileLines(TOTAL_USER_DATA_NAME);

            boolean masechetLineFound = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("מסכתות שנבחרו:")) {
                    // אם מצאנו את השורה, נוסיף את המסכת החדשה אחרי פסיק
                    if (line.endsWith(",")) {
                        // אם כבר יש פסיק בסוף, נוסיף רק את המסכת החדשה
                        lines.set(i, line + " " + masechet + ",");
                    } else {
                        // אם אין פסיק בסוף, נוסיף פסיק לפני המסכת החדשה
                        lines.set(i, line + " " + masechet + ",");
                    }
                    masechetLineFound = true;
                    break;
                }
            }

            // אם לא מצאנו שורה שמתחילה ב-"מסכתות שנבחרו:", נוסיף שורה חדשה
            if (!masechetLineFound) {
                lines.add("מסכתות שנבחרו: " + masechet + ",");
            }

            // שמירה חזרה לקובץ
            fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, String.join("\n", lines), false);

        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בשמירת המסכת לקובץ!", Toast.LENGTH_SHORT).show();
        }
    }}
