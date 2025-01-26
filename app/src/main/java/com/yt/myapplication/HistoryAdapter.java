package com.yt.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;
    private Context context;

    public HistoryAdapter(Context context, List<HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        HistoryItem historyItem = historyList.get(position);
        holder.actionTextView.setText(historyItem.getAction());
        holder.timestampTextView.setText(historyItem.getTimestamp());

        holder.itemView.setOnLongClickListener(v -> {
            // יצירת AlertDialog לאישור מחיקה
            new AlertDialog.Builder(context)
                    .setTitle("מחיקת פריט")
                    .setMessage("האם אתה בטוח שברצונך למחוק את הפריט הזה?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        // הגנה לוודא שהמיקום תקין לפני שמחיקים את הפריט
                        if (position < historyList.size()) {
                            historyList.remove(position);
                            // עדכון ה-RecyclerView
                            notifyItemRemoved(position);

                            // עדכון המצב של ההיסטוריה הריקה
                            if (historyList.isEmpty() && context instanceof History) {
                                ((History) context).updateEmptyView();  // עדכון מצב הצגת ההיסטוריה הריקה
                            }

                            // שמירה של ההיסטוריה המעודכנת ב-SharedPreferences
                            saveHistory();
                        }
                    })
                    .setNegativeButton("לא", null)
                    .show();
            return true;  // מחזיר true כי לא נרצה שהאירוע יתפוס גם פעולה רגילה של לחיצה
        });
    }
        @Override
    public int getItemCount() {
        return historyList.size();
    }

    // ViewHolder המייצג את פריט ההיסטוריה
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView actionTextView;
        TextView timestampTextView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            actionTextView = itemView.findViewById(R.id.actionTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }

    // פונקציה לשמירת ההיסטוריה לאחר מחיקה
    private void saveHistory() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("history_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("history_key", convertListToString(historyList)); // המרת הרשימה למחרוזת לשמירה
        editor.apply(); // שמירה ב-SharedPreferences
    }


    // המרת רשימת ה- HistoryItems למחרוזת כדי לשמור אותה ב-SharedPreferences
    private String convertListToString(List<HistoryItem> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (HistoryItem item : list) {
            stringBuilder.append(item.getAction()).append(" - ").append(item.getTimestamp()).append("\n");
        }
        return stringBuilder.toString();
    }
}
