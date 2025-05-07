package com.example.mybookshelf.LayoutManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybookshelf.DataBaseConnection;
import com.example.mybookshelf.MainActivity;
import com.example.mybookshelf.dataClass.Goal;

import java.util.List;

public class CustomGoalAdapter extends RecyclerView.Adapter<CustomGoalAdapter.GoalViewHolder> {
    private List<Goal> goals;
    private MainActivity context;
    private DataBaseConnection db;

    public CustomGoalAdapter(MainActivity context, List<Goal> goals, DataBaseConnection db) {
        this.context = context;
        this.goals = goals;
        this.db = db;
    }



    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout layout = new FrameLayout(context);
        return new GoalViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.layout.removeAllViews();
        holder.layout.addView(createGoalBox(goal));
    }

    public void addGoal(Goal goal) {
        goals.add(goal);
        notifyItemInserted(goals.size() - 1);
    }

    /**
     * Updates the goals list with new data
     *
     * @param newGoals The new list of goals to display
     */
    public void setGoals(List<Goal> newGoals) {
        this.goals.clear();
        if (newGoals != null) {
            this.goals.addAll(newGoals);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layout;
        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (FrameLayout) itemView;
        }
    }

    private View createGoalBox(Goal goal) {

        // Root container (FrameLayout for absolute positioning)
        FrameLayout rootLayout = new FrameLayout(context);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        // Card container (LinearLayout)
        LinearLayout cardLayout = new LinearLayout(context);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(16, 16, 16, 16); // Reduced padding

        // Card background
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(24f);
        background.setStroke(2, Color.LTGRAY);
        cardLayout.setBackground(background);

        // Cancel button (absolute positioning)
        Button cancelButton = new Button(context);
        cancelButton.setText("Cancel");
        cancelButton.setTextColor(Color.WHITE);
        cancelButton.setBackgroundColor(Color.parseColor("#E53935"));
        cancelButton.setAllCaps(false);
        cancelButton.setOnClickListener(v -> {
            // Remove the goal from the list
            goals.remove(goal);
            context.getUser().getGoalList().remove(goal);
            db.removeGoal(goal.getId(), context.getUser().getUid());
            notifyDataSetChanged();

        });

        // Position button outside the card at top-right
        FrameLayout.LayoutParams cancelParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        cancelParams.gravity = Gravity.END|Gravity.TOP;
        cancelParams.setMargins(0, 8, 8, 0);
        cancelButton.setLayoutParams(cancelParams);

        // Goal text
        TextView textView = new TextView(context);
        textView.setText("📘 Goal: " + goal.getFrequenzy());
        textView.setTextSize(16);
        textView.setTextColor(Color.DKGRAY);

        // Progress bar (fixed width)
        ProgressBar progressBar = new ProgressBar(context, null,
                android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Will expand to available width
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        progressBar.setMax(goal.getTarget());
        progressBar.setProgress(goal.getProgress());

        // Progress text
        TextView progressText = new TextView(context);
        progressText.setText("Progress: " + goal.getProgress() + " / " + goal.getTarget());
        progressText.setTextColor(Color.GRAY);
        progressText.setTextSize(14);

        // Add views to card
        cardLayout.addView(textView);
        cardLayout.addView(progressBar);
        cardLayout.addView(progressText);

        // Add views to root
        rootLayout.addView(cardLayout);
        rootLayout.addView(cancelButton);

        // Post-layout adjustment to match widths
        cardLayout.post(() -> {
            int buttonWidth = cancelButton.getWidth();
            int cardWidth = cardLayout.getWidth();

            // Set minimum width to ensure progress bar + button fit
            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (context instanceof Activity) {
                context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);


            }
            int minWidth = displayMetrics.widthPixels;
            cardLayout.setMinimumWidth(minWidth);
        });

        return rootLayout;
    }
}