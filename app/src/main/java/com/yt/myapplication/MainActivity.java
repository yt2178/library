package com.yt.myapplication;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TOTAL_PAGES = "לא הוגדר";// יעד הדפים שהמשתמש בחר (ברירת מחדל: "לא הוגדר")
    private static final String TOTAL_USER_DATA_NAME = "user_data.shinantam";
    private FileManager m_fileManager; // אובייקט לניהול קבצים
    private int m_pagesLearned; // משתנה למעקב אחרי מספר הדפים שלמד המשתמש
    private TextView m_textViewPagesLearned; // תצוגת מספר הדפים שלמד
    private TextView m_textViewPagesRemaining;// תצוגת מספר הדפים שנותרו

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        // מציב את ה-Toolbar כ-ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // חזרה לאחור - סגירת האקטיביטי הנוכחי
                onBackPressed();
            }
        });


        this.m_textViewPagesLearned = (TextView) findViewById(R.id.textViewNumberPagesLearned);
        this.m_textViewPagesRemaining = (TextView) findViewById(R.id.textViewNumberPagesRemaining);
        this.m_fileManager = new FileManager(this); // יצירת אובייקט לניהול קבצים
        try {
            String readInternalFile = this.m_fileManager.readInternalFile(TOTAL_USER_DATA_NAME);// קריאת הקובץ
            if (readInternalFile.length() == 0) {//אם הקובץ ריק
                this.m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, "0", false);
                TOTAL_PAGES = "לא הוגדר";  // אם אין קובץ או אם הוא ריק
                this.m_pagesLearned = 0;

            } else {
                this.m_pagesLearned = Integer.parseInt(readInternalFile);//אם יש בקובץ מידע זה ממיר אותו למספר
            }
        } catch (IOException e) {
            Toast.makeText(this, "הקובץ לא נמצא!!!", Toast.LENGTH_SHORT).show();
            TOTAL_PAGES = "לא הוגדר";  // אם קרתה שגיאה, היעד לא הוגדר
            this.m_pagesLearned = 0;
        }
        checkIfUserNameExists();
        updatePointsDisplay();
    }

    protected void onPause() {
        super.onPause();
        try {
            this.m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, Integer.toString(this.m_pagesLearned), false);
            this.m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, Integer.toString(this.m_pagesLearned), false);

        } catch (IOException e) {
            Log.e("IOError", "could not best score");
        }
    }
    private void askUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הזן את שמך");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = input.getText().toString();
                // אם השם לא ריק, שומר את השם שהוזן
                if (userName.isEmpty()) {
                    userName = "בחור יקר";  // אם לא הוזן שם, הגדר שם ברירת מחדל
                }
                    try {
                        // שמירת שם המשתמש לקובץ
                        m_fileManager.appendToFile("total_pages.shinantam", "שם משתמש: " + userName);
                        if (userName.equals("בחור יקר")) {
                            Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "שם המשתמש נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "אירעה שגיאה בשמירת שם המשתמש.", Toast.LENGTH_SHORT).show();
                    }

                // וודא שהמקלדת לא תיפתח שוב אחרי אישור
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }

        });

        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = "בחור יקר";  // אם נלחץ על ביטול, הגדר את השם כ"בחור יקר"
                try {
                    m_fileManager.appendToFile("total_pages.shinantam", "שם משתמש: " + userName);
                    Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בשמירת שם המשתמש.", Toast.LENGTH_SHORT).show();
                }
                // וודא שהמקלדת לא תיפתח שוב אחרי ביטול
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
            }
        });
        // הוספת Listener במקרה של חזרה (Back) או ביטול הדיאלוג (למשל, לחיצה על כפתור חזור במכשיר)
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                String userName = "בחור יקר";  // הגדרת שם ברירת מחדל במקרה של ביטול הדיאלוג
                try {
                    m_fileManager.appendToFile("total_pages.shinantam", "שם משתמש: " + userName);
                    Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בשמירת שם המשתמש.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.show();
        // הבאת המוקד (פוקוס) לתוך ה-EditText
        input.requestFocus();
        // קריאה לפונקציה שתפתח את המקלדת אחרי שהדיאלוג יוצג
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
    }

    private void checkIfUserNameExists() {
        try {
            FileManager fileManager = new FileManager(this);  // יצירת האובייקט FileManager
            List<String> lines = m_fileManager.readFileLines("total_pages.shinantam");
            if (lines.isEmpty()) {
                askUserName();
                return;
            }
            for (String line : lines) {
                if (line.startsWith("שם משתמש: ")) {
                    String userName = line.substring("שם משתמש: ".length()); // חתוך את "שם משתמש: " בלי לציין מספר קבוע
                    Toast.makeText(this, "ברוך הבא, " + userName + "!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            // אם אין שם משתמש, נבקש שם חדש
            askUserName();
        } catch (IOException e) {
            askUserName();
        }
    }

    public void onClickAddPointButton(View view) {
        if (TOTAL_PAGES.equals("לא הוגדר")) {
            this.m_pagesLearned++;
            updatePointsDisplay();
        }else{
            int totalPages = Integer.parseInt(TOTAL_PAGES);// המרת  TOTAL_PAGES - String ל- totalPages - int
            if (this.m_pagesLearned < totalPages) { //בדיקה אם שמשתמש הגיעה ליעד
                this.m_pagesLearned++;
                updatePointsDisplay();


                // בדיקה אם המשתמש השלים את כל הדפים
                if (this.m_pagesLearned == totalPages) {
                    startActivity(new Intent(this, CongratulationsActivity.class));
                }
            } else {
                Toast.makeText(this, "סיימת את ה-" + TOTAL_PAGES + " דף שלקחת על עצמך, חזק וברוך!", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void onClickRemovePointButton(View view) {
        if (this.m_pagesLearned > 0) {
            this.m_pagesLearned--;

            updatePointsDisplay();
            return;
        }
        Toast.makeText(this, "לא ניתן לרדת מתחת ל-0 דף!", Toast.LENGTH_SHORT).show();
    }
    private void openSetTargetDialog(){
        // יצירת דיאלוג
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הגדר יעד דפים");

        // יצירת הקלטת טקסט
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        // הוספת כפתור OK
        builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // וודא שהמקלדת לא תיפתח שוב אחרי ביטול
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                String target = input.getText().toString();
                if (!target.isEmpty()){
                    try {
                        int targetPages = Integer.parseInt(target);
                            TOTAL_PAGES = Integer.toString(targetPages);  // עדכון יעד הדפים
                            Toast.makeText(MainActivity.this, "היעד הוגדר בהצלחה!", Toast.LENGTH_LONG).show();
                            updatePointsDisplay();  // עדכון התצוגה לאחר עדכון היעד
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "אנא הזן מספר תקין", Toast.LENGTH_SHORT).show();
                    }
                }   else {
                TOTAL_PAGES = "לא הוגדר"; // אם לא הוזן יעד, הצג "לא הוגדר"
                updatePointsDisplay();  // עדכון התצוגה לאחר עדכון היעד
            }
        }
    });
        // הוספת כפתור Cancel
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // וודא שהמקלדת לא תיפתח שוב אחרי ביטול
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
            }});

        // הצגת הדיאלוג
        builder.show();
        // הבאת המוקד (פוקוס) לתוך ה-EditText
        input.requestFocus();
        // קריאה לפונקציה שתפתח את המקלדת אחרי שהדיאלוג יוצג
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    private void updatePointsDisplay() {
        this.m_textViewPagesLearned.setText("מספר דפים שנלמדו: " + this.m_pagesLearned);
        if (TOTAL_PAGES.equals("לא הוגדר")) {
            this.m_textViewPagesRemaining.setText("מספר דפים שנלמדו: לא הוגדר");
        } else {
            int remainingPages = Integer.parseInt(TOTAL_PAGES) - this.m_pagesLearned;
            this.m_textViewPagesRemaining.setText("מספר דפים שנותרו: " + remainingPages);;
        }
    }
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, Settings.class));
        } if (menuItem.getItemId() == R.id.menu_About) {
            startActivity(new Intent(this, About.class));
        } if (menuItem.getItemId() == R.id.menu_history) {
            startActivity(new Intent(this, History.class));
        } if (menuItem.getItemId() == R.id.ask_User_Name) {
            askUserName();
        }else if (menuItem.getItemId() == R.id.menu_set_target){
            openSetTargetDialog();
        }
        return true;
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
