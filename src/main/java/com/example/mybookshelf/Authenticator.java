package com.example.mybookshelf;

import java.util.ArrayList;
import java.util.List;

public class Authenticator {

    List<Login> safedLogins = new ArrayList<>();

    public Authenticator() {
        safedLogins.add(new Login("Test","123"));
        safedLogins.add(new Login("Test1","Hallo"));
        safedLogins.add(new Login("Test2","nnnn"));
        safedLogins.add(new Login("Test3","Buh!"));

    }

    public boolean checkLogin(Login attempt){
        for (Login login : safedLogins){
            if (login.getPassword().equals(attempt.getPassword()) && login.getUser().equals(attempt.getUser())) return true;
        }
        return false;
    }
}
