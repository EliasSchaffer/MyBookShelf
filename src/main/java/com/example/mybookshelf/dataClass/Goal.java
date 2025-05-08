package com.example.mybookshelf.dataClass;

public class Goal {
    int id;
    boolean completed;
    int progress;
    int target;
    String bookName;
    String goal;
    String frequenzy;
    boolean reminder;

    public Goal(int progress, int target, String goalType, String goal, boolean reminder) {
        this.progress = progress;
        this.target = target;
        this.goal = goal;
        this.frequenzy = goalType;
        this.reminder = reminder;
        if (progress == target) {
            completed = true;
        } else completed = false;
    }

    public Goal(int progress, int target, String bookName, String goalType, String goal, boolean reminder) {
        this.progress = progress;
        this.bookName = bookName;
        this.frequenzy = goalType;
        this.reminder=reminder;
        this.target = target;
        this.goal = goal;
        if (progress == target) {
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

    public String getFrequenzy() {
        return frequenzy;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", completed=" + completed +
                ", progress=" + progress +
                ", target=" + target +
                ", bookName='" + bookName + '\'' +
                ", goal='" + goal + '\'' +
                ", frequenzy='" + frequenzy + '\'' +
                ", reminder=" + reminder +
                '}';
    }
}
