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
    private String workgroupId;
    private String displayName;
    private String info;
    private String role;
    @Exclude
    private boolean selected;

    public UserWorkgroup(String workgroupId, String displayName, String info, String role) {
        this.workgroupId = workgroupId;
        this.displayName = displayName;
        this.info = info;
        this.role = role;
    }

    //Parcelable implementation
    private UserWorkgroup(Parcel in) {
        this.workgroupId = in.readString();
        this.displayName = in.readString();
        this.info = in.readString();
        this.role = in.readString();
        this.selected = in.readByte() != 0;
    }

    public String getWorkgroupId() {
        return workgroupId;
    }

    public void setWorkgroupId(String workgroupId) {
        this.workgroupId = workgroupId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(workgroupId);
        out.writeString(displayName);
        out.writeString(info);
        out.writeString(role);
        out.writeByte((byte) (selected ? 1 : 0));
    }
}
