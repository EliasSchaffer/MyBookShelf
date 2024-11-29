package com.example.mybookshelf;

import java.util.ArrayList;
import java.util.List;

public class Authenticator {

    List<User> safedUsers = new ArrayList<>();

    public Authenticator() {
        safedUsers.add(new User("Test","123"));
        safedUsers.add(new User("Test1","Hallo"));
        safedUsers.add(new User("Test2","nnnn"));
        safedUsers.add(new User("Test3","Buh!"));

    }

    public boolean checkLogin(User attempt){
        for (User user : safedUsers){
            if (user.getPassword().equals(attempt.getPassword()) && user.getUser().equals(attempt.getUser())) return true;
        }
        return false;
    }
}
