package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Exclude;

/**
 * Created by Julio on 21/01/2018.
 * USER.java
 */

public class User implements Parcelable {
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

    //Parcelable implementation
    private User(Parcel in) {
        this.email = in.readString();
        this.displayname = in.readString();
    }

    @Exclude
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(email);
        out.writeString(displayname);
    }
}
