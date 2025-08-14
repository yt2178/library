package com.yt.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// הוא גם מבטיח (implements) שהוא יודע להגיב לפעולות שהמשתמש עושה על הרשימה,כמו לחיצה על "מחק" או "ערוך".
public class PlanActivity extends AppCompatActivity implements PlanAdapter.PlanInteractionListener {

    private Spinner spinnerMasechet, spinnerStartDaf, spinnerEndDaf; // התיבות הנגללות לבחירת מסכת ודפים.
    private boolean isUserInteraction = true;
    private Button btnDatePicker, btnSave; // הכפתורים לבחירת תאריך ולשמירה.
    private TextView tvSelectedDate; // תיבת הטקסט שמציגה את התאריך שנבחר.
    private RecyclerView recyclerViewPlans; // הרשימה הגדולה שמציגה את כל התוכניות.
    private PlanAdapter planAdapter; // ה"מנהל" של הרשימה, שאחראי לשים כל תוכנית במקום הנכון.
    private PlanManager planManager; // ה"מזכיר" שאחראי לשמור ולטעון את התוכניות מהזיכרון.
    private TextView tvFormTitle; // הכותרת של טופס ההוספה/עריכה.
    private String currentlyEditingPlanId = null; // פתק שעליו נכתוב את ה-ID של התוכנית שאנחנו עורכים כרגע. אם הפתק ריק, אנחנו מוסיפים תוכנית חדשה.
    private long endDateTimestamp = 0; // פתק שעליו נכתוב את תאריך הסיום שהמשתמש בחר, בתור מספר גדול מאוד (timestamp).
    private ReminderManager reminderManager; // המנהל החדש שלנו
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        // יוצרים את ה"מזכיר" שלנו (PlanManager) שיהיה מוכן לעבודה.
        planManager = new PlanManager(this);
        // פונקציות שמחברות בין המשתנים שלנו (המגירות) לרכיבים שב-layout
        spinnerMasechet = findViewById(R.id.spinner_plan_masechet);
        spinnerStartDaf = findViewById(R.id.spinner_plan_start);
        spinnerEndDaf = findViewById(R.id.spinner_plan_end);
        btnDatePicker = findViewById(R.id.button_plan_datepicker);
        btnSave = findViewById(R.id.button_plan_save);
        tvSelectedDate = findViewById(R.id.textview_plan_end_date);
        recyclerViewPlans = findViewById(R.id.recycler_view_plans);
        tvFormTitle = findViewById(R.id.textview_form_title);
        reminderManager = new ReminderManager(this);
        // "כשלחצו על כפתור התאריך, תפעיל את הפונקציה שמציגה את לוח השנה"
        btnDatePicker.setOnClickListener(v -> showDatePickerDialog());
        // "כשלחצו על כפתור השמירה, תפעיל את הפונקציה ששומרת את התוכנית".
        btnSave.setOnClickListener(v -> savePlan());
        // קוראים לפונקציה שמכינה את טופס ההוספה (ממלאת מסכתות וכו').
        setupCreatePlanForm();
        // קוראים לפונקציה שטוענת את כל התוכניות השמורות ומציגה אותן ברשימה.
        loadAndDisplayPlans();
        // בסוף המתודה onCreate, לפני הסוגר, הוסף קריאה לבקשת ההרשאה
        requestNotificationPermission();
    }
    // הוסף את כל המתודה הזו לקובץ PlanActivity.java
    private void requestNotificationPermission() {
        // רלוונטי רק לאנדרואיד 13 (API 33) ומעלה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // אם אין הרשאה, נציג למשתמש את הדיאלוג של המערכת
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    // הוסף גם את המתודה הזו כדי לטפל בתשובת המשתמש לבקשת ההרשאה
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "הרשאה להתראות ניתנה!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "לא ניתנה הרשאה להתראות, התזכורות לא יפעלו.", Toast.LENGTH_LONG).show();
            }
        }
    }
    // פונקציה שמכינה את טופס ההוספה לשימוש.
    private void setupCreatePlanForm() {
        // 1. מבקשים מ-MasechetData את רשימת כל המסכתות.
        List<String> masechetList = MasechetData.getMasechetList();
        // 2. יוצרים "מתאם" (Adapter) פשוט שיודע איך להציג רשימת מילים בתוך Spinner.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, masechetList);
        // 3. מגדירים לו איך ייראה כל פריט ברשימה הנגללת.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 4. נותנים את המתאם ל-Spinner של המסכתות כדי שיציג אותן.
        spinnerMasechet.setAdapter(adapter);
        // 5. מגדירים "מאזין" ל-Spinner של המסכתות.
        // "בכל פעם שהמשתמש בוחר מסכת אחרת, תפעיל את הקוד הזה".
        spinnerMasechet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUserInteraction) { // אם זה לא המשתמש, אל תעשה כלום
                    return;
                }
                // קוראים לפונקציה שמעדכנת את רשימות הדפים לפי המסכת שנבחרה.
                updateDafSpinners(parent.getItemAtPosition(position).toString(), null, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        // 6. מגדירים "מאזין" דומה ל-Spinner של דף ההתחלה.
        spinnerStartDaf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUserInteraction) { // אם זה לא המשתמש, אל תעשה כלום
                    return;
                }
                // "...תעדכן את רשימת הדפים האפשריים ב-Spinner של דף הסיום".
                updateEndDafSpinner(position, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (!masechetList.isEmpty()) {
            updateDafSpinners(masechetList.get(0), null, null);
        }
    }

    // פונקציה שמעדכנת את רשימת הדפים בשני ה-Spinners (התחלה וסיום).
    private void updateDafSpinners(String masechetName, String startDafToSelect, String endDafToSelect) {
        // 1. שואלים כמה עמודים יש במסכת שנבחרה.
        int totalPages = MasechetData.getPages(masechetName);
        if (totalPages > 0) {
            // 2. משתמשים במחשבון שלנו כדי לקבל רשימה של כל שמות העמודים (ב., ב:, ג. וכו').
            TalmudPageCalculator calculator = new TalmudPageCalculator();
            List<String> dafList = calculator.calculatePages(totalPages);
            // 3. יוצרים "מתאם" חדש עבור רשימת העמודים.
            ArrayAdapter<String> dafAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dafList);
            dafAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // 4. נותנים את המתאם הזה ל-Spinner של דף ההתחלה.
            spinnerStartDaf.setAdapter(dafAdapter);
            // 5. קובעים את מיקום דף ההתחלה. ברירת המחדל היא 0.
            int startPosition = 0;
            if (startDafToSelect != null) {
                int foundPosition = dafAdapter.getPosition(startDafToSelect);
                if (foundPosition >= 0) {
                    startPosition = foundPosition;
                }
            }
            spinnerStartDaf.setSelection(startPosition);
            // 6. קוראים לפונקציה שמעדכנת את ה-Spinner של דף הסיום,
            //    ומעבירים לה את המיקום המדויק שחישבנו.
            updateEndDafSpinner(startPosition, endDafToSelect);
        }
    }

    // פונקציה שמעדכנת רק את ה-Spinner של דף הסיום.
    private void updateEndDafSpinner(int startDafPosition, String endDafToSelect) {
        // 1. בודקים שוב מהי המסכת הנוכחית וכמה עמודים יש בה.
        String masechetName = spinnerMasechet.getSelectedItem().toString();
        int totalPages = MasechetData.getPages(masechetName);
        if (totalPages <= 0) return;
        // 2. מייצרים שוב את רשימת כל העמודים.
        TalmudPageCalculator calculator = new TalmudPageCalculator();
        List<String> allDafim = calculator.calculatePages(totalPages);
        // 3. בודקים אם מיקום דף ההתחלה שקיבלנו תקין
        if (startDafPosition != AdapterView.INVALID_POSITION && startDafPosition < allDafim.size()) {
            // 4. ...יוצרים רשימה חדשה שמתחילה מדף ההתחלה ועד סוף המסכת.
            List<String> endDafOptions = new ArrayList<>(allDafim.subList(startDafPosition, allDafim.size()));
            // 5. יוצרים מתאם חדש עם הרשימה המסוננת.
            ArrayAdapter<String> endDafAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, endDafOptions);
            endDafAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // 6. נותנים את המתאם החדש ל-Spinner של דף הסיום.
            spinnerEndDaf.setAdapter(endDafAdapter);
            // 7. אם קיבלנו דף סיום ספציפי שצריך לבחור (במצב עריכה)...
            if (endDafToSelect != null) {
                // ...מוצאים אותו ברשימה החדשה ובוחרים אותו.
                int endPosition = endDafAdapter.getPosition(endDafToSelect);
                if (endPosition >= 0) spinnerEndDaf.setSelection(endPosition);
            }
        }
    }
    // הוסף את המתודה הזו לקובץ PlanActivity.java
    private void showReminderSettingsDialog(LearningPlan plan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הגדרת תזכורת");

        // רשימת הימים לבחירה
        final String[] daysOfWeek = {"ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת"};
        // מערך בוליאני שיחזיק את הימים שנבחרו
        final boolean[] checkedDays = new boolean[7];
        // רשימה של קבועי ה-Calendar התואמים
        final int[] calendarDays = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};

        // אתחול הימים שנבחרו לפי מה ששמור בתוכנית
        for (int i = 0; i < calendarDays.length; i++) {
            if (plan.reminderDays.contains(calendarDays[i])) {
                checkedDays[i] = true;
            }
        }

        // הגדרת פריטי הבחירה מרובה (ימים)
        builder.setMultiChoiceItems(daysOfWeek, checkedDays, (dialog, which, isChecked) -> {
            // עדכון המערך checkedDays בכל פעם שהמשתמש מסמן/מבטל סימון
            checkedDays[which] = isChecked;
        });

        // כפתור לבחירת שעה
        builder.setNeutralButton("בחר שעה (" + String.format("%02d:%02d", plan.reminderHour, plan.reminderMinute) + ")", null);

        // כפתור אישור
        builder.setPositiveButton("שמור והפעל", (dialog, which) -> {
            // עדכון הימים שנבחרו בתוכנית
            plan.reminderDays.clear();
            for (int i = 0; i < checkedDays.length; i++) {
                if (checkedDays[i]) {
                    plan.reminderDays.add(calendarDays[i]);
                }
            }

            if (plan.reminderDays.isEmpty()) {
                Toast.makeText(this, "יש לבחור לפחות יום אחד", Toast.LENGTH_SHORT).show();
                return;
            }

            // הפעלת התזכורת עם ההגדרות החדשות
            plan.isReminderActive = true;
            onReminderChanged(plan, true);
        });

        // כפתור ביטול
        builder.setNegativeButton("ביטול", (dialog, which) -> {
            // אם המשתמש ביטל, נשאיר את המתג כבוי
            plan.isReminderActive = false;
            planAdapter.notifyDataSetChanged();
        });

        AlertDialog dialog = builder.create();

        // הוספת לוגיקה לכפתור בחירת השעה
        dialog.setOnShowListener(d -> {
            Button timeButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            timeButton.setOnClickListener(v -> {
                // יוצרים דיאלוג לבחירת שעה
                android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(this,
                        (view, hourOfDay, minute) -> {
                            // שומרים את השעה והדקה שהמשתמש בחר בתוכנית
                            plan.reminderHour = hourOfDay;
                            plan.reminderMinute = minute;
                            // מעדכנים את הטקסט על הכפתור
                            timeButton.setText("בחר שעה (" + String.format("%02d:%02d", hourOfDay, minute) + ")");
                        }, plan.reminderHour, plan.reminderMinute, true // true = פורמט 24 שעות
                );
                timePickerDialog.show();
            });
        });

        dialog.show();
    }
    // פונקציה שטוענת את התוכניות מהזיכרון ומציגה אותן.
    private void loadAndDisplayPlans() {
        // 1. מבקשים מה"מזכיר" (PlanManager) את רשימת כל התוכניות השמורות.
        List<LearningPlan> plans = planManager.getPlans();
        // 2. אומרים ל-RecyclerView איך לסדר את הפריטים (אחד מתחת לשני).
        recyclerViewPlans.setLayoutManager(new LinearLayoutManager(this));
        // 3. יוצרים את ה"מנהל" של הרשימה (PlanAdapter) ונותנים לו את רשימת התוכניות ואת ה-Activity עצמו
        //    כדי שהמנהל יוכל "לדבר" עם ה-Activity ולהודיע לו על לחיצות.
        planAdapter = new PlanAdapter(plans, this);
        // 4. מחברים את המנהל לרשימה הגרפית.
        recyclerViewPlans.setAdapter(planAdapter);
    }

    // פונקציה שמופעלת כשלוחצים על כפתור "שמור" או "עדכן".
    private void savePlan() {
        // 1. אוספים את כל המידע מהטופס.
        String masechet = spinnerMasechet.getSelectedItem().toString();
        String startDaf = spinnerStartDaf.getSelectedItem().toString();
        String endDaf = spinnerEndDaf.getSelectedItem().toString();
        // 2. בדיקה אם נבחר תאריך סיום.
        if (endDateTimestamp == 0) {
            Toast.makeText(this, "יש לבחור תאריך סיום", Toast.LENGTH_SHORT).show();
            return;// עוצרים את הפונקציה.
        }
        // 3. החלטה: האם אנחנו במצב עריכה או הוספה?
        // בודקים את ה"פתק" שהכנו. אם כתוב עליו משהו...
        if (currentlyEditingPlanId != null) {
            // --- מצב עדכון ---
            // א. מבקשים מהמזכיר את כל רשימת התוכניות.
            List<LearningPlan> plans = planManager.getPlans();
            // ב. עוברים על הרשימה ומוצאים את התוכנית עם ה-ID ששמור לנו בפתק.
            for (LearningPlan plan : plans) {
                if (plan.id.equals(currentlyEditingPlanId)) {
                    // ג. מעדכנים את הפרטים שלה עם המידע החדש מהטופס.
                    plan.masechetName = masechet;
                    plan.startDaf = startDaf;
                    plan.endDaf = endDaf;
                    plan.endDateTimestamp = endDateTimestamp;
                    break;// יוצאים מהלולאה כי מצאנו ועדכנו.
                }
            }
            // ד. שומרים את כל הרשימה המעודכנת בחזרה לזיכרון.
            planManager.savePlans(plans);
            Toast.makeText(this, "התוכנית עודכנה!", Toast.LENGTH_SHORT).show();
        } else {
            // --- מצב הוספה ---
            // א. יוצרים אובייקט "תוכנית לימוד" חדש לגמרי עם המידע מהטופס.
            LearningPlan newPlan = new LearningPlan(masechet, startDaf, endDaf, endDateTimestamp);
            // ב. מבקשים מהמזכיר להוסיף את התוכנית החדשה לרשימה.
            planManager.addPlan(newPlan);
            Toast.makeText(this, "התוכנית נוספה!", Toast.LENGTH_SHORT).show();
        }
        // 4. בסוף, לא משנה אם הוספנו או עדכנו, אנחנו מנקים את הטופס
        //    וטוענים מחדש את הרשימה כדי להציג את השינויים.
        resetForm();
        loadAndDisplayPlans();
    }

    // פונקציה שמנקה את הטופס ומחזירה אותו למצב התחלתי.
    private void resetForm() {
        // מוחקים את מה שכתוב בפתק של העריכה.
        currentlyEditingPlanId = null;
        // מחזירים את הטקסט של הכפתור ל"הוסף תוכנית".
        btnSave.setText("הוסף תוכנית");
        // מחזירים את הכותרת של הטופס למצב הוספה.
        tvFormTitle.setText("הוספת תוכנית חדשה");
        // מנקים את תיבת הטקסט של התאריך.
        tvSelectedDate.setText("לא נבחר תאריך");
        // מאפסים את משתנה התאריך.
        endDateTimestamp = 0;
        // מחזירים את ה-Spinner של המסכתות לפריט הראשון.
        spinnerMasechet.setSelection(0);
    }

    // פונקציה שמציגה את לוח השנה לבחירת תאריך.
    private void showDatePickerDialog() {
        // יוצרים לוח שנה רגיל.
        Calendar cal = Calendar.getInstance();
        // אם כבר יש לנו תאריך שמור (במצב עריכה), מכוונים את לוח השנה לתאריך הזה.
        if (endDateTimestamp > 0) {
            cal.setTimeInMillis(endDateTimestamp);
        }
        // יוצרים דיאלוג של בחירת תאריך.
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                // מה לעשות אחרי שהמשתמש בחר תאריך ולחץ "אישור":
                (view, year, month, dayOfMonth) -> {
                    // א. יוצרים לוח שנה חדש עם התאריך הלועזי שנבחר.
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    // ב. שומרים את התאריך בתור מספר גדול (timestamp).
                    endDateTimestamp = selectedDate.getTimeInMillis();
                    // ג. משתמשים בספריית zmanim כדי להמיר את התאריך לעברי.
                    JewishCalendar jc = new JewishCalendar(selectedDate);
                    HebrewDateFormatter hdf = new HebrewDateFormatter();
                    hdf.setHebrewFormat(true);
                    // ד. מציגים את התאריך העברי בתיבת הטקסט.
                    tvSelectedDate.setText("תאריך שנבחר: " + hdf.format(jc));
                },
                // נותנים לדיאלוג את התאריך הנוכחי כנקודת התחלה.
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        // לא מאפשרים לבחור תאריכים בעבר.
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        // מציגים את הדיאלוג של בחירת התאריך.
        datePickerDialog.show();
    }

    // פונקציה שמציגה חלון קופץ לשאול "אתה בטוח שאתה רוצה למחוק?".
    private void showDeleteConfirmationDialog(String planId) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת תוכנית")
                .setMessage("האם אתה בטוח שברצונך למחוק את התוכנית הזו?")
                // אם המשתמש לחץ "כן":
                .setPositiveButton("כן", (dialog, which) -> {
                    planManager.deletePlan(planId);// מבקשים מהמזכיר למחוק.
                    loadAndDisplayPlans();// טוענים מחדש את הרשימה המעודכנת.
                    Toast.makeText(this, "התוכנית נמחקה", Toast.LENGTH_SHORT).show();
                })
                // אם המשתמש לחץ "לא", לא עושים כלום.
                .setNegativeButton("לא", null)
                .show();
    }

    // --- מימוש הפונקציות שהבטחנו ב"חוזה" (implements) ---
    // מה לעשות כשהמשתמש לוחץ "מחק" ברשימה.
    @Override
    public void onDeleteClicked(String planId) {
        // פשוט קוראים לפונקציה שמציגה את חלון האישור.
        showDeleteConfirmationDialog(planId);
    }

    // מה לעשות כשהמשתמש משנה את המתג של התזכורת.
    // בתוך הקובץ PlanActivity.java

    @Override
    public void onReminderChanged(LearningPlan plan, boolean isActive) {
        // מקרה 1: המשתמש מפעיל את המתג (הוא היה כבוי ועכשיו דלוק)
        // המטרה כאן היא רק לפתוח את דיאלוג ההגדרות.
        if (isActive && !plan.isReminderActive) {
            showReminderSettingsDialog(plan);
            return; // יוצאים מהפונקציה כדי למנוע שמירה כפולה
        }

        // מקרה 2: המשתמש מכבה את המתג
        if (!isActive) {
            plan.isReminderActive = false;
            reminderManager.cancelReminder(plan);
        }
        // מקרה 3: הדיאלוג נסגר עם "שמור והפעל"
        // במקרה הזה, plan.isReminderActive כבר יהיה true (נקבע בדיאלוג),
        // ו-isActive גם הוא true.
        else {
            plan.isReminderActive = true;
            reminderManager.setReminder(plan);
        }

        // בסוף, אחרי כל פעולה, שומרים את המצב החדש של כל התוכניות
        List<LearningPlan> currentPlans = planManager.getPlans();
        planManager.savePlans(currentPlans);

        // ומרעננים את התצוגה
        if (planAdapter != null) {
            planAdapter.notifyDataSetChanged();
        }
    }
    // מה לעשות כשהמשתמש לוחץ על "הצג מידע פשוט/מפורט".
    @Override
    public void onToggleDetailsClicked(LearningPlan plan) {
        // 1. בודקים מה מצב התצוגה הנוכחי של התוכנית הזו.
        boolean currentDetailedState = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentDetailedState = planAdapter.isDetailedViewMap.getOrDefault(plan.id, true);
        }
        // 2. הופכים את המצב (אם היה מפורט -> פשוט, ולהיפך).
        planAdapter.isDetailedViewMap.put(plan.id, !currentDetailedState);
        // 3. מרעננים את כל הרשימה כדי שהשינוי יוצג.
        planAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditClicked(LearningPlan plan) {
        // ---- שלב 1: הכנה ----
        // הגדר את כל המידע שאינו תלוי ב-Spinners
        currentlyEditingPlanId = plan.id;
        btnSave.setText("עדכן תוכנית");
        tvFormTitle.setText("עריכת תוכנית");

        // עדכון התאריך
        endDateTimestamp = plan.endDateTimestamp;
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(endDateTimestamp);
        JewishCalendar jc = new JewishCalendar(selectedDate);
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);
        tvSelectedDate.setText("תאריך שנבחר: " + hdf.format(jc));


        // ---- שלב 2: עדכון ה-Spinners בצורה מבוקרת ----
        // הגדר את הדגל כדי למנוע מהמאזינים להפריע
        isUserInteraction = false;

        // א. עדכן את המסכת. הפעולה הזו לא תפעיל את המאזין בגלל הדגל.
        ArrayAdapter<String> masechetAdapter = (ArrayAdapter<String>) spinnerMasechet.getAdapter();
        int masechetPosition = masechetAdapter.getPosition(plan.masechetName);
        if (masechetPosition >= 0) {
            spinnerMasechet.setSelection(masechetPosition, false); // ה-false מונע אנימציה
        }

        // ב. בנה מחדש את רשימת דפי ההתחלה ובחר את הדף הנכון.
        TalmudPageCalculator calculator = new TalmudPageCalculator();
        int totalPages = MasechetData.getPages(plan.masechetName);
        List<String> dafList = calculator.calculatePages(totalPages);
        ArrayAdapter<String> dafAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dafList);
        dafAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartDaf.setAdapter(dafAdapter);

        int startPosition = dafAdapter.getPosition(plan.startDaf);
        if (startPosition >= 0) {
            spinnerStartDaf.setSelection(startPosition, false);
        }

        // ג. בנה מחדש את רשימת דפי הסיום (בהתבסס על דף ההתחלה שכבר קבענו)
        //    ובחר את הדף הנכון.
        if (startPosition >= 0) {
            List<String> endDafOptions = new ArrayList<>(dafList.subList(startPosition, dafList.size()));
            ArrayAdapter<String> endDafAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, endDafOptions);
            endDafAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEndDaf.setAdapter(endDafAdapter);

            int endPosition = endDafAdapter.getPosition(plan.endDaf);
            if (endPosition >= 0) {
                spinnerEndDaf.setSelection(endPosition, false);
            }
        }

        // ---- שלב 3: הפעל מחדש את המאזינים ----
        // השתמש ב-post כדי להבטיח שהדגל יחזור ל-true רק אחרי שכל עדכוני ה-UI הסתיימו.
        spinnerMasechet.post(() -> isUserInteraction = true);
    }
}