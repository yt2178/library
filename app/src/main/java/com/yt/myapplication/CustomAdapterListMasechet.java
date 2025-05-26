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
        // נמצא את ה-TextView ברשימה
        TextView masechetName = rowView.findViewById(R.id.masechetName);
        String currentMasechet = masechetList.get(position);

        // הסרת נקודה בסוף השם אם קיימת וגם רווחים מיותרים
        if (currentMasechet.endsWith(".")) {
            currentMasechet = currentMasechet.substring(0, currentMasechet.length() - 1).trim(); // הסרת נקודה ורווחים
        } else {
            currentMasechet = currentMasechet.trim(); // הסרת רווחים מיותרים
        }

        // קבלת מספר הדפים של המסכת
        int totalPages = MasechetData.getPages(currentMasechet);
        // חישוב הדף האחרון בפורמט עברי
        String lastPage = pageCalculator.getHebrewDafFormat(totalPages);

        // הצגת שם המסכת והדף האחרון
        masechetName.setText(currentMasechet + " - דפים - " + lastPage);

        // סימון מסכתות שכבר נבחרו
        if (selectedMasechetList.contains(currentMasechet)) {
            rowView.setBackgroundResource(R.color.third_brown);
       }
        // מחזירים את ה-View עם העדכונים
        return rowView;
    }
}
