package com.juliogv14.turnosync.data;

/*
* Created by Julio on 13/07/2018
* ShiftChangeRequest.java
*/

import android.os.Parcel;
import android.os.Parcelable;

public class ShiftChangeRequest implements Parcelable{
    //Constants
    public static final String REQUESTED = "requested";
    public static final String ACCEPTED = "accepted";
    public static final String CONFIRMED = "confirmed";

    private String id;
    private Shift ownShift;
    private Shift otherShift;
    private String state;

    public ShiftChangeRequest(Shift ownShift, Shift otherShift) {
        this.ownShift = ownShift;
        this.otherShift = otherShift;
        this.state = REQUESTED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    private ShiftChangeRequest (Parcel in){
        this.id = in.readString();
        this.ownShift = in.readParcelable(Shift.class.getClassLoader());
        this.otherShift = in.readParcelable(Shift.class.getClassLoader());
        this.state = in.readString();
    }

    public static final Creator<ShiftChangeRequest> CREATOR = new Creator<ShiftChangeRequest>() {
        @Override
        public ShiftChangeRequest createFromParcel(Parcel in) {
            return new ShiftChangeRequest(in);
        }

        @Override
        public ShiftChangeRequest[] newArray(int size) {
            return new ShiftChangeRequest[size];
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
