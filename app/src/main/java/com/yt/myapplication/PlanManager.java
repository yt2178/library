package com.yt.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlanManager {
    private static final String PLAN_PREFS = "learning_plan_prefs";
    private static final String PLANS_KEY = "plans_list";
    private SharedPreferences prefs;
    private Gson gson;

    public PlanManager(Context context) {
        this.prefs = context.getSharedPreferences(PLAN_PREFS, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public List<LearningPlan> getPlans() {
        String json = prefs.getString(PLANS_KEY, null);
        if (json == null) {
            return new ArrayList<>(); // Return an empty list if no plans exist
        }
        Type type = new TypeToken<ArrayList<LearningPlan>>() {}.getType();
        return gson.fromJson(json, type);
    }
// בתוך הקובץ PlanManager.java

    public LearningPlan getPlanById(String planId) {
        if (planId == null) return null;
        List<LearningPlan> plans = getPlans();
        for (LearningPlan plan : plans) {
            if (planId.equals(plan.id)) {
                return plan;
            }
        }
        return null; // לא נמצאה תוכנית עם ה-ID הזה
    }
    public void savePlans(List<LearningPlan> plans) {
        String json = gson.toJson(plans);
        prefs.edit().putString(PLANS_KEY, json).apply();
    }

    public void addPlan(LearningPlan newPlan) {
        List<LearningPlan> plans = getPlans();
        plans.add(newPlan);
        savePlans(plans);
    }

    public void deletePlan(String planId) {
        List<LearningPlan> plans = getPlans();
        LearningPlan planToRemove = null;
        for (LearningPlan plan : plans) {
            if (plan.id.equals(planId)) {
                planToRemove = plan;
                break;
            }
        }
        if (planToRemove != null) {
            plans.remove(planToRemove);
            savePlans(plans);
        }
    }
}