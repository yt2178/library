package com.yt.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomAdapterListDaf extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> pages;//רשימה של שמות הדפים
    private final List<String> dafSelected;//רשימת הדפים שנבחרו


    public CustomAdapterListDaf(Context context, List<String> pages, List<String> dafList) {
        super(context, R.layout.daf_item, pages);
        this.context = context;
        this.pages = pages;
        this.dafSelected = dafList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.daf_item, parent, false);

        String currentDaf = pages.get(position);

        // נמצא את ה-TextView ברשימה
        TextView textView = rowView.findViewById(R.id.dafName);

        // הצגת שם הדף ב-TextView
        textView.setText(currentDaf);

        // אם הדף נמצא ברשימת הדפים שנבחרו, נבצע שינוי ברקע
        if (dafSelected.contains(currentDaf)) {
            rowView.setBackgroundResource(R.color.third_brown);  // צבע רקע לדפים שנבחרו
        }
        // מחזירים את ה-View עם העדכונים
        return rowView;
    }
}