package com.example.mybookshelf.LayoutManager;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybookshelf.dataClass.Goal;

import java.util.List;

public class CustomGoalAdapter extends RecyclerView.Adapter<CustomGoalAdapter.GoalViewHolder> {
    private List<Goal> goals;
    private Context context;

    public CustomGoalAdapter(Context context, List<Goal> goals) {
        this.context = context;
        this.goals = goals;
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

    private View createGoalBox(Goal goal){
        FrameLayout rootLayout = new FrameLayout(context);

        // Create the Cancel button
        Button cancelButton = new Button(context);
        cancelButton.setText("Cancel");
        cancelButton.setBackgroundColor(Color.RED);
        cancelButton.setTextColor(Color.WHITE);

        // Set layout params for top-left placement
        FrameLayout.LayoutParams cancelParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        cancelParams.gravity = Gravity.TOP | Gravity.END;
        cancelParams.setMargins(20, 20, 0, 0); // Add some padding from edges
        cancelButton.setLayoutParams(cancelParams);

        // Add a TextView
        TextView textView = new TextView(context);
        textView.setText("Goal: " + goal.getGoalType() + "| Progress: " + goal.getProgress() + "/" +goal.getTarget());
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);

        // Center the TextView
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(textParams);

        // Optional: Set Cancel button action
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Cancel button action
            }
        });

        // Add views to root layout
        rootLayout.addView(cancelButton);
        rootLayout.addView(textView);

        return rootLayout;
    }
}
