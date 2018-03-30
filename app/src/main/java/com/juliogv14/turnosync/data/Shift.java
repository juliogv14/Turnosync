package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

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
    private String start;
    private String end;

    public Shift() {
    }

    public Shift(String type, String userID, int year, int month, int day, String start, String end) {
        this.type = type;
        this.userID = userID;
        this.year = year;
        this.month = month;
        this.day = day;
        this.start = start;
        this.end = end;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
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
        this.type = in.readString();
        this.userID = in.readString();
        this.year = in.readInt();
        this.month = in.readInt();
        this.day = in.readInt();
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
        out.writeString(type);
        out.writeString(userID);
        out.writeInt(year);
        out.writeInt(month);
        out.writeInt(day);
        out.writeString(start);
        out.writeString(end);
    }
}
