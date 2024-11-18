package com.yt.myapplication;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static int TOTAL_PAGES = 0;
    private static final String TOTAL_PAGES_FILE_NAME = "total_pages";
    private FileManager m_fileManager;
    private int m_pagesLearned;
    private TextView m_textViewPagesLearned;
    private TextView m_textViewPagesRemaining;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        this.m_textViewPagesLearned = (TextView) findViewById(R.id.textViewPagesLearned);
        this.m_textViewPagesRemaining = (TextView) findViewById(R.id.textViewPagesRemaining);
        this.m_fileManager = new FileManager(this);
        try {
            String readInternalFile = this.m_fileManager.readInternalFile(TOTAL_PAGES_FILE_NAME);
            this.m_pagesLearned = readInternalFile.length() == 0 ? 0 : Integer.parseInt(readInternalFile);
        } catch (IOException e) {
            Toast.makeText(this, "הקובץ לא נמצא!!!", Toast.LENGTH_SHORT).show();
            this.m_pagesLearned = 0;
        }
        updatePointsDisplay();
    }

    protected void onPause() {
        super.onPause();
        try {
            this.m_fileManager.writeInternalFile(TOTAL_PAGES_FILE_NAME, Integer.toString(this.m_pagesLearned), false);
        } catch (IOException e) {
            Log.e("IOError", "could not best score");
        }
    }

    public void onClickAddPointButton(View view) {
        if (this.m_pagesLearned < TOTAL_PAGES) {
            this.m_pagesLearned++;
            updatePointsDisplay();

            // בדיקה אם המשתמש השלים את כל הדפים
            if (this.m_pagesLearned == TOTAL_PAGES) {
                startActivity(new Intent(this, CongratulationsActivity.class));
            }
            return;
        }
        Toast.makeText(this, "סיימת את ה"+TOTAL_PAGES+ "דף שלקחת על עצמך, חזק וברוך", Toast.LENGTH_SHORT).show();
    }

    public void onClickRemovePointButton(View view) {
        if (this.m_pagesLearned > 0) {
            this.m_pagesLearned--;
            updatePointsDisplay();
            return;
        }
        Toast.makeText(this, "לא ניתן לרדת מתחת ל-0 דף!", Toast.LENGTH_SHORT).show();
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
      if (menuItem.getItemId() == R.id.menu_About) {
          startActivity(new Intent(this, About.class));
      } if (menuItem.getItemId() == R.id.menu_history) {
            startActivity(new Intent(this, History.class));
        }else if (menuItem.getItemId() == R.id.menu_set_target){
            openSetTargetDialog();
        }
        return true;
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
                String target = input.getText().toString();
                if (!target.isEmpty()){
                    try {
                        int targetPages = Integer.parseInt(target);

                            TOTAL_PAGES = targetPages;  // עדכון יעד הדפים
                            Toast.makeText(MainActivity.this, "היעד הוגדר בהצלחה!", Toast.LENGTH_LONG).show();
                            updatePointsDisplay();  // עדכון התצוגה לאחר עדכון היעד
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "אנא הזן מספר תקין", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton("ביטול", null);

        // הצגת הדיאלוג
        builder.show();
    }

    private void updatePointsDisplay() {
        this.m_textViewPagesLearned.setText(Integer.toString(this.m_pagesLearned));
        this.m_textViewPagesRemaining.setText(Integer.toString(TOTAL_PAGES - this.m_pagesLearned));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}