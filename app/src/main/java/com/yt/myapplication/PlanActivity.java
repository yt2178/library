package com.yt.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // We will add this for the list of plans

import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PlanActivity extends AppCompatActivity {

    // Views for creating a new plan
    private LinearLayout layoutCreatePlan;
    private Spinner spinnerMasechet, spinnerStartDaf, spinnerEndDaf;
    private Button btnDatePicker, btnSave;
    private TextView tvSelectedDate;

    // We need a RecyclerView to display the list of plans
    private RecyclerView recyclerViewPlans;
    private PlanAdapter planAdapter; // A new adapter we will create

    private PlanManager planManager;
    private long endDateTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan); // You will need to update this layout

        planManager = new PlanManager(this);

        initViews();
        setupCreatePlanForm();
        loadAndDisplayPlans();
    }

    private void initViews() {
        // Find views for creating a plan
        layoutCreatePlan = findViewById(R.id.layout_create_plan);
        spinnerMasechet = findViewById(R.id.spinner_plan_masechet);
        spinnerStartDaf = findViewById(R.id.spinner_plan_start);
        spinnerEndDaf = findViewById(R.id.spinner_plan_end);
        btnDatePicker = findViewById(R.id.button_plan_datepicker);
        btnSave = findViewById(R.id.button_plan_save);
        tvSelectedDate = findViewById(R.id.textview_plan_end_date);

        // Find the RecyclerView for displaying plans
        recyclerViewPlans = findViewById(R.id.recycler_view_plans); // We will add this ID to the XML

     btnDatePicker.setOnClickListener(v -> showDatePickerDialog());
        btnSave.setOnClickListener(v -> savePlan());
    }

    private void setupCreatePlanForm() {
        // This part remains mostly the same
        List<String> masechetList = MasechetData.getMasechetList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, masechetList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMasechet.setAdapter(adapter);

        spinnerMasechet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDafSpinners((String) parent.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (!masechetList.isEmpty()) {
            updateDafSpinners(masechetList.get(0));
        }
    }

    private void loadAndDisplayPlans() {
        List<LearningPlan> plans = planManager.getPlans();

        recyclerViewPlans.setLayoutManager(new LinearLayoutManager(this));
        // We need to create a PlanAdapter class
        planAdapter = new PlanAdapter(plans, planId -> {
            // This is the delete listener
            showDeleteConfirmationDialog(planId);
        });
        recyclerViewPlans.setAdapter(planAdapter);
    }

    private void updateDafSpinners(String masechetName) {
        int totalPages = MasechetData.getPages(masechetName);
        if (totalPages > 0) {
            TalmudPageCalculator calculator = new TalmudPageCalculator();
            List<String> dafList = calculator.calculatePages(totalPages);
            ArrayAdapter<String> dafAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dafList);
            dafAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStartDaf.setAdapter(dafAdapter);
            spinnerEndDaf.setAdapter(dafAdapter);
        }
    }

    private void showDeleteConfirmationDialog(String planId) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת תוכנית")
                .setMessage("האם אתה בטוח שברצונך למחוק את התוכנית הזו?")
                .setPositiveButton("כן", (dialog, which) -> {
                    planManager.deletePlan(planId);
                    loadAndDisplayPlans(); // Refresh the list
                    Toast.makeText(this, "התוכנית נמחקה", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("לא", null)
                .show();
    }


    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // 1. קח את התאריך הלועזי שהמשתמש בחר
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    endDateTimestamp = selectedDate.getTimeInMillis(); // שמור את ה-timestamp

                    // 2. השתמש ב-zmanim כדי להמיר ולהציג תאריך עברי
                    JewishCalendar jc = new JewishCalendar(selectedDate);
                    HebrewDateFormatter hdf = new HebrewDateFormatter();
                    hdf.setHebrewFormat(true);
                    String hebrewDateString = hdf.format(jc);

                    // 3. הצג את התאריך העברי למשתמש!
                    tvSelectedDate.setText("תאריך שנבחר: " + hebrewDateString);

                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        // מונע בחירת תאריך בעבר
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void savePlan() {
        String masechet = spinnerMasechet.getSelectedItem().toString();
        String startDaf = spinnerStartDaf.getSelectedItem().toString();
        String endDaf = spinnerEndDaf.getSelectedItem().toString();

        if (endDateTimestamp == 0) {
            Toast.makeText(this, "יש לבחור תאריך סיום", Toast.LENGTH_SHORT).show();
            return;
        }

        TalmudPageCalculator calculator = new TalmudPageCalculator();
        if (calculator.getPageNumber(startDaf) > calculator.getPageNumber(endDaf)) {
            Toast.makeText(this, "דף הסיום חייב להיות אחרי דף ההתחלה", Toast.LENGTH_SHORT).show();
            return;
        }

        LearningPlan newPlan = new LearningPlan(masechet, startDaf, endDaf, endDateTimestamp);
        planManager.addPlan(newPlan);

        Toast.makeText(this, "התוכנית נוספה!", Toast.LENGTH_SHORT).show();

        // Refresh the list to show the new plan
        loadAndDisplayPlans();

        // Optional: clear the form fields after saving
        tvSelectedDate.setText("");
        endDateTimestamp = 0;
    }
}