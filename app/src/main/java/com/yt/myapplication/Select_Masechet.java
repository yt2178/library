package com.yt.myapplication;

import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Select_Masechet extends AppCompatActivity {
    private static final String TOTAL_USER_DATA_NAME = "user_data.shinantam";
    private RecyclerView masechetRecyclerView;
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

        masechetRecyclerView = findViewById(R.id.masechetRecyclerView);
        masechetList = new ArrayList<>();
        selectedMasechetList = new ArrayList<>(); // אתחול הרשימה שנבחרה

        // הוספת כל המסכתות לרשימה
        addMasechetList();

        // קריאת רשימת המסכתות שנבחרו מהקובץ
        loadSelectedMasechetFromFile();

        // יצירת LayoutManager עבור RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        masechetRecyclerView.setLayoutManager(layoutManager);

        // יצירת Adapter עבור RecyclerView
        MasechetAdapter adapter = new MasechetAdapter(masechetList);
        masechetRecyclerView.setAdapter(adapter);
        // טיפול בבחירת מסכת
        adapter.setOnItemClickListener((position) -> {
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




    }
    private void loadSelectedMasechetFromFile() {
        try {
            FileManager fileManager = new FileManager(this);
            List<String> lines = fileManager.readFileLines(TOTAL_USER_DATA_NAME);

            for (String line : lines) {
                if (line.startsWith("מסכתות שנבחרו:")) {
                    String masechetData = line.substring("מסכתות שנבחרו:".length()).trim();
                    if (masechetData.endsWith(",")) {
                        masechetData = masechetData.substring(0, masechetData.length() - 1).trim();
                    }

                    String[] masechetArray = masechetData.split(",");
                    for (String masechet : masechetArray) {
                        selectedMasechetList.add(masechet.trim());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בקריאת הקובץ!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSelectedMasechetToFile(String masechet) {
        try {
            FileManager fileManager = new FileManager(this);
            List<String> lines = fileManager.readFileLines(TOTAL_USER_DATA_NAME);

            boolean masechetLineFound = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("מסכתות שנבחרו:")) {
                    if (line.endsWith(",")) {
                        lines.set(i, line + " " + masechet + ",");
                    } else {
                        lines.set(i, line + " " + masechet + ",");
                    }
                    masechetLineFound = true;
                    break;
                }
            }

            if (!masechetLineFound) {
                lines.add("מסכתות שנבחרו: " + masechet + ",");
            }

            fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, String.join("\n", lines), false);

        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בשמירת המסכת לקובץ!", Toast.LENGTH_SHORT).show();
        }
    }
}