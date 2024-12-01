package com.yt.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Select_Masechet extends AppCompatActivity {

    private ListView masechetListView;
    private List<String> masechetList;

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

        // רשימה לדוגמה של מסכתות
        // הוספת כל המסכתות לרשימה
        //סדר זרעים
        masechetList.add("ברכות");
        //סדר מועד
        masechetList.add("שבת");
        masechetList.add("עירובין");
        masechetList.add("פסחים");
        masechetList.add("ראש השנה");
        masechetList.add("יומא");
        masechetList.add("סוכה");
        masechetList.add("ביצה");
        masechetList.add("תענית");
        masechetList.add("מגילה");
        masechetList.add("מועד קטן");
        masechetList.add("חגיגה");
        //סדר נשים
        masechetList.add("יבמות");
        masechetList.add("כתובות");
        masechetList.add("נדרים");
        masechetList.add("נזיר");
        masechetList.add("סוטה");
        masechetList.add("גיטין");
        masechetList.add("קידושין");
        //סדר נזיקין
        masechetList.add("בבא קמא");
        masechetList.add("בבא מציעא");
        masechetList.add("בבא בתרא");
        masechetList.add("סנהדרין");
        masechetList.add("מכות");
        masechetList.add("שבועות");
        masechetList.add("עבודה זרה");
        masechetList.add("הוריות");
        //סדר קודשים
        masechetList.add("זבחים");
        masechetList.add("מנחות");
        masechetList.add("חולין");
        masechetList.add("בכורות");
        masechetList.add("ערכין");
        masechetList.add("תמורה");
        masechetList.add("כריתות");
        masechetList.add("מעילה");
        masechetList.add("תמיד");
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
          //אנחנו יוצרים אובייקט Intent חדש כדי לשלוח תוצאה לאקטיביטי הראשי (MainActivity). אנחנו שולחים את שם המסכת שנבחרה עם הפונקציה putExtra שמקבלת את השם של הנתון ואת הערך.
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedMasechet", selectedMasechet);// שלח את המסכת לאקטיביטי הראשי
            setResult(RESULT_OK, resultIntent);// שלח תוצאה לאקטיביטי הראשי
            finish();  // סגור את אקטיביטי Select_Masechet
        });
    }
}
