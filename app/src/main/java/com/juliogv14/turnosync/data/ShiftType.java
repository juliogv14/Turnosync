package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * La clase ShiftType representa un tipo de turno. Usado para definir un turno Shift
 * Esta clase se usa como plantilla para cargar y guardar objetos desde Cloud Firestore
 * Implementa Parcelable para poder ser incluida en Bundles
 *
 * @author Julio García
 * @see com.juliogv14.turnosync.data.Shift
 * @see Parcelable
 * @see android.os.Bundle
 */
public class ShiftType implements Parcelable{

    /** Identificador en base de datos */
    private String id;
    /** Indica si está actualmente activo o solo está en el historico */
    private boolean active;
    /** Nombre del turno */
    private String name;
    /** Abreviación para mostrar en el calendario */
    private String tag;
    /** Hora de inicio del turno */
    private String startTime;
    /** Duracion del turno en milisegundos */
    private long period;
    /** Color asociado al turno para mostrar en el calendario */
    private int color;

    /** Constructor vacío requerido para la carga de datos desde Cloud Firestore */
    public ShiftType() {   }

    /** Getter id */
    public String getId() {
        return id;
    }

    /** Setter id */
    public void setId(String id) {
        this.id = id;
    }

    /** Getter active */
    public boolean isActive() {
        return active;
    }

    /** Setter active */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** Getter name */
    public String getName() {
        return name;
    }

    /** Setter name */
    public void setName(String name) {
        this.name = name;
    }

    /** Getter tag */
    public String getTag() {
        return tag;
    }

    /** Setter tag */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /** Getter startTime */
    public String getStartTime() {
        return startTime;
    }

    /** Setter startTime */
    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    /** Getter period */
    public long getPeriod() {
        return period;
    }

    /** Setter period */
    public void setPeriod(long period) {
        this.period = period;
    }

    /** Getter jodaStartTime. Devuelve la hora de inicio en un objeto Joda LocalTime
     * Anotacion @exclude para que no se incluya en Cloud Firestore
     * @see LocalTime
     */
    @Exclude
    public LocalTime getJodaStartTime() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        return fmt.parseLocalTime(startTime);
    }

    /** Setter jodaStartTime. Establece la hora de inicio a partir de un objeto Joda LocalTime
     * Anotacion @exclude para que no se incluya en Cloud Firestore
     * @see LocalTime
     */
    @Exclude
    public void setJodaStartTime(LocalTime startTime) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        this.startTime = fmt.print(startTime);;
    }

    /** Getter jodaPeriod. Devuelve la duración de un turno en un objeto Joda Period
     * Anotacion @exclude para que no se incluya en Cloud Firestore
     * @see Period
     */
    @Exclude
    public Period getJodaPeriod() {
        return new Period(period);
    }

    /** Setter jodaPeriod. Establece la duración de un turno a partir de un objeto Joda Period
     * Anotacion @exclude para que no se incluya en Cloud Firestore
     * @see Period
     */
    @Exclude
    public void setJodaPeriod(Period period) {
        this.period = period.toStandardDuration().getMillis();
    }

    /** Getter color */
    public int getColor() {
        return color;
    }
    /** Setter color */
    public void setColor(int color) {
        this.color = color;
    }

    /** Implementacion de Parcelable
     * Construye una instancia desde un Parcel
     * Requerido para pder usarse dentro de un Bundle
     * @see Parcel
     * @see android.os.Bundle
     */
    private ShiftType(Parcel in) {
        this.id = in.readString();
        this.active = in.readByte() != 0;
        this.name = in.readString();
        this.tag = in.readString();
        this.startTime = in.readString();
        this.period = in.readLong();
        this.color = in.readInt();
    }

    /** Implementacion de Parcelable
     * Creator necesario para crear listas
     * @see android.os.Parcelable.Creator
     */
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

    /** Implementacion de Parcelable
     * Descriptor
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /** Implementacion de Parcelable
     * Escritura al Parcel
     * @see Parcel
     */
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
