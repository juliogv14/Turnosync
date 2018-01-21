package com.juliogv14.turnosync.data;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Julio on 21/01/2018.
 * User.java
 */

public class User {
    private String email;
    private String displayname;

    public User(String email, String displayname) {
        this.email = email;
        this.displayname = displayname;
    }

    public User(FirebaseUser user) {
        this.email = user.getEmail();
        this.displayname = user.getDisplayName();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }
}
