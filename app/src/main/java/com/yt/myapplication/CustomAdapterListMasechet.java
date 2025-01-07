package com.yt.myapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class CustomAdapterListMasechet extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;
    private final List<String> selectedMasechetList; // רשימה של המסכתות שנבחרו

    public CustomAdapterListMasechet(Context context, List<String> values, List<String> selectedMasechetList) {
        super(context, android.R.layout.simple_list_item_1, values);
        this.context = context;
        this.values = values;
        this.selectedMasechetList = selectedMasechetList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // קבלת ה-View לכל פריט ברשימה
        View view = super.getView(position, convertView, parent);

        // קבלת המסכת שנמצאת במיקום הנוכחי
        String masechet = values.get(position);

        // בדיקה אם המסכת נבחרה
        if (selectedMasechetList.contains(masechet)) {
            // אם המסכת נבחרה, נצבע אותה בצבע אחר
            view.setBackgroundColor(context.getResources().getColor(R.color.third_brown));
        } else {
            // אם לא נבחרה, היא תהיה בצבע ברירת המחדל
            view.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }

        return view;
    }
}
