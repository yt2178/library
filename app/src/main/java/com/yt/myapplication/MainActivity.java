package com.yt.myapplication;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TOTAL_USER_DATA = "user_data.shinantam";
    // הגדרה של קבוע
    private static final String USERNAME_PREFIX = "שם משתמש:";
    private static final int REQUEST_CODE = 1;
    private FileManager m_fileManager; // אובייקט לניהול קבצים
    private int m_pagesLearned; // משתנה למעקב אחרי מספר הדפים שלמד המשתמש
    private int m_pagesRemaining; // משתנה למעקב אחרי מספר הדפים שנשאר למשתמש
    private TextView textViewNumberPagesLearned; // תצוגת מספר הדפים שלמד
    private TextView textViewNumberPagesRemaining;// תצוגת מספר הדפים שנותרו
    private boolean isDialogOpen = false;
    private ListView selectedmasechetListView;
    private List<String> selectedMasechetList; // הוספנו את הרשימה כאן
    private Spinner spinner;

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
        this.textViewNumberPagesLearned = findViewById(R.id.textViewNumberPagesLearned);//מציאת ה-ID של דפים שנלמדו
        this.textViewNumberPagesRemaining = findViewById(R.id.textViewNumberPagesRemaining);//מציאת ה-ID של דפים שנשארו
        selectedmasechetListView = findViewById(R.id.masechetListView);//מציאת ה-ID של הרשימה של המסכתות שנבחרו
        spinner = findViewById(R.id.spinner);
        m_fileManager = new FileManager(this);// יצירת אובייקט FileManager
        // קריאת הנתונים מהקובץ לפני כל שימוש במשתנים
        try {
            m_fileManager = new FileManager(this); //יצירת אובייקט לניהול קבצים
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);//קריאת הקובץ
            // לולאת חיפוש שם המשתמש בקובץ
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                if (line.startsWith("דפים שנלמדו:")) {//אם השורה מתחילה ב-"דפים שנלמדו:"
                        //חותך את הדפים שנלמדו מהשורה ומסיר רווחים בהתחלה ובסוף ולוקח זאת למשתנה
                        String PagesLearned = line.substring("דפים שנלמדו:".length()).trim();
                    if (!PagesLearned.isEmpty()) { // בדיקה אם המחרוזת אינה ריקה
                        m_pagesLearned = Integer.parseInt(PagesLearned);
                    } else {
                        m_pagesLearned = 0; // ערך ברירת מחדל במקרה של מחרוזת ריקה
                    }
                    break;  // יציאה מהלולאה לאחר עדכון
                }
            }
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                if (line.startsWith("דפים שנשארו:")) {//אם השורה מתחילה ב-"דפים שנשארו:"
                    //חותך את הדפים שנלמדו מהשורה ומסיר רווחים בהתחלה ובסוף ולוקח זאת למשתנה
                    String PagesRemaining = line.substring("דפים שנשארו:".length()).trim();
                    if (!PagesRemaining.isEmpty()) { // בדיקה אם המחרוזת אינה ריקה
                        m_pagesRemaining = Integer.parseInt(PagesRemaining);
                    } else {
                        m_pagesRemaining = 0; // ערך ברירת מחדל במקרה של מחרוזת ריקה
                    }
                    break;  // יציאה מהלולאה לאחר עדכון
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה! לא הצליח להפוך את הקובץ לרשימה!", Toast.LENGTH_SHORT).show();
        }
        checkIfUserNameExists();//בדיקה אם קיים שם משתמש
        updateDafDisplay();//עדכון התצוגה
        selectedmasechetListView.setFocusable(true);//פוקוס
        selectedmasechetListView.setFocusableInTouchMode(true);//פוקוס
        selectedmasechetListView.requestFocus();//פוקוס
        selectedMasechetList = new ArrayList<>();
        isDialogOpen = false;//הדיאלוג מוגדר כסגור והתפריט יכול להפתח כרגיל
        // הצגת המסכתות ברשימה (ListView)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedMasechetList);
        selectedmasechetListView.setAdapter(adapter);
        selectedmasechetListView.requestFocus(); // מבטיח שהרשימה תקבל פוקוס אחרי עדכון הנתונים
        //********לחיצה רגילה על מסכת מהרשימה תפתח את מספר הדפים שלה - בעתיד*****

        //לחיצה ארוכה על מסכת מהרשימה תפעיל פונקציה
        selectedmasechetListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String masechetToRemove = selectedMasechetList.get(position); // המסכת שנבחרה נשמרת במשתנה
                showRemoveMasechetDialog(masechetToRemove, position); // שליחת המסכת לפונקציה והפעלת הפונקציה
                return true; // מנע את פעולתו הרגילה של הלחיצה
            }
        });
        loadSelectedMasechetFromFile(); // קריאת המסכתות שנבחרו מהקובץ
    }
    @Override
    protected void onResume() {//חזרה למצב פעיל לאקטיביטי
        super.onResume();
        // הגדרת פוקוס על ה-ListView
        selectedmasechetListView.requestFocus();
        selectedmasechetListView.setFocusable(true);
        selectedmasechetListView.setFocusableInTouchMode(true);
    }
    @Override
    protected void onPause() {//יציאה ממצב פעיל ועובד ברקע
        super.onPause();
        updateDafDisplay();
        isDialogOpen = false;//הדיאלוג מוגדר כסגור והתפריט יכול להפתח כרגיל
    }
    private void checkIfUserNameExists() {
        try {
            this.m_fileManager = new FileManager(this); // יצירת אובייקט לניהול קבצים
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);//קריאת הקובץ
             for (String line : lines) {
                if (line.startsWith(USERNAME_PREFIX)) {
                    String userName = line.substring(USERNAME_PREFIX.length()).trim(); // חתוך את "שם משתמש: " בלי לציין מספר קבוע,,והסר רווחים מיותרים
                    Toast.makeText(this, "ברוך הבא  " + userName + "!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // אם אין שורה שמתחילה ב"שם משתמש:" נבקש שם משתמש
            askUserName();
        } catch (IOException e) {//אם הקובץ לא נמצא
            askUserName();
        }
    }//נבדק
    private void askUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//בניית הדיאלוג
        builder.setTitle("הזן את שמך");//הגדרת כותרת לדיאלוג
        final EditText input = new EditText(this);//יצירת שדה קלט של טקסט
        builder.setView(input);//הכנסת  שדה הקלט לדיאלוג
        try {//קריאה לשם המשתמש הקיים והכנסתו כשקוף לשדה הקלט
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            String UserNameDefault = "בחור יקר"; // ברירת מחדל
                for (String line : lines) {
                    if (line.startsWith(USERNAME_PREFIX)) {
                        UserNameDefault = line.substring(USERNAME_PREFIX.length());
                        break;
                    }
                }
            input.setHint(UserNameDefault); // הצגת שם המשתמש בשדה הקלט
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "אירעה שגיאה בקריאת שם המשתמש הקודם", Toast.LENGTH_SHORT).show();
        }
        builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = input.getText().toString();//לקיחת הטקסט שהוכנס והפיכתו למשתנה
                try {
                    m_fileManager = new FileManager(MainActivity.this); //יצירת אובייקט לניהול קבצים
                    List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);//קריאת הקובץ
                    if (userName.isEmpty()) {
                        userName = "בחור יקר";  // אם לא הוזן שם, הגדר המשתנה כברירת מחדל
                    }
                    // לולאת חיפוש שם המשתמש בקובץ
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                        if (line.startsWith(USERNAME_PREFIX)) {
                            // אם השורה מתחילה ב-"שם משתמש:", עדכון השם בקובץ
                            lines.set(i, USERNAME_PREFIX + userName);  // עדכון השם בַּשורה המתאימה
                            break;  // יציאה מהלולאה לאחר עדכון
                        }
                    }
                    //איחוד כל השורות ברשימה lines לתווך אחד ארוך כשכל שורה מופרדת ע"י אנטר וכותב זאת לקובץ הפנימי
                m_fileManager.writeInternalFile(TOTAL_USER_DATA,String.join("\n",lines),false);
                        if (userName.equals("בחור יקר")) {
                            Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "שם המשתמש נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {//במידה וקרתה שגיאה כתיבה לקובץ הקפץ הודעה
                        Toast.makeText(MainActivity.this, "אירעה שגיאה בקריאת הקובץ!", Toast.LENGTH_SHORT).show();
                }
                hideKeyboard(input);
                }
        });
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA); // קריאת הקובץ
                String userName = "בחור יקר";  // הגדר את המשתנה כ"בחור יקר"
                   //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                        if (line.startsWith(USERNAME_PREFIX)) {
                            // אם השורה מתחילה ב-"שם משתמש:", חותך את השם אחרי "שם משתמש:" ושומר אותו במשתנה
                            String currentUserName = line.substring(USERNAME_PREFIX.length()).trim(); //
                            if (!currentUserName.isEmpty()) {//אם המשתנה לא ריק
                                userName = currentUserName;//מגדיר את המשתנה בשם שנחתך
                                Toast.makeText(MainActivity.this, "שם המשתמש נשאר כפי שהיה.", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                            }
                            break;  // יציאה מהלולאה אחרי שמצאנו את השם
                        }
                    }
                    //איחוד כל השורות ברשימה lines לתווך אחד ארוך כשכל שורה מופרדת ע"י אנטר וכותב זאת לקובץ הפנימי
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
                    hideKeyboard(input); // הסתרת המקלדת

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בשמירת שם המשתמש!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // הוספת Listener במקרה של חזרה (Back) או ביטול הדיאלוג (למשל, לחיצה על כפתור חזור במכשיר)
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                try {
                    List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA); // קריאת הקובץ
                    String userName = "בחור יקר";  // הגדר את המשתנה כ"בחור יקר"
                    //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                        if (line.startsWith(USERNAME_PREFIX)) {
                            // אם השורה מתחילה ב-"שם משתמש:", חותך את השם אחרי "שם משתמש:" ושומר אותו במשתנה
                            String currentUserName = line.substring(USERNAME_PREFIX.length()).trim(); //
                            if (!currentUserName.isEmpty()) {//אם המשתנה לא ריק
                                userName = currentUserName;//מגדיר את המשתנה בשם שנחתך
                                Toast.makeText(MainActivity.this, "שם המשתמש נשאר כפי שהיה.", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                            }
                            break;  // יציאה מהלולאה אחרי שמצאנו את השם
                        }
                    }
                    //איחוד כל השורות ברשימה lines לתווך אחד ארוך כשכל שורה מופרדת ע"י אנטר וכותב זאת לקובץ הפנימי
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
                    hideKeyboard(input); // הסתרת המקלדת

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בשמירת שם המשתמש!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // הוספת Listener במקרה של חזרה (Back) או ביטול הדיאלוג (למשל, לחיצה על כפתור חזור במכשיר)
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA); // קריאת הקובץ
                    String userName = "בחור יקר";  // הגדר את המשתנה כ"בחור יקר"
                    //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                        if (line.startsWith(USERNAME_PREFIX)) {
                            // אם השורה מתחילה ב-"שם משתמש:", חותך את השם אחרי "שם משתמש:" ושומר אותו במשתנה
                            String currentUserName = line.substring(USERNAME_PREFIX.length()).trim(); //
                            if (!currentUserName.isEmpty()) {//אם המשתנה לא ריק
                                userName = currentUserName;//מגדיר את המשתנה בשם שנחתך
                                Toast.makeText(MainActivity.this, "שם המשתמש נשאר כפי שהיה.", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                            }
                            break;  // יציאה מהלולאה אחרי שמצאנו את השם
                        }
                    }
                    //איחוד כל הורות ברשימה lines לתווך אחד ארוך כשכל שורה מופרדת ע"י אנטר וכותב זאת לקובץ הפנימי
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
                    hideKeyboard(input); // הסתרת המקלדת

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בשמירת שם המשתמש!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        final AlertDialog dialog = builder.create();
        isDialogOpen = true;//הדיאלוג מוגדר כפתוח והתפריט לא יכול להפתח
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    // לחיצה על כפתור ה-menu תבצע את פעולה של כפתור ה"אישור"
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;  // מונע את הפעולה הרגילה של כפתור התפריט
                }
                return false;// אם זה לא כפתור Menu, תחזור להתנהגות הרגילה
            }
        });
        // הוספת טיימר שמנטרל את כפתור התפריט לחצי שנייה כאשר הדיאלוג נסגר
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isDialogOpen = false;//הדיאלוג מוגדר כסגור והתפריט יכול להפתח כרגיל
                    }
                }, 500); // 1000 מילישניות = 1 שניות
            }
        });
        // הבאת המוקד (פוקוס) לתוך ה-EditText
        input.requestFocus();
        dialog.show();
        showKeyboard(input);
    }//לא נבדק
    private void openSetTargetDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//בניית הדיאלוג
        builder.setTitle("אנא הגדר יעד דפים");//הגדרת כותרת לדיאלוג
        final EditText input = new EditText(this);//יצירת שדה קלט של טקסט
        input.setInputType(InputType.TYPE_CLASS_NUMBER);//הגדרת שדה הקלט שיקלוט רק מספרים
        builder.setView(input);//הכנסת  שדה הקלט לדיאלוג

        builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideKeyboard(input);//הסתרת המקלדת בלחיצה על אישור
                String target = input.getText().toString();//לקיחת המספר שהוכנס והפיכתו למשתנה סטרינגי
                if (!target.isEmpty()){//אם המשתנה לא ריק
                    try {
                        int targetInt = Integer.parseInt(target); // המרת המשתנה הסטרינגי למספרי
                        if (targetInt < m_pagesLearned) { // בדיקה אם היעד שהוזן קטן מהדפים שנלמדו
                            Toast.makeText(MainActivity.this, "היעד לא יכול להיות קטן מהדפים שנלמדו!", Toast.LENGTH_SHORT).show();
                            return; // יוצאים מהפונקציה אם היעד קטן מדי
                        }
                        m_pagesRemaining = Integer.parseInt(target);//המרת המשתנה הסטרינגי למשתנה המספרי
                        m_pagesRemaining -= m_pagesLearned;//הפחתת הדפים שנלמדו מהדפים שנותרו
                        Toast.makeText(MainActivity.this, "היעד הוגדר בהצלחה!", Toast.LENGTH_LONG).show();
                        updateDafDisplay();//עדכון התצוגה לאחר עדכון היעד
                        updateDafInTheFile();//עדכון הקובץ לאחר עדכון היעד
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "אנא הזן מספר תקין", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // הוספת כפתור Cancel
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDialogOpen = false;//הדיאלוג מוגדר כסגור והתפריט יכול להפתח כרגיל
                hideKeyboard(input);
            }});
        final AlertDialog dialog = builder.create();//הדיאלוג נוצר על פי ההגדרות שבוצעו עד כה.
        isDialogOpen = true;//הדיאלוג מוגדר כפתוח והתפריט לא יכול להפתח
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    // לחיצה על כפתור ה-menu תבצע את פעולה של כפתור ה"אישור"
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;  // מונע את הפעולה הרגילה של התפריט
                }
                return false;  // אם זה לא כפתור Menu, תחזור להתנהגות הרגילה
            }
        });
        // הוספת מאזין שיתבצע כאשר הדיאלוג נסגר
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isDialogOpen = false;//הדיאלוג מוגדר כסגור והתפריט יכול להפתח כרגיל
                    }
                }, 500); // 500 מילישניות = 0.5 שניות
            }
        });
        dialog.show();// הצגת הדיאלוג
        input.requestFocus();// הבאת המוקד (פוקוס) לתוך ה-EditText
        showKeyboard(input);//הצגת המקלדת
    }//נבדק
    private void updateDafInTheFile (){
        try {
            m_fileManager = new FileManager(this); //יצירת אובייקט לניהול קבצים
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);//קריאת הקובץ
            // לולאת חיפוש שם המשתמש בקובץ
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                if (line.startsWith("דפים שנלמדו:")) {
                    // אם השורה מתחילה ב-"שם משתמש:", עדכון השם בקובץ
                    lines.set(i, "דפים שנלמדו:" + m_pagesLearned);  // עדכון השם בַּשורה המתאימה
                    break;  // יציאה מהלולאה לאחר עדכון
                }
            }
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                if (line.startsWith("דפים שנשארו:")) {
                    // אם השורה מתחילה ב-"שם משתמש:", עדכון השם בקובץ
                    lines.set(i, "דפים שנשארו:" + m_pagesRemaining);  // עדכון השם בַּשורה המתאימה
                    break;  // יציאה מהלולאה לאחר עדכון
                }
            }
            //איחוד כל השורות ברשימה lines לתווך אחד ארוך כשכל שורה מופרדת ע"י אנטר וכותב זאת לקובץ הפנימי
            m_fileManager.writeInternalFile(TOTAL_USER_DATA,String.join("\n",lines),false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }//נבדק
    @SuppressLint("SetTextI18n")
    private void updateDafDisplay() {
        // עדכון התצוגה לאחר שמירת הנתונים בקובץ
        this.textViewNumberPagesLearned.setText("מספר דפים שנלמדו: " + this.m_pagesLearned);
        if (m_pagesRemaining == 0) {
            this.textViewNumberPagesRemaining.setText("מספר דפים שנותרו: לא הוגדר");
        } else {
            this.textViewNumberPagesRemaining.setText("מספר דפים שנותרו: " + this.m_pagesRemaining);
        }
    }//נבדק
    private void loadSelectedMasechetFromFile() { // פונקציה לקרוא את המסכתות מהקובץ ולהציגן ברשימה
        try {
            // ניקוי הרשימה לפני הטעינה
            selectedMasechetList.clear();
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            for (String line : lines) {
                // מחפשים את השורה שמתחילה ב-"מסכתות שנבחרו:"
                if (line.startsWith("מסכתות שנבחרו:")) {
                    // חיתוך המידע ללא רווח עד סוף השורה אחרי "מסכתות שנבחרו:"והפיכתו למשתנה שמכיל את כל רשימת המסכתות
                    String masechetData = line.substring("מסכתות שנבחרו:".length()).trim();
                    if (masechetData.endsWith(",")) { // אם יש פסיק בסוף, נוודא שאין אותו
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
            Toast.makeText(this, "שגיאה בקריאת קובץ המסכתות!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // קבלת הרשימה שנשלחה
            ArrayList<String> selectedMasechetListFromIntent = data.getStringArrayListExtra("selected_masechet_list");
            if (selectedMasechetListFromIntent != null && !selectedMasechetListFromIntent.contains(selectedMasechetListFromIntent)) {
                // הוספת כל המסכתות שהתקבלו לרשימה הקיימת
                for (String masechet : selectedMasechetListFromIntent) {
                    if (!selectedMasechetList.contains(masechet)) {
                        selectedMasechetList.add(masechet);  // הוסף רק אם המסכת לא קיימת כבר
                    }
                }

                // עדכון ה-ListView עם הרשימה המעודכנת
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedMasechetList);
                selectedmasechetListView.setAdapter(adapter);
            }
        }
    }
    public void onClickAddPointButton(View view) {//לחיצה על כפתור הוספה
        if (!(this.m_pagesRemaining == 0)) { //כל עוד הדפים שנשארו הם לא 0
            this.m_pagesLearned++;//הוספת מספר לדפים שנלמדו
            this.m_pagesRemaining--;//הסרת מספר מהדפים שנותרו
            updateDafDisplay();// עדכון התצוגה לאחר עדכון היעד
            updateDafInTheFile();//עדכון הקובץ לאחר עדכון היעד
            if (this.m_pagesRemaining == 0) {// בדיקה אם המשתמש השלים את כל הדפים
                startActivity(new Intent(this,CongratulationsActivity.class));
            }
        } else {//אם הדפים שנשארו הם 0 ונלחץ הכפתור הוספה
            openSetTargetDialog();
            Toast.makeText(this, "כדאי להגדיר יעד ומטרה!", Toast.LENGTH_LONG).show();
        }
    }//נבדק
    public void onClickRemovePointButton(View view) {//לחיצה על כפתור הסרה
        if (this.m_pagesLearned > 0) {//בדיקה שהדפים שנלמדו לא יורדים מתחת ל-0
            this.m_pagesRemaining++;//הוספת מספר לדפים שנותרו
            this.m_pagesLearned--;//הסרת מספר מהדפים שנלמדו
            updateDafDisplay();//עדכון התצוגה לאחר עדכון היעד
            updateDafInTheFile();//עדכון הקובץ לאחר עדכון היעד
            return;
        }
        Toast.makeText(this, "לא ניתן לרדת מתחת ל-0 דף!", Toast.LENGTH_SHORT).show();
    }//נבדק
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_select_maschet) {
            Intent intent = new Intent(this, Select_Masechet.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, Settings.class));
        }if (item.getItemId() == R.id.menu_About) {
            startActivity(new Intent(this, About.class));
        }if (item.getItemId() == R.id.menu_history) {
            startActivity(new Intent(this, History.class));
        }if (item.getItemId() == R.id.ask_User_Name) {
            askUserName();
        }else if (item.getItemId() == R.id.menu_set_target){
            openSetTargetDialog();
        }
        return true;
    } //נבדק
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }//נבדק
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && isDialogOpen) {
            //אם הוא מקבל true מה שאומר "האירוע טופל" אין צורך לעשות את מה שזה עושה בד"כ (שזה פתיחת התפריט)
            return true;  // חוסם את ההתנהגות הרגילה של כפתור ה-Menu
        }
        return super.dispatchKeyEvent(event); //עושה את הפעולות שבדר"כ הוא עושה (שזה פתיחת התפריט)
    }//נבדק
    private void showRemoveMasechetDialog(final String masechetToRemove, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("האם אתה בטוח שברצונך להסיר את המסכת: " + masechetToRemove + "?")
                .setCancelable(false)
                .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // הסרת המסכת מהרשימה
                        selectedMasechetList.remove(position);

                        // עדכון ה-ListView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, selectedMasechetList);
                        selectedmasechetListView.setAdapter(adapter);

                        // עדכון הקובץ על מנת להסיר את המסכת
                        removeMasechetFromFile(masechetToRemove);
                    }
                })
                .setNegativeButton("לא", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss(); // סגירת הדיאלוג בלי פעולה
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private void removeMasechetFromFile(String masechetToRemove) {
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("מסכתות שנבחרו:")) {
                    // חילוץ המסכתות מתוך השורה
                    String masechetData = lines.get(i).substring("מסכתות שנבחרו:".length()).trim();
                    String[] masechetArray = masechetData.split(",");

                    // יצירת רשימה חדשה של מסכתות תוך התחשבות במסכת להסרה
                    List<String> newMasechetList = new ArrayList<>();
                    for (String masechet : masechetArray) {
                        masechet = masechet.trim();
                        if (!masechet.equals(masechetToRemove) && !masechet.isEmpty()) {
                            newMasechetList.add(masechet); // הוספה אם זה לא המסכת להסרה
                        }
                    }

                    // בניית מחרוזת מעודכנת
                    String updatedMasechetData = String.join(", ", newMasechetList);

                    // הוספת פסיק בסוף אם הרשימה אינה ריקה
                    if (!updatedMasechetData.isEmpty()) {
                        updatedMasechetData += ",";
                    }

                    // עדכון השורה בקובץ
                    lines.set(i, "מסכתות שנבחרו: " + updatedMasechetData);

                    // כתיבת הנתונים המעודכנים לקובץ
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
                    break;
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בהסרת מסכת מהקובץ!", Toast.LENGTH_SHORT).show();
        }
    }
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

