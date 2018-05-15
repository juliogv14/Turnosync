package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Julio on 20/01/2018.
 * Shift.java
 */

public class Shift implements Parcelable {
    //JSON database
    private String type;
    private String userID;

    private int year;
    private int month;
    private int day;
    private Date date;
    private String start;
    private String end;

    public Shift() {
    }

    public Shift(String userID, String type, Date date, String start, String end) {
        this.type = type;
        this.userID = userID;
        this.date = date;
        this.start = start;
        this.end = end;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }


    //Parcelable implementation
    private Shift(Parcel in) {
        this.userID = in.readString();
        this.type = in.readString();
        this.date = new Date(in.readLong());
        this.start = in.readString();
        this.end = in.readString();
    }

    public static final Creator<Shift> CREATOR = new Creator<Shift>() {
        @Override
        public Shift createFromParcel(Parcel in) {
            return new Shift(in);
        }

        @Override
        public Shift[] newArray(int size) {
            return new Shift[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(userID);
        out.writeString(type);
        out.writeLong(date.getTime());
        out.writeString(start);
        out.writeString(end);
    }
}
