package com.yt.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<LearningPlan> plans;
    private OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDeleteClicked(String planId);
    }

    public PlanAdapter(List<LearningPlan> plans, OnDeleteListener listener) {
        this.plans = plans;
        this.deleteListener = listener;
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
        holder.bind(plan, deleteListener);
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvDetails, tvDeadline;
        Button btnDelete;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDetails = itemView.findViewById(R.id.item_plan_details);
            tvDeadline = itemView.findViewById(R.id.item_plan_deadline);
            btnDelete = itemView.findViewById(R.id.item_plan_delete_button);
        }

        public void bind(final LearningPlan plan, final OnDeleteListener listener) {
            tvDetails.setText("מסכת " + plan.masechetName + " מדף " + plan.startDaf + " עד " + plan.endDaf);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDeadline.setText("יעד: " + sdf.format(new Date(plan.endDateTimestamp)));

            btnDelete.setOnClickListener(v -> listener.onDeleteClicked(plan.id));
        }
    }
}