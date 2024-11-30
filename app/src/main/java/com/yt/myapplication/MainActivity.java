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
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static String TOTAL_PAGES = "לא הוגדר";// יעד הדפים שהמשתמש בחר (ברירת מחדל: "לא הוגדר")
    private static final String TOTAL_USER_DATA_NAME = "user_data.shinantam";
    // הגדרה של קבוע
    private static final String USERNAME_PREFIX = "שם משתמש: ";

    private FileManager m_fileManager; // אובייקט לניהול קבצים
    private String m_pagesLearned; // משתנה למעקב אחרי מספר הדפים שלמד המשתמש
    private TextView m_textViewPagesLearned; // תצוגת מספר הדפים שלמד
    private TextView m_textViewPagesRemaining;// תצוגת מספר הדפים שנותרו

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
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


        this.m_textViewPagesLearned = (TextView) findViewById(R.id.textViewNumberPagesLearned);
        this.m_textViewPagesRemaining = (TextView) findViewById(R.id.textViewNumberPagesRemaining);
        this.m_fileManager = new FileManager(this); // יצירת אובייקט לניהול קבצים
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            if (lines.isEmpty()) {//אם הרשימה ריקה
                this.m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, "דפים שנלמדו:" + "0", false);
                Toast.makeText(this, "!כניסה ראשונה, ברוכים הבאים", Toast.LENGTH_SHORT).show();
                return;
            }
            for (String line : lines) {
                if (line.startsWith("דפים שנלמדו:")) {
                    // חותך את "דפים שנלמדו:" בלי לציין מספר קבוע ושומר אות במשתנה
                    this.m_pagesLearned = line.substring("דפים שנלמדו:".length());
                    return;
                }
            }

        } catch (IOException e) {
            Toast.makeText(this, "שגיאה! לא הצליח להפוך את הקובץ לרשימה!", Toast.LENGTH_SHORT).show();
        }
        checkIfUserNameExists();
        updatePointsDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            if (lines.isEmpty()) {
                lines.add(USERNAME_PREFIX + "בחור יקר");
                lines.add("דפים שנלמדו:" + m_pagesLearned);
            } else if (lines.size() > 1) {
                lines.set(1, "דפים שנלמדו:" + m_pagesLearned);
            } else {
                lines.add("דפים שנלמדו:" + m_pagesLearned);
            }
            m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, String.join("\n", lines), false);
        } catch (IOException e) {
            Log.e("IOError", "לא ניתן לשמור את נתוני המשתמש!");
        }
    }

    private void askUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הזן את שמך");

        final EditText input = new EditText(this);//יצירת שדה קלט של טקסט
        builder.setView(input);//הכנסת  שדה הקלט לדיאלוג

        builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = input.getText().toString();
                // אם השם ריק, שומר את השם שהוזן
                if (userName.isEmpty()) {
                    userName = "בחור יקר";  // אם לא הוזן שם, הגדר המשתנה כברירת מחדל
                }try {
                List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
                if (lines.isEmpty()) {
                    lines.add(USERNAME_PREFIX + userName); // שורה ראשונה: שם המשתמש
                }else{
                    lines.set(0, USERNAME_PREFIX + userName);
                }
                m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME,String.join("/n",lines),false);
                        if (userName.equals("בחור יקר")) {
                            Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "שם המשתמש נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                        }//במידה וקרתה שגיאה כתיבה לקובץ הקפץ הודעה

                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "אירעה שגיאה בשמירת שם המשתמש!", Toast.LENGTH_SHORT).show();
                }

                hideKeyboard(input);
                }

        });

        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = "בחור יקר";  // אם נלחץ על ביטול, הגדר את השם כ"בחור יקר"
                Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                try {
                    List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);//קורא את הקובץ לרשימה שנשמרת במשתנה
                    if (lines.isEmpty()) {//אם  כל השורות ריקות
                        lines.add(USERNAME_PREFIX + userName); // שורה ראשונה: שם המשתמש
                    }else{//אם השורות לא ריקות
                        lines.set(0, USERNAME_PREFIX + userName);//הגדרת השורה הראשונה בתור שם משתמש
                        lines.set(1,m_pagesLearned);
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בהגדרת שם משתמש ברירת מחדל1", Toast.LENGTH_SHORT).show();
                }
                hideKeyboard(input);
            }
        });
        // הוספת Listener במקרה של חזרה (Back) או ביטול הדיאלוג (למשל, לחיצה על כפתור חזור במכשיר)
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                String userName = "בחור יקר";  // הגדרת שם ברירת מחדל במקרה של ביטול הדיאלוג
                try {
                    List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);//קורא את הקובץ לרשימה שנשמרת במשתנה
                    if (lines.isEmpty()) {//אם  כל השורות ריקות
                        lines.add(USERNAME_PREFIX + userName); // שורה ראשונה: שם המשתמש
                    }else{//אם השורות לא ריקות
                        lines.set(0, USERNAME_PREFIX + userName);//הגדרת השורה הראשונה בתור שם משתמש
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בהגדרת שם משתמש ברירת מחדל2", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.show();
        // הבאת המוקד (פוקוס) לתוך ה-EditText
        input.requestFocus();
        showKeyboard(input);
    }

    private void checkIfUserNameExists() {
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            if (lines.isEmpty()) {//אם הרשימה ריקה לבקש שם משתמש
                askUserName();
                return;
            }
            for (String line : lines) {
                if (line.startsWith(USERNAME_PREFIX)) {
                    String userName = line.substring(USERNAME_PREFIX.length()).trim(); // חתוך את "שם משתמש: " בלי לציין מספר קבוע,,והסר רווחים מיותרים
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

    private void openSetTargetDialog(){
        // יצירת דיאלוג
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הגדר יעד דפים");

        final EditText input = new EditText(this);//יצירת שדה קלט של טקסט
        input.setInputType(InputType.TYPE_CLASS_NUMBER);//הגדרת שדה הקלט שיקלוט רק מספרים
        builder.setView(input);//הכנסת  שדה הקלט לדיאלוג

        builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideKeyboard(input);
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
                hideKeyboard(input);
            }});

        // הצגת הדיאלוג
        builder.show();
        // הבאת המוקד (פוקוס) לתוך ה-EditText
        input.requestFocus();
      showKeyboard(input);
    }

    public void onClickAddPointButton(View view) {
        if (TOTAL_PAGES.equals("לא הוגדר")) {
            // המרת המחרוזת למספר
            int learnedPages = Integer.parseInt(this.m_pagesLearned);
            // הוספת נקודה
            learnedPages++;
            // המרה חזרה לסטרינג ושמירה במשתנה
            this.m_pagesLearned = String.valueOf(learnedPages);
            updatePointsDisplay();
        }else{
            int totalPages = Integer.parseInt(TOTAL_PAGES);// המרת  TOTAL_PAGES - String ל- totalPages - int
           int learnedPages = Integer.parseInt(this.m_pagesLearned);// המרת  this.m_pagesLearned - String ל- learnedPages - int
            if (learnedPages < totalPages) { //בדיקה אם שמשתמש הגיעה ליעד
                learnedPages++;
                // המרה חזרה לסטרינג ושמירה
                this.m_pagesLearned = String.valueOf(learnedPages);
                updatePointsDisplay();


                // בדיקה אם המשתמש השלים את כל הדפים
                if (learnedPages == totalPages) {
                    startActivity(new Intent(this, CongratulationsActivity.class));
                }
            } else {
                Toast.makeText(this, "סיימת את ה-" + TOTAL_PAGES + " דף שלקחת על עצמך, חזק וברוך!", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void onClickRemovePointButton(View view) {
        // המרת המחרוזת למספר
        int learnedPages = Integer.parseInt(this.m_pagesLearned);
        if (learnedPages > 0) {
            learnedPages--;
            // המרה חזרה לסטרינג ושמירה במשתנה
            this.m_pagesLearned = String.valueOf(learnedPages);
            updatePointsDisplay();
            return;
        }
        Toast.makeText(this, "לא ניתן לרדת מתחת ל-0 דף!", Toast.LENGTH_SHORT).show();
    }
    private void updatePointsDisplay() {
        this.m_textViewPagesLearned.setText("מספר דפים שנלמדו: " + this.m_pagesLearned);
        if (TOTAL_PAGES.equals("לא הוגדר")) {
            this.m_textViewPagesRemaining.setText("מספר דפים שנלמדו: לא הוגדר");
        } else {
            // המרת המחרוזת למספר
            int learnedPages = Integer.parseInt(this.m_pagesLearned);
            int remainingPages = Integer.parseInt(TOTAL_PAGES) - learnedPages;
            // המרה חזרה לסטרינג ושמירה במשתנה
            this.m_pagesLearned = String.valueOf(learnedPages);
            this.m_textViewPagesRemaining.setText("מספר דפים שנותרו: " + remainingPages);;
        }
    }
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_select_maschet) {
            startActivity(new Intent(this, Select_Masechet.class));
        }if (menuItem.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, Settings.class));
        }if (menuItem.getItemId() == R.id.menu_About) {
            startActivity(new Intent(this, About.class));
        }if (menuItem.getItemId() == R.id.menu_history) {
            startActivity(new Intent(this, History.class));
        }if (menuItem.getItemId() == R.id.ask_User_Name) {
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
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}

