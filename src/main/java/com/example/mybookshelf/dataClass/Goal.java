package com.example.mybookshelf.dataClass;

public class Goal {
    int id;
    boolean completed;
    int progress;
    int target;

    public Goal(int id, int progress, int target) {
        this.id = id;
        this.progress = progress;
        this.target = target;
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
}
