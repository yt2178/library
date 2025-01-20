package com.yt.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.IOException;
import java.util.List;

public class Welcome extends AppCompatActivity {
    private static final String TOTAL_USER_DATA = "user_data.shinantam";
    private static final String USERNAME_PREFIX = "שם משתמש:";
    // הצהרת משתנה textViewUserName
    private TextView textViewUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //הצגה על כל המסך
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        textViewUserName = findViewById(R.id.textViewUserName); // קבלת גישה לתיבת הטקסט

        checkUserNameAndCreateFileIfNeeded();
        
        // דחיית המעבר לאקטיביטי הבא ב-3 שניות
        new Handler().postDelayed(() -> {
            // יצירת Intent לעבור לאקטיביטי הבא
            Intent intent = new Intent(Welcome.this, MainActivity.class); // שים את שם הקטיביטי הבא במקום NextActivity
            startActivity(intent);
            finish(); // לסיים את הקטיביטי הנוכחי כדי שלא יחזור אליו
        }, 2000); // 2000 מילישניות = 2 שניות
    }
    @SuppressLint("SetTextI18n")
    private void checkUserNameAndCreateFileIfNeeded() {
        FileManager fileManager = new FileManager(this);
        try {
            //יוצר אובייקט File שמייצג את הקובץ בספריית הקבצים הפנימיים של האפליקציה, בעזרת הנתיב שניתן ב־getFilesDir().
            File file = new File(getFilesDir(), TOTAL_USER_DATA);
            if (!file.exists()) {//אם הקובץ לא קיים
                String defaultData = "שם משתמש:\nדפים שנלמדו:\nדפים שנשארו:\nמסכתות שנבחרו:";//הגדרת סטרינג ברירת מחדל
                fileManager.writeInternalFile(TOTAL_USER_DATA, defaultData, false);//כתיבת הברירת מחדל לקובץ
                // יציג רק בטקסט
                textViewUserName.setText("ברוך הבא, בחור יקר!");
            } else {//אם הקובץ קיים
                // קריאת את תוכן הקובץ ושמירתו כמשתנה שורות
                List<String> lines = fileManager.readFileLines(TOTAL_USER_DATA);
                if (lines.isEmpty()) {//אם השורות ריקות בקובץ
                    textViewUserName.setText("ברוך הבא, בחור יקר!");  // יציג רק בטקסט
                } else {//אם השורות לא ריקות
                    // קריאת שם המשתמש מהשורה הראשונה
                    for (String line : lines) {//עבור בלולאה על כל הקובץ
                        if (line.startsWith(USERNAME_PREFIX)) {//מצא שורה שמחילה בשם משתמש
                            //חתוך את שם המשתמש מהשורה והסרת רווחים תחילה וסוף ולוקח זאת למשתנה
                            String userName = line.substring(USERNAME_PREFIX.length()).trim();
                            textViewUserName.setText("ברוך הבא, " + userName + "!");
                            break;//עוצר את הלולאה במידה והשורה נמצאה
                        }
                    }
                }
            }
        }catch (IOException e) {
            Toast.makeText(Welcome.this, "לא ניתן לשמור נתונים", Toast.LENGTH_SHORT).show();
            // ליצור בעתיד פתיחת התמיכה במקרה שלא מצליח ליצור את הקובץ
        }
    }
}