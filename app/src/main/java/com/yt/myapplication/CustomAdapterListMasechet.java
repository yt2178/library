package com.yt.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomAdapterListMasechet extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> masechetList;
    private final List<String> selectedMasechetList;
    private final TalmudPageCalculator pageCalculator;

    public CustomAdapterListMasechet(Context context, List<String> masechetList, List<String> selectedMasechetList) {
        super(context, R.layout.masechet_item, masechetList);
        this.context = context;
        this.masechetList = masechetList;
        this.selectedMasechetList = selectedMasechetList;
        this.pageCalculator = new TalmudPageCalculator();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.masechet_item, parent, false);

        TextView masechetName = rowView.findViewById(R.id.masechetName);
        String currentMasechet = masechetList.get(position);

        // הסרת נקודה בסוף השם אם קיימת וגם רווחים מיותרים
        if (currentMasechet.endsWith(".")) {
            currentMasechet = currentMasechet.substring(0, currentMasechet.length() - 1).trim(); // הסרת נקודה ורווחים
        } else {
            currentMasechet = currentMasechet.trim(); // הסרת רווחים מיותרים
        }

        // הדפסת שם המסכת לאחר הסרת נקודה ורווחים
        Log.d("Masechet after trim", currentMasechet);

        // הצגת תוכן הרשימה של מסכתות נבחרות
        Log.d("Selected Masechet List", selectedMasechetList.toString());

        // קבלת מספר הדפים של המסכת
        int totalPages = MasechetData.getPages(currentMasechet);
        // חישוב הדף האחרון בפורמט עברי
        String lastPage = pageCalculator.getHebrewDafFormat(totalPages);

        // הצגת שם המסכת והדף האחרון
        masechetName.setText(currentMasechet + " - מספר הדפים: " + lastPage);

        // סימון מסכתות שכבר נבחרו
        if (selectedMasechetList.contains(currentMasechet)) {
            Log.d("Selected Masechet", "Found: " + currentMasechet); // הוסף כאן הדפסה כדי לראות אם המסכת נמצאת ברשימה
            rowView.setBackgroundResource(R.color.third_brown);
        } else {
            Log.d("Selected Masechet", "Not found: " + currentMasechet); // הדפסת "לא נמצא" אם המסכת לא נמצאת ברשימה
        }

        return rowView;
    }
}
