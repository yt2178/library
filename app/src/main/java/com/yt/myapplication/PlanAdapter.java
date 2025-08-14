package com.yt.myapplication;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu; // חשוב - לייבא את PopupMenu
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private final List<LearningPlan> plans;
    private final PlanInteractionListener interactionListener;
    Map<String, Boolean> isDetailedViewMap = new HashMap<>();

    // In PlanAdapter.java

    public interface PlanInteractionListener {
        void onDeleteClicked(String planId);
        void onReminderChanged(LearningPlan plan, boolean isActive);
        void onEditClicked(LearningPlan plan);
        void onToggleDetailsClicked(LearningPlan plan); // <-- הוסף את המתודה הזו
    }

    public PlanAdapter(List<LearningPlan> plans, PlanInteractionListener listener) {
        this.plans = plans;
        this.interactionListener = listener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        LearningPlan plan = plans.get(position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isDetailedViewMap.putIfAbsent(plan.id, false);
        }
        holder.bind(plan, interactionListener, isDetailedViewMap);
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvDetails, tvPacing, tvDeadline;
        ImageButton menuButton; // כפתור התפריט החדש

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDetails = itemView.findViewById(R.id.item_plan_details);
            tvPacing = itemView.findViewById(R.id.item_plan_pacing);
            tvDeadline = itemView.findViewById(R.id.item_plan_deadline);
            menuButton = itemView.findViewById(R.id.item_plan_menu_button);
        }
        public void bind(final LearningPlan plan, final PlanInteractionListener listener, final Map<String, Boolean> viewStateMap) {
            tvDetails.setText("מסכת " + plan.masechetName + " מדף " + plan.startDaf + " עד " + plan.endDaf);
            Calendar deadlineCalendar = Calendar.getInstance();
            deadlineCalendar.setTimeInMillis(plan.endDateTimestamp);
            JewishCalendar jcDeadline = new JewishCalendar(deadlineCalendar);
            HebrewDateFormatter hdf = new HebrewDateFormatter();
            hdf.setHebrewFormat(true);
            tvDeadline.setText("יעד סיום: " + hdf.format(jcDeadline));
            PlanCalculator calculator = new PlanCalculator(plan);
            boolean isDetailed = viewStateMap.getOrDefault(plan.id, false); // ברירת מחדל פשוטה
            if (isDetailed) {
                tvPacing.setText(calculator.getPacingSummary());
            } else {
                tvPacing.setText(calculator.getSimplePacingSummary());
            }
            menuButton.setOnClickListener(v -> showPopupMenu(v, plan, listener, viewStateMap));
        }
        private void showPopupMenu(View view, final LearningPlan plan, final PlanInteractionListener listener, final Map<String, Boolean> viewStateMap) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.plan_item_menu, popup.getMenu());
            MenuItem toggleDetailsItem = popup.getMenu().findItem(R.id.menu_toggle_details);
            boolean isDetailed = viewStateMap.getOrDefault(plan.id, true);
            toggleDetailsItem.setTitle(isDetailed ? "הצג מידע פשוט" : "הצג מידע מפורט");
            MenuItem toggleReminderItem = popup.getMenu().findItem(R.id.menu_toggle_reminder);
            toggleReminderItem.setTitle(plan.isReminderActive ? "כבה תזכורת" : "הפעל תזכורת");
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_edit_plan) {
                    listener.onEditClicked(plan);
                    return true;
                } else if (itemId == R.id.menu_delete_plan) {
                    listener.onDeleteClicked(plan.id);
                    return true;
                } else if (itemId == R.id.menu_toggle_details) {
                    listener.onToggleDetailsClicked(plan);
                    return true;
                } else if (itemId == R.id.menu_toggle_reminder) {
                    listener.onReminderChanged(plan, !plan.isReminderActive);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
}