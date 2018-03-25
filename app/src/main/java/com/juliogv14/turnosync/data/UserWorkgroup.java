package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Created by Julio on 24/03/2018.
 * UserWorkgroup
 */
@IgnoreExtraProperties
public class UserWorkgroup implements Parcelable {
    private String workgroupID;
    private String displayname;
    private String info;
    private String role;
    @Exclude
    private boolean selected;

    public UserWorkgroup(String workgroupID, String displayname, String info, String role) {
        this.workgroupID =
                this.displayname = displayname;
        this.info = info;
        this.role = role;
    }

    public String getWorkgroupID() {
        return workgroupID;
    }

    public void setWorkgroupID(String workgroupID) {
        this.workgroupID = workgroupID;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Exclude
    public boolean isSelected() {
        return selected;
    }

    @Exclude
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    //Parcelable implementation
    private UserWorkgroup(Parcel in) {
        workgroupID = in.readString();
        displayname = in.readString();
        info = in.readString();
        role = in.readString();
        selected = in.readByte() != 0;
    }

    @Exclude
    public static final Parcelable.Creator<UserWorkgroup> CREATOR = new Parcelable.Creator<UserWorkgroup>() {
        @Override
        public UserWorkgroup createFromParcel(Parcel in) {
            return new UserWorkgroup(in);
        }

        @Override
        public UserWorkgroup[] newArray(int size) {
            return new UserWorkgroup[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(workgroupID);
        out.writeString(displayname);
        out.writeString(info);
        out.writeString(role);
        out.writeByte((byte) (selected ? 1 : 0));
    }
}
