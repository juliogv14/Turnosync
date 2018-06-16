package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

/**
 * Created by Julio on 06/06/2018.
 * UserRef.java
 */

public class UserRef implements Parcelable {
    private String uid;
    private boolean active;

    public UserRef() {
    }

    public UserRef(String uid, boolean active) {
        this.uid = uid;
        this.active = active;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    //Parcelable implementation
    private UserRef(Parcel in) {
        this.uid = in.readString();
        this.active = in.readByte() != 0;
    }

    @Exclude
    public static final Creator<UserRef> CREATOR = new Creator<UserRef>() {
        @Override
        public UserRef createFromParcel(Parcel in) {
            return new UserRef(in);
        }

        @Override
        public UserRef[] newArray(int size) {
            return new UserRef[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(uid);
        out.writeByte((byte)(active ? 1 : 0 ));
    }
}
