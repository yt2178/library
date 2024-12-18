package com.yt.myapplication;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TOTAL_USER_DATA_NAME = "user_data.shinantam";
    // הגדרה של קבוע
    private static final String USERNAME_PREFIX = "שם משתמש: ";
    private static final int REQUEST_CODE = 1;
    private static String TOTAL_PAGES = "0";// יעד הדפים שהמשתמש בחר
    private FileManager m_fileManager; // אובייקט לניהול קבצים
    private int m_pagesLearned; // משתנה למעקב אחרי מספר הדפים שלמד המשתמש
    private int m_pagesRemaining; // משתנה למעקב אחרי מספר הדפים שנשאר למשתמש
    private TextView textViewNumberPagesLearned; // תצוגת מספר הדפים שלמד
    private TextView textViewNumberPagesRemaining;// תצוגת מספר הדפים שנותרו
    private boolean isDialogOpen = false;
    private ListView selectedmasechetListView;
    private List<String> selectedMasechetList; // הוספנו את הרשימה כאן


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
        checkIfUserNameExists();
        updatePointsDisplay();

        // אתחול ה-ListView והרשימה
        selectedmasechetListView = findViewById(R.id.masechetListView);
        selectedmasechetListView.setFocusable(true);
        selectedmasechetListView.setFocusableInTouchMode(true);
        selectedmasechetListView.requestFocus();

        selectedMasechetList = new ArrayList<>();

        m_fileManager = new FileManager(this);

        isDialogOpen = false;

        // הצגת המסכתות ברשימה (ListView)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedMasechetList);
        selectedmasechetListView.setAdapter(adapter);
        selectedmasechetListView.requestFocus(); // מבטיח שהרשימה תקבל פוקוס אחרי עדכון הנתונים

        selectedmasechetListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String masechetToRemove = selectedMasechetList.get(position); // המסכת שנבחרה
                showRemoveMasechetDialog(masechetToRemove, position); // הצגת דיאלוג לאישור
                return true; // מנע את פעולתו הרגילה של הלחיצה
            }
        });

        this.textViewNumberPagesLearned = (TextView) findViewById(R.id.textViewNumberPagesLearned);
        this.textViewNumberPagesRemaining = (TextView) findViewById(R.id.textViewNumberPagesRemaining);
        this.m_fileManager = new FileManager(this); // יצירת אובייקט לניהול קבצים
        try {
        File file = new File(getFilesDir(), TOTAL_USER_DATA_NAME);
        if (!file.exists()) {
            // אם הקובץ לא קיים, יצור אותו עם נתוני ברירת מחדל
            m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, "דפים שנלמדו: 0", false);
            Toast.makeText(this, "כניסה ראשונה, ברוכים הבאים!", Toast.LENGTH_SHORT).show();
            return; // צא מהפונקציה כי אין צורך להמשיך
        }
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            if (lines.isEmpty()) {//אם הרשימה ריקה
                lines.add(USERNAME_PREFIX + "בחור יקר");
                lines.add("דפים שנלמדו: " + m_pagesLearned);
                lines.add("דפים שנשארו: "+ m_pagesRemaining);
                lines.add("מסכתות שנבחרו: ");
                this.m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, "דפים שנלמדו: 0", false);
                Toast.makeText(this, "!כניסה ראשונה, ברוכים הבאים", Toast.LENGTH_SHORT).show();
                return;
            }
            for (String line : lines) {
                // קריאת המסכתות שנבחרו מהקובץ
                loadSelectedMasechetFromFile();
                if (line.startsWith("דפים שנלמדו: ")) {
                    // חותך את "דפים שנלמדו:" בלי לציין מספר קבוע ושומר אות במשתנה
                    String learnedPages = line.substring("דפים שנלמדו: ".length());
                    this.m_pagesLearned = Integer.parseInt(learnedPages);
                    break;
                }
            }

        } catch (IOException e) {
            Toast.makeText(this, "שגיאה! לא הצליח להפוך את הקובץ לרשימה!", Toast.LENGTH_SHORT).show();
        }
        updatePointsDisplay();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // הגדרת פוקוס על ה-ListView
        selectedmasechetListView.requestFocus();
        selectedmasechetListView.setFocusable(true);
        selectedmasechetListView.setFocusableInTouchMode(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isDialogOpen = false;
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            if (lines.isEmpty()) {
                lines.add(USERNAME_PREFIX + "בחור יקר");
                lines.add("דפים שנלמדו: " + "0");
                lines.add("דפים שנשארו: "+ "0");
                lines.add("מסכתות שנבחרו: ");
            } else if (lines.size() > 2) {
                lines.set(0,USERNAME_PREFIX + "בחור יקר");
                lines.set(1, "דפים שנלמדו: " + m_pagesLearned);
                if (m_pagesRemaining > 0) {
                    lines.set(2, "דפים שנשארו: " + m_pagesRemaining);
                } else {
                    lines.set(2, "דפים שנשארו: 0");
                }

            } else {
                lines.add("דפים שנלמדו: " + m_pagesLearned);
                lines.add("דפים שנשארו: "+ m_pagesRemaining);
                }

            m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, String.join("\n", lines), false);
        } catch (IOException e) {
            Toast.makeText(this, "לא ניתן לשמור את נתוני המשתמש!", Toast.LENGTH_SHORT).show();
        }
    }

    // פונקציה לקרוא את המסכתות מהקובץ ולהציגן ברשימה
    private void loadSelectedMasechetFromFile() {
        try {
            // ניקוי הרשימה לפני הטעינה
            selectedMasechetList.clear();

            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);

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

    private void askUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הזן את שמך");

        final EditText input = new EditText(this);//יצירת שדה קלט של טקסט

        try {
            // קריאה לשם המשתמש הקיים
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            String existingUserName = "בחור יקר"; // ברירת מחדל
            if (!lines.isEmpty()) {
                for (String line : lines) {
                    if (line.startsWith(USERNAME_PREFIX)) {
                        existingUserName = line.substring(USERNAME_PREFIX.length()); // חותך את החלק שמכיל את הקידומת "USERNAME_PREFIX"
                        break;
                    }
                }
            }

            input.setHint(existingUserName); // הצגת שם המשתמש בשדה הקלט

        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "אירעה שגיאה בקריאת שם המשתמש", Toast.LENGTH_SHORT).show();
        }

        builder.setView(input);//הכנסת  שדה הקלט לדיאלוג

        builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = input.getText().toString();
             try {
                List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
                if (lines.isEmpty()) {
                    if (userName.isEmpty()) {
                        userName = "בחור יקר";  // אם לא הוזן שם, הגדר המשתנה כברירת מחדל
                    }
                    lines.add(USERNAME_PREFIX + userName); // שורה ראשונה: שם המשתמש
                    lines.add("דפים שנלמדו: " + m_pagesLearned);
                    lines.add("דפים שנשארו: "+ m_pagesRemaining);
                    lines.add("מסכתות שנבחרו: ");

                }else{
                    lines.set(0, USERNAME_PREFIX + userName);

                }
                m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME,String.join("\n",lines),false);
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
                        lines.add(USERNAME_PREFIX + userName);
                        lines.add("דפים שנלמדו: " + m_pagesLearned);
                        lines.add("דפים שנשארו: "+ m_pagesRemaining);
                        lines.add("מסכתות שנבחרו: ");

                    }else{//אם השורות לא ריקות
                        if (lines.size() > 1) {
                        lines.set(1, String.valueOf(m_pagesLearned));

                        }
                    }
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME,String.join("\n",lines),false);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בהגדרת שם משתמש ברירת מחדל כשנלחץ ביטול", Toast.LENGTH_SHORT).show();
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
                    }else {
                        // אם השורות לא ריקות, אנחנו לא רוצים לשנות את השם אם יש שם קודם.
                        if (lines.get(0).startsWith(USERNAME_PREFIX)) {
                            return;  // אם השם כבר קיים בשורה הראשונה, אל תשנה אותו
                        }
                    }
                        lines.set(0, USERNAME_PREFIX + userName); // הגדרת שם ברירת מחדל בשורה הראשונה
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בהגדרת שם משתמש ברירת מחדל כשבוצעה יציאה מהדיאלוג", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // הוספת Listener במקרה של חזרה (Back) או ביטול הדיאלוג (למשל, לחיצה על כפתור חזור במכשיר)
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String userName = "בחור יקר";  // הגדרת שם ברירת מחדל במקרה של ביטול הדיאלוג
                try {
                    List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
                    if (lines.isEmpty()) {
                        lines.add(USERNAME_PREFIX + userName); // אם כל השורות ריקות
                    } else {
                        // אם השורות לא ריקות, אנחנו לא רוצים לשנות את השם אם יש שם קודם.
                        if (lines.get(0).startsWith(USERNAME_PREFIX)) {
                            return;  // אם השם כבר קיים בשורה הראשונה, אל תשנה אותו
                        }
                        lines.set(0, USERNAME_PREFIX + userName); // הגדרת שם ברירת מחדל בשורה הראשונה
                    }
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, String.join("\n", lines), false);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "אירעה שגיאה בהגדרת שם משתמש ברירת מחדל כשבוצעה יציאה מהדיאלוג", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final AlertDialog dialog = builder.create();
        // במהלך יצירת הדיאלוג, נסמן אותו כפתוח
        isDialogOpen = true;
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
                        isDialogOpen = false;
                    }
                }, 500); // 1000 מילישניות = 1 שניות
            }
        });


        dialog.show();
        // הבאת המוקד (פוקוס) לתוך ה-EditText
        input.requestFocus();
        showKeyboard(input);
    }
    private void openSetTargetDialog(){
        // יצירת דיאלוג
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("אנא הגדר יעד דפים");// הגדרת כותרת לדיאלוג.

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
                    updatePointsDisplay();  // עדכון התצוגה לאחר עדכון היעד
                }
            }
        });
        // הוספת כפתור Cancel
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDialogOpen = false;
                hideKeyboard(input);
            }});

        final AlertDialog dialog = builder.create();
        // במהלך יצירת הדיאלוג, נסמן אותו כפתוח
        isDialogOpen = true;
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
                        isDialogOpen = false;
                    }
                }, 500); // 1000 מילישניות = 1 שניות
            }
        });



        // הצגת הדיאלוג
        dialog.show();

        // הבאת המוקד (פוקוס) לתוך ה-EditText
        input.requestFocus();
        showKeyboard(input);
    }
    private void checkIfUserNameExists() {
        try {
            this.m_fileManager = new FileManager(this); // יצירת אובייקט לניהול קבצים
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            if (lines.isEmpty()) {//אם הרשימה ריקה לבקש שם משתמש
                askUserName();
                return;
            }
            for (String line : lines) {
                if (line.startsWith(USERNAME_PREFIX)) {
                    String userName = line.substring(USERNAME_PREFIX.length()).trim(); // חתוך את "שם משתמש: " בלי לציין מספר קבוע,,והסר רווחים מיותרים
                    Toast.makeText(this, "ברוך הבא  " + userName + "!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // אם אין שם משתמש, נבקש שם חדש
            askUserName();
        } catch (IOException e) {
            askUserName();
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
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
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
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, String.join("\n", lines), false);
                    break;
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בהסרת מסכת מהקובץ!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && isDialogOpen) {
            // מונעים את פתיחת התפריט אם הדיאלוג פתוח
            return true;  // חוסם את ההתנהגות הרגילה של כפתור ה-Menu
        }
        return super.dispatchKeyEvent(event);
    }
    public void onClickAddPointButton(View view) {
        if (TOTAL_PAGES.equals("0")) {
            // הוספת נקודה
            this.m_pagesLearned++;
            updatePointsDisplay();
        }else{
            int totalPages = Integer.parseInt(TOTAL_PAGES);
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
    @SuppressLint("SetTextI18n")
    private void updatePointsDisplay() {
        this.textViewNumberPagesLearned = (TextView) findViewById(R.id.textViewNumberPagesLearned);
        this.textViewNumberPagesRemaining = (TextView) findViewById(R.id.textViewNumberPagesRemaining);
        this.textViewNumberPagesLearned.setText("מספר דפים שנלמדו: " + this.m_pagesLearned);
        if (TOTAL_PAGES.equals("0")) {
            this.textViewNumberPagesRemaining.setText("מספר דפים שנותרו: לא הוגדר");
        } else {
            this.m_pagesRemaining = Integer.parseInt(TOTAL_PAGES) - this.m_pagesLearned;
            this.textViewNumberPagesRemaining.setText("מספר דפים שנותרו: " + this.m_pagesRemaining);

        }
    }
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

