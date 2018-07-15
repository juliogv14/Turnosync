package com.juliogv14.turnosync.data;

/*
* Created by Julio on 13/07/2018
* ChangeRequest.java
*/

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ChangeRequest implements Parcelable{
    //Constants
    public static final String REQUESTED = "requested";
    public static final String ACCEPTED = "accepted";
    public static final String APPROVED = "approved";

    private String id;
    private Date timestamp;
    private Shift ownShift;
    private Shift otherShift;
    private String state;

    public ChangeRequest() {
    }

    public ChangeRequest(Shift ownShift, Shift otherShift, Date timestamp) {
        this.ownShift = ownShift;
        this.otherShift = otherShift;
        this.timestamp = timestamp;
        this.state = REQUESTED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Shift getOwnShift() {
        return ownShift;
    }

    public void setOwnShift(Shift ownShift) {
        this.ownShift = ownShift;
    }

    public Shift getOtherShift() {
        return otherShift;
    }

    public void setOtherShift(Shift otherShift) {
        this.otherShift = otherShift;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    //Parcelable implementation
    private ChangeRequest(Parcel in){
        this.id = in.readString();
        this.ownShift = in.readParcelable(Shift.class.getClassLoader());
        this.otherShift = in.readParcelable(Shift.class.getClassLoader());
        this.state = in.readString();
    }

    public static final Creator<ChangeRequest> CREATOR = new Creator<ChangeRequest>() {
        @Override
        public ChangeRequest createFromParcel(Parcel in) {
            return new ChangeRequest(in);
        }

        @Override
        public ChangeRequest[] newArray(int size) {
            return new ChangeRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeParcelable(ownShift, flags);
        out.writeParcelable(otherShift, flags);
        out.writeString(state);
    }

}
