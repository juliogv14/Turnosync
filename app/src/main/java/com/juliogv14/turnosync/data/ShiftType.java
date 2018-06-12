package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ShiftType implements Parcelable{
    private String id;
    private boolean active;

    private String name;
    private String tag;
    private Date startTime;
    private Date endTime;
    private int color;

    public ShiftType() {   }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    //Parcelable implementation
    private ShiftType(Parcel in) {
        this.id = in.readString();
        this.active = in.readByte() != 0;
        this.name = in.readString();
        this.tag = in.readString();
        this.startTime = new Date(in.readLong());
        this.endTime = new Date(in.readLong());
        this.color = in.readInt();
    }

    public static final Creator<ShiftType> CREATOR = new Creator<ShiftType>() {
        @Override
        public ShiftType createFromParcel(Parcel in) {
            return new ShiftType(in);
        }

        @Override
        public ShiftType[] newArray(int size) {
            return new ShiftType[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeByte((byte)(active ? 1 : 0));
        out.writeString(name);
        out.writeString(tag);
        out.writeLong(startTime.getTime());
        out.writeLong(endTime.getTime());
        out.writeInt(color);
    }

}
