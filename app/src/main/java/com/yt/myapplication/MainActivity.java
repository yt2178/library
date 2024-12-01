package com.yt.myapplication;


import android.annotation.SuppressLint;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    private ListView masechetListView;
    private List<String> selectedMasechetList; // הוספנו את הרשימה כאן


    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        EdgeToEdge.enable(this);
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
        masechetListView = findViewById(R.id.masechetListView);
        selectedMasechetList = new ArrayList<>();

        m_fileManager = new FileManager(this);

        // קריאת המסכתות שנבחרו מהקובץ
        loadSelectedMasechetFromFile();

        // הצגת המסכתות ברשימה (ListView)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedMasechetList);
        masechetListView.setAdapter(adapter);


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
                this.m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, "דפים שנלמדו: 0", false);
                Toast.makeText(this, "!כניסה ראשונה, ברוכים הבאים", Toast.LENGTH_SHORT).show();
                return;
            }
            for (String line : lines) {
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
    protected void onPause() {
        super.onPause();

        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
            if (lines.isEmpty()) {
                lines.add(USERNAME_PREFIX + "בחור יקר");
                lines.add("דפים שנלמדו: " + m_pagesLearned);
            } else if (lines.size() > 1) {
                lines.set(1, "דפים שנלמדו: " + m_pagesLearned);
                lines.set(2,"דפים שנשארו: "+ m_pagesRemaining);
            } else {
                lines.add("דפים שנלמדו: " + m_pagesLearned);
            }
            m_fileManager.writeInternalFile(TOTAL_USER_DATA_NAME, String.join("\n", lines), false);
        } catch (IOException e) {
            Toast.makeText(this, "לא ניתן לשמור את נתוני המשתמש!", Toast.LENGTH_SHORT).show();
        }
    }

    // פונקציה לקרוא את המסכתות מהקובץ ולהציגן ברשימה
    private void loadSelectedMasechetFromFile() {
        try {
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
            Toast.makeText(this, "שגיאה בקריאת הקובץ!", Toast.LENGTH_SHORT).show();
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
             try {
                List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA_NAME);
                if (lines.isEmpty()) {
                    if (userName.isEmpty()) {
                        userName = "בחור יקר";  // אם לא הוזן שם, הגדר המשתנה כברירת מחדל
                    }
                    lines.add(USERNAME_PREFIX + userName); // שורה ראשונה: שם המשתמש
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


        builder.show();
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
                    Toast.makeText(this, "ברוך הבא, " + userName + "!", Toast.LENGTH_SHORT).show();
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
        builder.setTitle("אנא הגדר יעד דפים");

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
                TOTAL_PAGES = "0"; // אם לא הוזן יעד, הצג "לא הוגדר"
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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_select_maschet) {
            Intent intent = new Intent(this, Select_Masechet.class);
            startActivityForResult(intent, REQUEST_CODE); // כך נוכל לקבל תוצאה
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

