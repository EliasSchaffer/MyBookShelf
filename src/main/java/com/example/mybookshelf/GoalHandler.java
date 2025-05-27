package com.example.mybookshelf;

import com.example.mybookshelf.dataClass.Book;
import com.example.mybookshelf.dataClass.Goal;
import com.example.mybookshelf.dataClass.User;
import com.example.mybookshelf.LayoutManager.CustomGoalAdapter;
import com.example.mybookshelf.notifications.NotificationScheduler;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class GoalHandler {

    private User user;
    private CustomGoalAdapter adapter;
    private List<Goal> goalList;
    private List<Goal> completedGoalList;
    private DataBaseConnection db;
    private MainActivity mainActivity;

    public GoalHandler(User user, DataBaseConnection db, MainActivity mainActivity) {
        this.user = user;
        this.goalList = user.getGoalList();
        this.completedGoalList = user.getCompletedGoalList();
        this.db = db;

    }


    public void handleBookAdded(Book book) {
        if (book != null && book.getStatus().equals("Completed")) {
            int progress = 0;
            Iterator<Goal> iterator = goalList.iterator();
            while (iterator.hasNext()) {
                Goal goal = iterator.next();
                if (book != null && book.getStatus().equals("Completed")) {
                    progress = goal.getProgress();
                    switch (goal.getGoal()) {
                        case "Read Books":
                            progress += 1;
                            if (progress == goal.getTarget()) {
                                iterator.remove();
                                goal.setProgress(progress);
                                completedGoalList.add(goal);
                            } else {
                                goal.setProgress(progress);
                            }
                            break;

                        case "Read Pages":
                            progress += book.getPages();
                            if (progress == goal.getTarget()) {
                                iterator.remove();
                                goal.setProgress(progress);
                                completedGoalList.add(goal);
                            } else {
                                goal.setProgress(progress);
                            }
                            break;

                        case "Read Time":
                            progress += book.getPages() * 1.5;
                            if (progress == goal.getTarget()) {
                                iterator.remove();
                                goal.setProgress(progress);
                                completedGoalList.add(goal);
                            } else {
                                goal.setProgress(progress);
                            }
                            break;

                        case "Read Specific Book":
                            if (goal.getBookName().equals(book.getName())) {
                                iterator.remove();
                                goal.setProgress(goal.getTarget());
                                completedGoalList.add(goal);
                            }
                            break;

                        default:
                            break;
                    }
                    db.updateGoalProgress(user.getUid(),goal.getId(),progress);
                }
            }

        }

    }

    public void handleBookChange(Book book) {
        if (book == null) return;

        if (book.getStatus().equals("Completed")) {
            Iterator<Goal> iterator = goalList.iterator();
            while (iterator.hasNext()) {
                Goal goal = iterator.next();
                int progress = goal.getProgress();

                switch (goal.getGoal()) {
                    case "Read Books":
                        progress += 1;
                        if (progress == goal.getTarget()) {
                            iterator.remove();
                            goal.setProgress(progress);
                            completedGoalList.add(goal);
                        } else {
                            goal.setProgress(progress);
                        }
                        break;

                    case "Read Pages":
                        progress += book.getPages();
                        if (progress == goal.getTarget()) {
                            iterator.remove();
                            goal.setProgress(progress);
                            completedGoalList.add(goal);
                        } else {
                            goal.setProgress(progress);
                        }
                        break;

                    case "Read Time":
                        progress += book.getPages() * 1.5;
                        if (progress == goal.getTarget()) {
                            iterator.remove();
                            goal.setProgress(progress);
                            completedGoalList.add(goal);
                        } else {
                            goal.setProgress(progress);
                        }
                        break;

                    case "Read Specific Book":
                        if (goal.getBookName().equals(book.getName())) {
                            iterator.remove();
                            goal.setProgress(goal.getTarget());
                            completedGoalList.add(goal);
                        }
                        break;

                    default:
                        break;
                }
                db.updateGoalProgress(user.getUid(),goal.getId(),progress);
            }
        } else {
            Iterator<Goal> iterator = goalList.iterator();
            while (iterator.hasNext()) {
                Goal goal = iterator.next();
                int progress = goal.getProgress();

                switch (goal.getGoal()) {
                    case "Read Books":
                        progress -= 1;
                        if (progress == goal.getTarget()) {
                            iterator.remove();
                            goal.setProgress(progress);
                            completedGoalList.add(goal);
                        } else {
                            goal.setProgress(progress);
                        }
                        break;

                    case "Read Pages":
                        progress -= book.getPages(); // ❗ Achtung: sollte hier evtl. -= sein?
                        if (progress == goal.getTarget()) {
                            iterator.remove();
                            goal.setProgress(progress);
                            completedGoalList.add(goal);
                        } else {
                            goal.setProgress(progress);
                        }
                        break;

                    case "Read Time":
                        progress -= book.getPages() * 1.5;
                        if (progress == goal.getTarget()) {
                            iterator.remove();
                            goal.setProgress(progress);
                            completedGoalList.add(goal);
                        } else {
                            goal.setProgress(progress);
                        }
                        break;

                    case "Read Specific Book":
                        if (goal.getBookName().equals(book.getName())) {
                            iterator.remove();
                            goal.setProgress(goal.getTarget());
                            completedGoalList.add(goal);
                        }
                        break;

                    default:
                        break;
                }
                db.updateGoalProgress(user.getUid(),goal.getId(),progress);

            }
        }
    }

    public void handleBookRemoved(Book book) {
            int progress = 0;
            Iterator<Goal> iterator = goalList.iterator();
            while (iterator.hasNext()) {
                Goal goal = iterator.next();
                    progress = goal.getProgress();
                    switch (goal.getGoal()) {
                        case "Read Books":
                            progress -= 1;
                            if (progress == goal.getTarget()) {
                                iterator.remove();
                                goal.setProgress(progress);
                                completedGoalList.add(goal);
                            } else {
                                goal.setProgress(progress);
                            }
                            break;

                        case "Read Pages":
                            progress -= book.getPages();
                            if (progress == goal.getTarget()) {
                                iterator.remove();
                                goal.setProgress(progress);
                                completedGoalList.add(goal);
                            } else {
                                goal.setProgress(progress);
                            }
                            break;

                        case "Read Time":
                            progress -= book.getPages() * 1.5;
                            if (progress == goal.getTarget()) {
                                iterator.remove();
                                goal.setProgress(progress);
                                completedGoalList.add(goal);
                            } else {
                                goal.setProgress(progress);
                            }
                            break;

                        case "Read Specific Book":
                            if (goal.getBookName().equals(book.getName())) {
                                iterator.remove();
                                goal.setProgress(goal.getTarget());
                                completedGoalList.add(goal);
                            }
                            break;

                        default:
                            break;
                    }
                    db.updateGoalProgress(user.getUid(),goal.getId(),progress);
                    if (goalList.isEmpty()){
                        NotificationScheduler.cancelDailyNotification(mainActivity);
                    }
                }
    }
}
