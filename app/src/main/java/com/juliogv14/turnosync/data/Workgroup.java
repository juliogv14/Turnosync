package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Julio on 20/01/2018.
 * Workgroup
 */

public class Workgroup implements Parcelable {
    private String workgroupID;
    private String displayname;
    private String info;

    private int level;
    private boolean selected;

    public Workgroup() {
    }

    public Workgroup(String workgroupID, String displayname, String info) {
        this.workgroupID = workgroupID;
        this.displayname = displayname;
        this.info = info;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    //Parcelable implementation
    private Workgroup(Parcel in) {
        workgroupID = in.readString();
        displayname = in.readString();
        info = in.readString();
    }

    public static final Creator<Workgroup> CREATOR = new Creator<Workgroup>() {
        @Override
        public Workgroup createFromParcel(Parcel in) {
            return new Workgroup(in);
        }

        @Override
        public Workgroup[] newArray(int size) {
            return new Workgroup[size];
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
    }


}
