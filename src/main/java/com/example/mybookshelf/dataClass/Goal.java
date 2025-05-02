package com.example.mybookshelf.dataClass;

public class Goal {
    int id;
    boolean completed;
    int progress;
    int target;
    String bookName;
    String goal;
    String goalType;

    public Goal(int progress, int target, String goalType, String goal) {
        this.progress = progress;
        this.target = target;
        this.goal = goal;
        if (progress == 100) {
            completed = true;
        } else completed = false;
    }

    public Goal(int progress, String bookName, String goalType, String goal) {
        this.progress = progress;
        this.bookName = bookName;
        this.goal = goal;
        if (progress == 100) {
            completed = true;
        } else completed = false;
    }

    public int getId() {
        return id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getProgress() {
        return progress;
    }

    public int getTarget() {
        return target;
    }

    public String getGoal() {
        return goal;
    }

    public String getBookName() {
        return bookName;
    }

    public String getGoalType() {
        return goalType;
    }
}
