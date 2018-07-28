package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * La clase Shift representa un turno de trabajo de un usuario.
 * Esta clase se usa como plantilla para cargar y guardar objetos desde Cloud Firestore
 * Implementa Parcelable para poder ser incluida en Bundles
 * Implementa Comparable para poder ordenar una colección de elementos de este tipo
 *
 * @author Julio García
 * @see Parcelable
 * @see android.os.Bundle
 * @see Comparable
 * @see ShiftType
 */

public class Shift implements Parcelable, Comparable<Shift> {

    /** Identificador en base de datos */
    private String id;
    /** Identificador del usuario asociado */
    private String userId;
    /** Identificador del tipo de turno*/
    private String type;
    /** Fecha del dia en el que el turno tiene lugar. La hora es establecida a las 00:00 UTC*/
    private Date date;

    /** Constructor vacío requerido para la carga de datos desde Cloud Firestore */
    public Shift() {
    }

    /** Construye una instancia de la clase
     * @param userId Identificador del usuario asociado
     * @param date Fecha del dia en el que el turno tiene lugar
     * @param type Identificador del tipo de turno
     */
    public Shift(String userId, Date date, String type) {
        this.type = type;
        this.userId = userId;
        this.date = date;
    }

    /** Getter id */
    public String getId() {
        return id;
    }

    /** Setter id */
    public void setId(String id) {
        this.id = id;
    }

    /** Getter userId */
    public String getUserId() {
        return userId;
    }

    /** Setter userId */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** Getter type */
    public String getType() {
        return type;
    }

    /** Setter type */
    public void setType(String type) {
        this.type = type;
    }

    /** Getter date */
    public Date getDate() {
        return date;
    }

    /** Setter date */
    public void setDate(Date date) {
        this.date = date;
    }


    /** Implementacion de Parcelable
     * Construye una instancia desde un Parcel
     * Requerido para pder usarse dentro de un Bundle
     * @see Parcel
     * @see android.os.Bundle
     */
    private Shift(Parcel in) {
        this.userId = in.readString();
        this.type = in.readString();
        this.date = new Date(in.readLong());
    }

    /** Implementacion de Parcelable
     * Creator necesario para crear listas
     * @see android.os.Parcelable.Creator
     */
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
        out.writeString(userId);
        out.writeString(type);
        out.writeLong(date.getTime());
    }

    /** Implementacion de Comparable
     * Usado para designar el orden dentro de una colección usando la fecha como campo.
     */
    @Override
    public int compareTo(@NonNull Shift o) {
        return Long.compare(this.date.getTime(), o.getDate().getTime());
    }
}
