package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ShiftType implements Parcelable{
    private String id;
    private boolean active;

    private String name;
    private String tag;
    private String startTime;
    private long period;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    @Exclude
    public LocalTime getJodaStartTime() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        return fmt.parseLocalTime(startTime);
    }
    @Exclude
    public void setJodaStartTime(LocalTime startTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        this.startTime = fmt.print(startTime);;
    }

    @Exclude
    public Period getJodaPeriod() {
        return new Period(period);
    }
    @Exclude
    public void setJodaPeriod(Period period) {
        this.period = period.toStandardDuration().getMillis();
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
        this.startTime = in.readString();
        this.period = in.readLong();
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
        out.writeString(startTime);
        out.writeLong(period);
        out.writeInt(color);
    }

}
