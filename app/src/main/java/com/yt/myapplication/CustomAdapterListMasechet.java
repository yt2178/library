package com.yt.myapplication;

import android.content.Context;
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

        // קבלת מספר הדפים של המסכת
        int totalPages = MasechetData.getPages(currentMasechet);
        // חישוב הדף האחרון בפורמט עברי
        String lastPage = pageCalculator.getHebrewDafFormat(totalPages);

        // הצגת שם המסכת והדף האחרון
        masechetName.setText(currentMasechet + " - דף אחרון: " + lastPage);

        // סימון מסכתות שכבר נבחרו
        if (selectedMasechetList.contains(currentMasechet)) {
            rowView.setBackgroundResource(android.R.color.darker_gray);
        }

        return rowView;
    }
}
