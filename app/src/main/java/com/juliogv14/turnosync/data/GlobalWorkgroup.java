package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Julio on 24/03/2018.
 * GlobalWorkgroup
 */

public class GlobalWorkgroup implements Parcelable {
    private String displayname;
    private String info;

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

    //Parcelable implementation
    private GlobalWorkgroup(Parcel in) {
        displayname = in.readString();
        info = in.readString();
    }

    public static final Creator<GlobalWorkgroup> CREATOR = new Creator<GlobalWorkgroup>() {
        @Override
        public GlobalWorkgroup createFromParcel(Parcel in) {
            return new GlobalWorkgroup(in);
        }

        @Override
        public GlobalWorkgroup[] newArray(int size) {
            return new GlobalWorkgroup[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(displayname);
        out.writeString(info);
    }
}
