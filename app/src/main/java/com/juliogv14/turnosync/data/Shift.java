package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Julio on 20/01/2018.
 * Shift.java
 */

public class Shift implements Parcelable, Comparable<Shift> {

    private String id;
    private String userId;
    private String type;

    private Date date;
    private String start;
    private String end;

    public Shift() {
    }

    public Shift(String userId, String type, Date date, String start, String end) {
        this.type = type;
        this.userId = userId;
        this.date = date;
        this.start = start;
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        this.userId = in.readString();
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
        out.writeString(userId);
        out.writeString(type);
        out.writeLong(date.getTime());
        out.writeString(start);
        out.writeString(end);
    }

    @Override
    public int compareTo(@NonNull Shift o) {
        return Long.compare(this.date.getTime(), o.getDate().getTime());
    }
}
