package com.example.mybookshelf.dataClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Goal {
    int id;
    boolean completed;
    int progress;
    int target;
    String bookName;
    String goal;
    String frequenzy;
    boolean reminder;
    LocalDateTime deadline;

    public Goal(int id, int progress, int target, String goalType, String goal, LocalDateTime deadline) {
        this.id = id;
        this.progress = progress;
        this.target = target;
        this.goal = goal;
        this.deadline = deadline;
        this.frequenzy = goalType;
        if (progress == target) {
            completed = true;
        } else completed = false;
    }

    public Goal(int id, int progress, int target, String goalType, String goal) {
        this.id = id;
        this.progress = progress;
        this.target = target;
        this.goal = goal;
        this.frequenzy = goalType;
        calculateDeadline();

        if (progress == target) {
            completed = true;
        } else completed = false;
    }

    public Goal(int id, int progress, int target, String bookName, String goalType, String goal) {
        this.progress = progress;
        this.bookName = bookName;
        this.frequenzy = goalType;
        this.target = target;
        this.goal = goal;
        this.deadline = LocalDateTime.now();

        calculateDeadline();
        if (progress == target) {
            completed = true;
        } else completed = false;
    }

    private void calculateDeadline() {
        deadline = LocalDateTime.now();

        switch (frequenzy.toLowerCase()) {
            case "daily":
                this.deadline = deadline.plusDays(1);
                break;
            case "weekly":
                this.deadline = deadline.plusWeeks(1);
                break;
            case "monthly":
                this.deadline = deadline.plusMonths(1);
                break;
            case "yearly":
                this.deadline = deadline.plusYears(1);
                break;
        }
    }

    /**
     * Returns the ID of the object.
     */
    public int getId() {
        return id;
    }

    /**
     * Checks if the task is completed.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Returns the current progress value.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Returns the target value.
     */
    public int getTarget() {
        return target;
    }

    /**
     * Returns the current goal.
     */
    public String getGoal() {
        return goal;
    }

    /**
     * Returns the name of the book.
     */
    public String getBookName() {
        return bookName;
    }

    /**
     * Returns the value of frequenzy.
     */
    public String getFrequenzy() {
        return frequenzy;
    }

    /**
     * Sets the ID of the object.
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    /**
     * Returns a string representation of the Goal object.
     */
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

    /**
     * Sets the progress to the specified value.
     */
    public void setProgress(int progress) {
        this.progress = progress;
    }
    public void setDeadline() {
        switch (frequenzy) {
            case "daily":
                deadline.plusDays(1);
                break;
            case "weeklky":
                deadline.plusWeeks(1);
                break;
            case "monthly":
                deadline.plusMonths(1);
                break;
            case "yearly":
                deadline.plusYears(1);
                break;
        }
    }

    /**
     * Returns the deadline.
     */
    public LocalDateTime getDeadline() {
        return deadline;
    }
}
