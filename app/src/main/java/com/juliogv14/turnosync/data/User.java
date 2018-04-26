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
    private String uid;
    private String email;
    private String displayname;

    public User(String uid, String email, String displayname) {
        this.uid = uid;
        this.email = email;
        this.displayname = displayname;
    }

    public User(FirebaseUser user) {
        this.uid = user.getUid();
        this.email = user.getEmail();
        this.displayname = user.getDisplayName();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
        this.uid = in.readString();
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
        out.writeString(uid);
        out.writeString(email);
        out.writeString(displayname);
    }
}
