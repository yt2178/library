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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TOTAL_USER_DATA = "user_data.shinantam";
    // הגדרה של קבוע
    private static final String USERNAME_PREFIX = "שם משתמש:";
    private String selectedMasechet = ""; // משתנה לשמור את שם המסכת הנבחרת
    private static final int REQUEST_CODE = 1;
    private FileManager m_fileManager; // אובייקט לניהול קבצים
    private int m_pagesLearned; // משתנה למעקב אחרי מספר הדפים שלמד המשתמש
    private int m_pagesRemaining; // משתנה למעקב אחרי מספר הדפים שנשאר למשתמש
    private TextView textViewNumberPagesLearned; // תצוגת מספר הדפים שלמד
    private TextView textViewNumberPagesRemaining;// תצוגת מספר הדפים שנותרו
    private boolean isDialogOpen = false;
    private ListView selectedmasechetListView;
    private List<String> selectedMasechetList; // הוספנו את הרשימה כאן
    private TalmudPageCalculator pageCalculator;
    private List<String> dafSelected = new ArrayList<>();
    private TextView emptyMasechetTextView;//TextView שמוצג אם ההיסטוריה ריקה

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
        selectedMasechetList = new ArrayList<>();
        pageCalculator = new TalmudPageCalculator();//יצירת אובייקט של המחלקה
        // הצגת המסכתות ברשימה (ListView)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedMasechetList);
        selectedmasechetListView.setAdapter(adapter);
        checkIfUserNameExists();//בדיקה אם קיים שם משתמש
        updateTotalDafDFromFile();//טעינת נתוני הדפים מהקובץ ושמירתם למשתנים
        updateTotalDafDisplay();//עדכון התצוגה מהמשתנים לתצוגה
        updateSelectedMasechetFromFile(); // טעינת המסכתות שנבחרו מהקובץ
        selectedmasechetListView.setFocusable(true);//פוקוס
        selectedmasechetListView.setFocusableInTouchMode(true);//פוקוס
        isDialogOpen = false;//הדיאלוג מוגדר כסגור והתפריט יכול להפתח כרגיל
        selectedmasechetListView.requestFocus(); // מבטיח שהרשימה תקבל פוקוס אחרי עדכון הנתונים
        // אתחול TextView לרשימת המסכתות ריקה
        emptyMasechetTextView = findViewById(R.id.emptyMasechetTextView);
        // אם הרשימה ריקה, הצג את ה-TextView
        updateEmptyView();
        //לחיצה קצרה על מסכת מהרשימה תציג את רשימת הדפים שלה
        selectedmasechetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedMasechet = selectedMasechetList.get(position);//שמירת שם המסכת שנבחרה למשתנה
                //קבלת מספר הדפים מהפעילות MasechetData ושמירה למשתנה
                int totalPages = new MasechetData().getPages(selectedMasechet);
               if (totalPages > 0) {//כל עוד הדפים של המסכת יותר מ-0
                   //קריאה לפונקציה  calculatePages מתוך הפונקציה  TalmudPageCalculator
                   //העברת מספר הדפים וקבלת תוצאה במשתנה pages של רשימה
                    List<String> pages = pageCalculator.calculatePages(totalPages);
                    // הצגת הדפים בתצוגה נוספת או בחלון חדש
                    showPages(pages);
                } else {//טיפול במקרה שבו המסכת לא נמצאה או אין לה דפים
                    Toast.makeText(MainActivity.this, "מסכת לא נמצאה", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //לחיצה ארוכה על מסכת מהרשימה תפעיל פונקציה
        selectedmasechetListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String masechetToRemove = selectedMasechetList.get(position); // המסכת שנבחרה נשמרת במשתנה
                showRemoveMasechetDialog(masechetToRemove, position); // שליחת המסכת לפונקציה והפעלת הפונקציה
                return true; // מנע את פעולתו הרגילה של הלחיצה
            }
        });
    }

    @Override
    protected void onResume() {//חזרה למצב פעיל לאקטיביטי
        super.onResume();
        updateEmptyView();
        updateTotalDafDFromFile();
        updateTotalDafDisplay();
        // הגדרת פוקוס על ה-ListView
        selectedmasechetListView.requestFocus();
        selectedmasechetListView.setFocusable(true);
        selectedmasechetListView.setFocusableInTouchMode(true);
    }
    @Override
    protected void onPause() {//יציאה ממצב פעיל ועובד ברקע
        super.onPause();
        updateTotalDafDisplay();
        isDialogOpen = false;//הדיאלוג מוגדר כסגור והתפריט יכול להפתח כרגיל
    }
    private void updateDafSelectedFromFile(String masechetName) {
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            for (String line : lines) {
                // מחפשים את השורה שמתחילה ב-"מסכתות שנבחרו:"
                if (line.startsWith("מסכתות שנבחרו:")) {
                    String masechetLine = line.substring("מסכתות שנבחרו:".length()).trim();
                    String[] masechetArray = masechetLine.split("\\|");
                    for (String masechetData : masechetArray) {
                        if (masechetData.startsWith(masechetName)) {
                            String DafString = masechetData.substring(masechetName.length()).trim();
                            String[] DafArray = DafString.split(",");
                            dafSelected.clear();  // נוודא שהרשימה מתאפסת לפני שמוסיפים דפים חדשים
                            for (String page : DafArray) {
                                dafSelected.add(page.trim());
                            }
                        }
                    }
                }
            }
        } catch(IOException e){
            Toast.makeText(this, "שגיאה בטעינת הדפים שנבחרו!", Toast.LENGTH_SHORT).show();
        }
    }
    private void showPages(List<String> pages){
            // יצירת ListView חדש לדפים
            ListView pagesListView = findViewById(R.id.pagesListView);
            // הצגת הרשימה והסתרת רשימת המסכתות
            pagesListView.setVisibility(View.VISIBLE);
            selectedmasechetListView.setVisibility(View.GONE);

            // יצירת CustomAdapterListDaf עם הדפים
             CustomAdapterListDaf adapter = new CustomAdapterListDaf(this, pages, dafSelected);
             pagesListView.setAdapter(adapter);
            // ביצוע עדכון הדפים שנלמדו מהקובץ
            updateDafSelectedFromFile(selectedMasechet);
        // נסה לבצע את הגלילה אחרי שהרשימה מעודכנת
        pagesListView.post(() -> scrollToLastSelectedPage(pagesListView, pages));
            // הגדרת מאזין ללחיצות על פריטים ברשימה
            pagesListView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedDaf = pages.get(position);
                // קריאה לפונקציה onClickAddDafButton
                onClickAddDafButton(view);// העברת את ה-View כאן
                // עדכון הקובץ - הוספת הדף הנבחר למסכת
                saveDafSelectedToFile(selectedMasechet, selectedDaf);
                // הוספת הדף הנבחר ל-`dafSelected`
                if (!dafSelected.contains(selectedDaf)) {
                    dafSelected.add(selectedDaf);  // הוסף את הדף לרשימה שנבחרו
                }
                // עדכון ה-Adapter
                adapter.notifyDataSetChanged(); // זה יגרום ל-ListView להתעדכן ולשנות את הצבע
                Toast.makeText(this, "נבחר דף: " + selectedDaf , Toast.LENGTH_SHORT).show();
            });
        // הגדרת מאזין ללחיצה ארוכה על דף מהרשימה
        pagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDaf = pages.get(position); // הדף שנבחר נשמר במשתנה
                showRemoveDafDialog(selectedDaf, position,pages); // שליחת הדף לפונקציה והפעלת הפונקציה
                return true; // מנע את פעולתו הרגילה של הלחיצה
            }
        });

    }
    private void scrollToLastSelectedPage(ListView pagesListView, List<String> pages) {
        // אם יש דף שנבחר, נמצא את המיקום שלו ברשימה
        if (!dafSelected.isEmpty()) {
            String lastSelectedPage = dafSelected.get(dafSelected.size() - 1);  // הדף האחרון שנבחר
            int position = pages.indexOf(lastSelectedPage);  // מצא את המיקום שלו ברשימה

            // אם מצאנו את הדף האחרון שנבחר, נבצע גלילה אוטומטית אליו
            if (position != -1) {
                // עיכוב קצר לפני הגלילה, על מנת לאפשר ל-ListView להתעדכן
                pagesListView.postDelayed(() -> {
                    // גלילה עד הדף האחרון, תוך הצגת הדף בראש הרשימה
                    pagesListView.smoothScrollToPositionFromTop(position, 0);
                }, 100); // עיכוב של 100ms
            }
        }
    }
    // פונקציה לעדכון מצב הצגת רשימת המסכתות הריקה
    void updateEmptyView() {
        if (selectedMasechetList.isEmpty()) {
            selectedmasechetListView.setVisibility(View.GONE);  // מחביא את ה-RecyclerView
            emptyMasechetTextView.setVisibility(View.VISIBLE);  // מציג את ה-TextView עם ההודעה
        } else {
            selectedmasechetListView.setVisibility(View.VISIBLE);  // מציג את ה-RecyclerView
            emptyMasechetTextView.setVisibility(View.GONE);  // מחביא את ה-TextView
        }
    }
    public void saveDafSelectedToFile(String masechetName, String saf) {
        // קריאה לקובץ ולחיפוש אחרי המסכת הנבחרת
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                // מחפשים את השורה שמתחילה ב-"מסכתות שנבחרו:"
                if (line.startsWith("מסכתות שנבחרו:")) {
                    // חילוץ המסכתות שנבחרו
                    String selectedMasechetLine = line.substring("מסכתות שנבחרו:".length()).trim();
                    String[] masechetArray = selectedMasechetLine.split("\\|");
                    // חיפוש אם המסכת כבר קיימת
                    for (int j = 0; j < masechetArray.length; j++) {
                        if (masechetArray[j].startsWith(masechetName)) { // אם המסכת כבר קיימת
                            // אם הדף לא קיים, נוסיף אותו
                            if (!masechetArray[j].contains(saf)) {
                                masechetArray[j] += "," + saf; // הוסף את הדף
                            }
                            lines.set(i, "מסכתות שנבחרו:" + String.join("|", masechetArray) + "|");
                            m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
                            // שמירה בהיסטוריה לאחר הוספת הדף
                            HistoryUtils.logAction(MainActivity.this, "נבחר דף: " + saf + " במסכת " + masechetName);
                            return;
                        }
                    }
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
                    return;
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בשמירת הדפים לקובץ!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed(){
        // אם רשימת הדפים מוצגת, נחזור לרשימת המסכתות
        ListView pagesListView = findViewById(R.id.pagesListView);
        if (pagesListView.getVisibility() == View.VISIBLE) {
            pagesListView.setVisibility(View.GONE);
            selectedmasechetListView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
    private void checkIfUserNameExists(){
        try {
            this.m_fileManager = new FileManager(this); // יצירת אובייקט לניהול קבצים
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);//קריאת הקובץ
             for (String line : lines) {
                if (line.startsWith(USERNAME_PREFIX)) {
                    String userName = line.substring(USERNAME_PREFIX.length()).trim(); // חתוך את "שם משתמש: " בלי לציין מספר קבוע,,והסר רווחים מיותרים
                    if (!userName.isEmpty()){
                        Toast.makeText(this, "ברוך הבא  " + userName + "!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            // אם אין שורה שמתחילה ב"שם משתמש:" נבקש שם משתמש
            askUserName();
        } catch (IOException e) {//אם הקובץ לא נמצא
            askUserName();
        }
    }//נבדק
    private void askUserName(){
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
                    //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
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
                    // שמירת פעולה בהיסטוריה עם שם המשתמש החדש
                    HistoryUtils.logAction(MainActivity.this, "הוזן שם משתמש חדש: " + userName);
                        if (userName.equals("בחור יקר")) {
                            Toast.makeText(MainActivity.this, "ניתן להגדיר שם משתמש בתפריט!", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "ברוך הבא " + userName +"!", Toast.LENGTH_SHORT).show();
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
                    // שמירת פעולה בהיסטוריה אם המשתמש בחר לא לשנות
                    HistoryUtils.logAction(MainActivity.this, "שם המשתמש הוגדר: " + userName);
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
                    // שמירת פעולה בהיסטוריה
                    HistoryUtils.logAction(MainActivity.this, "השם לא שונה, נשמר השם הקודם: " + userName);
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
                        if (targetInt == 0) { // בדיקה אם היעד שווה ל-0
                            Toast.makeText(MainActivity.this, "זה היעד שלך???", Toast.LENGTH_SHORT).show();
                            return; // יוצאים מהפונקציה אם היעד שווה ל-0
                        } else if (targetInt < m_pagesLearned) { // בדיקה אם היעד קטן מהדפים שנלמדו
                            Toast.makeText(MainActivity.this, "היעד לא יכול להיות קטן מהדפים שלמדת!", Toast.LENGTH_SHORT).show();
                            return; // יוצאים מהפונקציה אם היעד קטן מדי
                        }
                        m_pagesRemaining = Integer.parseInt(target);//המרת המשתנה הסטרינגי למשתנה המספרי
                        m_pagesRemaining -= m_pagesLearned;//הפחתת הדפים שנלמדו מהדפים שנותרו
                        Toast.makeText(MainActivity.this, "היעד הוגדר בהצלחה!", Toast.LENGTH_LONG).show();
                        updateTotalDafDisplay();//עדכון התצוגה לאחר עדכון היעד
                        saveTotalDafToFile();//עדכון הקובץ לאחר עדכון היעד
                        // שמירה בהיסטוריה לאחר הגדרת היעד
                        HistoryUtils.logAction(MainActivity.this, "הגדרת יעד חדש: " + targetInt + " דפים");
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
    private void saveTotalDafToFile(){
        try {
            m_fileManager = new FileManager(this); //יצירת אובייקט לניהול קבצים
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);//קריאת הקובץ
            //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);//מגדיר את השורה שנמצאה כמשתנה סטרינגי
                if (line.startsWith("דפים שנלמדו:")) {
                    // אם השורה מתחילה ב-"שם משתמש:", עדכון השם בקובץ
                    lines.set(i, "דפים שנלמדו:" + m_pagesLearned);  // עדכון השם בַּשורה המתאימה
                    break;  // יציאה מהלולאה לאחר עדכון
                }
            }
            //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
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
    private void updateTotalDafDisplay(){//עדכון מהמשתנים לתצוגה סך הדפים שנלמדו ונשארו בס"כ
        //עדכון התצוגה לאחר שמירת הנתונים בקובץ
        this.textViewNumberPagesLearned.setText("מספר דפים שנלמדו: " + this.m_pagesLearned);
        if (m_pagesRemaining == 0) {
            this.textViewNumberPagesRemaining.setText("מספר דפים שנותרו: לא הוגדר");
        } else {
            this.textViewNumberPagesRemaining.setText("מספר דפים שנותרו: " + this.m_pagesRemaining);
        }
    }//נבדק
    private void updateTotalDafDFromFile(){//עדכון מהקובץ למשתנים סך הדפים שנלמדו ונשארו בס"כ
        m_fileManager = new FileManager(this);// יצירת אובייקט FileManager
        // קריאת הנתונים מהקובץ ושמירתם במשתנים
        try {
            m_fileManager = new FileManager(this); //יצירת אובייקט לניהול קבצים
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);//קריאת הקובץ
            //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
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
            //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
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
    }//נבדק
    private void updateSelectedMasechetFromFile() {
        try { // ניקוי הרשימה לפני הטעינה
            selectedMasechetList.clear();
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            for (String line : lines) {
                // מחפשים את השורה שמתחילה ב-"מסכתות שנבחרו:"
                if (line.startsWith("מסכתות שנבחרו:")) {
                    // חיתוך המידע אחרי "מסכתות שנבחרו:"
                    String masechetData = line.substring("מסכתות שנבחרו:".length()).trim();
                    // אם אין מסכתות אחרי המילים "מסכתות שנבחרו:", הצג הודעה מתאימה
                    if (masechetData.isEmpty()) {
                        Toast.makeText(this, "לא נבחרו מסכתות", Toast.LENGTH_SHORT).show();
                        break; // יציאה מהלולאה אם אין מסכתות
                    }
                    // חיתוך המידע לפי פסיקים (|) -> כל מסכת נפרדת
                    String[] masechetArray = masechetData.split("\\|");
                    // הוספת כל המסכתות לרשימה תוך שמירה על שמותיהם בלבד (ללא דפים)
                    for (String masechet : masechetArray) {
                        //חיתוך כל מסכת לפי הדפים (נמחק את הדפים )
                        String masechetName = masechet.split(",")[0].trim();
                        // הוספת שם המסכת לרשימה אם הוא לא ריק ושהוא לא כבר ברשימה
                        if (!masechetName.isEmpty() && !selectedMasechetList.contains(masechetName)) {
                            selectedMasechetList.add(masechetName);
                        }
                    }
                    break;//סיום קריאת המידע
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
                        selectedMasechetList.add(masechet);//הוסף רק אם המסכת לא קיימת כבר
                    }
                }
                // עדכון ה-ListView עם הרשימה המעודכנת
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedMasechetList);
                selectedmasechetListView.setAdapter(adapter);
            }
        }
    }
    public void onClickAddDafButton(View view) {//לחיצה על כפתור הוספה
        if (!(this.m_pagesRemaining == 0)) { //כל עוד הדפים שנשארו הם לא 0
            this.m_pagesLearned++;//הוספת מספר לדפים שנלמדו
            this.m_pagesRemaining--;//הסרת מספר מהדפים שנותרו
            // שמירת הפעולה בהיסטוריה
            HistoryUtils.logAction(MainActivity.this, "הוספת דף");
            updateTotalDafDisplay();// עדכון התצוגה לאחר עדכון היעד
            saveTotalDafToFile();//עדכון הקובץ לאחר עדכון היעד
            if (this.m_pagesRemaining == 0) {// בדיקה אם המשתמש השלים את כל הדפים
                startActivity(new Intent(this,CongratulationsActivity.class));
            }
        } else {//אם הדפים שנשארו הם 0 ונלחץ הכפתור הוספה
            this.m_pagesLearned++;//הוספת מספר לדפים שנלמדו
            // שמירת הפעולה בהיסטוריה
            HistoryUtils.logAction(MainActivity.this, "הוספת דף");
            openSetTargetDialog();
            Toast.makeText(this, "כדאי להגדיר יעד ומטרה!", Toast.LENGTH_LONG).show();
        }
    }//נבדק
    public void onClickRemoveDafButton(View view) {//לחיצה על כפתור הסרה
        if (this.m_pagesLearned > 0) {//בדיקה שהדפים שנלמדו לא יורדים מתחת ל-0
            this.m_pagesRemaining++;//הוספת מספר לדפים שנותרו
            this.m_pagesLearned--;//הסרת מספר מהדפים שנלמדו
            HistoryUtils.logAction(MainActivity.this, "הסרת דף");
            updateTotalDafDisplay();//עדכון התצוגה לאחר עדכון היעד
            saveTotalDafToFile();//עדכון הקובץ לאחר עדכון היעד
            return;
        }
        Toast.makeText(this, "לא ניתן לרדת מתחת ל-0 דף!", Toast.LENGTH_SHORT).show();
    }//נבדק
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_select_maschet) {
            Intent intent = new Intent(this, Select_Masechet.class);
            startActivityForResult(intent, REQUEST_CODE);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, Settings.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }if (item.getItemId() == R.id.menu_About) {
            startActivity(new Intent(this, About.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }if (item.getItemId() == R.id.menu_history) {
            startActivity(new Intent(this, History.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
        builder.setMessage("האם אתה בטוח שברצונך להסיר את מסכת: " + masechetToRemove + "?")
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
                        Toast.makeText(MainActivity.this,   "מסכת "+ masechetToRemove +" הוסרה!", Toast.LENGTH_SHORT).show();
                        updateEmptyView();
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
            //לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("מסכתות שנבחרו:")) {
                    // חילוץ המסכתות מתוך השורה
                    String masechetData = lines.get(i).substring("מסכתות שנבחרו:".length()).trim();
                    String[] masechetArray = masechetData.split("\\|");

                    // יצירת רשימה חדשה של מסכתות תוך התחשבות במסכת להסרה
                    List<String> newMasechetList = new ArrayList<>();
                    for (String masechet : masechetArray) {
                        masechet = masechet.trim();
                        if (!masechet.equals(masechetToRemove) && !masechet.isEmpty()) {
                            newMasechetList.add(masechet); // הוספה אם זה לא המסכת להסרה
                        }
                    }

                    // בניית מחרוזת מעודכנת
                    String updatedMasechetData = String.join("|", newMasechetList);

                    // הוספת פסיק בסוף אם הרשימה אינה ריקה
                    if (!updatedMasechetData.isEmpty()) {
                        updatedMasechetData += "|";
                    }
                    HistoryUtils.logAction(MainActivity.this, "מסכת " + masechetToRemove + " הוסרה");
                    // עדכון השורה בקובץ
                    lines.set(i, "מסכתות שנבחרו:" + updatedMasechetData);

                    // כתיבת הנתונים המעודכנים לקובץ
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
                    break;
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בהסרת מסכת מהקובץ!", Toast.LENGTH_SHORT).show();
        }
    }
    private void showRemoveDafDialog(final String dafToRemove, final int position, final List<String> pages) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("האם אתה בטוח שברצונך להסיר את הדף: " + dafToRemove + "?")
                .setCancelable(false)
                .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ListView pagesListView = findViewById(R.id.pagesListView);  // חפש את ה-ListView מתוך ה-XML

                        // הסרת הדף מהרשימה
                        if (!pages.isEmpty() && position >= 0 && position < pages.size()) {
                            pages.remove(position);
                        } else {
                            // הוסף טיפול במצב שבו הרשימה ריקה או האינדקס לא תקין
                            Toast.makeText(MainActivity.this, "הרשימה ריקה או האינדקס לא תקין", Toast.LENGTH_SHORT).show();
                        }


                        // עדכון ה-ListView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, pages);
                        pagesListView.setAdapter(adapter);

                        // עדכון הקובץ על מנת להסיר את הדף
                        removeDafFromFile(dafToRemove);
                        Toast.makeText(MainActivity.this, "דף " + dafToRemove + " הוסר!", Toast.LENGTH_SHORT).show();
                        updateEmptyView();
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
    private void removeDafFromFile(String dafToRemove) {
        try {
            List<String> lines = m_fileManager.readFileLines(TOTAL_USER_DATA);
            // לולאה שעוברת על כל שורות ברשימת lines וכל שורה נשמרת במשתנה line לצורך עיבוד או בדיקה
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("דפים שנבחרו:")) {
                    // חילוץ הדפים מתוך השורה
                    String dafData = lines.get(i).substring("דפים שנבחרו:".length()).trim();
                    String[] dafArray = dafData.split("\\|");

                    // יצירת רשימה חדשה של דפים תוך התחשבות בדף להסרה
                    List<String> newDafList = new ArrayList<>();
                    for (String daf : dafArray) {
                        daf = daf.trim();
                        if (!daf.equals(dafToRemove) && !daf.isEmpty()) {
                            newDafList.add(daf); // הוספה אם זה לא הדף להסרה
                        }
                    }

                    // בניית מחרוזת מעודכנת
                    String updatedDafData = String.join("|", newDafList);

                    // הוספת פסיק בסוף אם הרשימה אינה ריקה
                    if (!updatedDafData.isEmpty()) {
                        updatedDafData += "|";
                    }

                    // עדכון השורה בקובץ
                    lines.set(i, "דפים שנבחרו:" + updatedDafData);

                    // כתיבת הנתונים המעודכנים לקובץ
                    m_fileManager.writeInternalFile(TOTAL_USER_DATA, String.join("\n", lines), false);
                    break;
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בהסרת דף מהקובץ!", Toast.LENGTH_SHORT).show();
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

