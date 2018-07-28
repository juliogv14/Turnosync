package com.juliogv14.turnosync.data;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * La clase ChangeRequest representa una solicitud de cambio de turno.
 * Esta clase se usa como plantilla para cargar y guardar objetos desde Cloud Firestore
 * Implementa Parcelable para poder ser incluida en Bundles
 *
 * @author Julio García
 * @see com.juliogv14.turnosync.data.Shift
 * @see Parcelable
 * @see android.os.Bundle
 */
public class ChangeRequest implements Parcelable{

    //@{
    /** Constantes para definir los posibles estados de un ChangeRequest */
    public static final String REQUESTED = "requested";
    public static final String ACCEPTED = "accepted";
    public static final String APPROVED = "approved";
    public static final String CANCELLED = "cancelled";
    public static final String CONFLICT = "conflict";
    public static final String DENIED_USER = "deniedUser";
    public static final String DENIED_MANAGER = "deniedManager";
    //@}

    /** Identificador en base de datos */
    private String id;

    /** Marca temporal de creación */
    private Date timestamp;

    /** Turno del solicitante con el solicitante */
    private Shift ownShift;

    /** Turno a intercambiar */
    private Shift otherShift;

    /** Estado de la solicitud */
    private String state;

    /** Constructor vacío requerido para la carga de datos desde Cloud Firestore */
    public ChangeRequest() {
    }

    /** Construye una instancia de la clase
     * @param ownShift Turno del solicitante
     * @param otherShift Turno a intercambiar con el solicitante
     * @param timestamp Marca temporal de creación
     */
    public ChangeRequest(Shift ownShift, Shift otherShift, Date timestamp) {
        this.ownShift = ownShift;
        this.otherShift = otherShift;
        this.timestamp = timestamp;
        this.state = REQUESTED;
    }
    /** Getter id */
    public String getId() {
        return id;
    }
    /** Setter id */
    public void setId(String id) {
        this.id = id;
    }
    /** Getter timestamp */
    public Date getTimestamp() {
        return timestamp;
    }
    /** Setter timestamp */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    /** Getter ownShift */
    public Shift getOwnShift() {
        return ownShift;
    }
    /** Setter ownShift */
    public void setOwnShift(Shift ownShift) {
        this.ownShift = ownShift;
    }
    /** Getter otherShift */
    public Shift getOtherShift() {
        return otherShift;
    }
    /** Setter otherShift */
    public void setOtherShift(Shift otherShift) {
        this.otherShift = otherShift;
    }
    /** Getter state */
    public String getState() {
        return state;
    }
    /** Setter state */
    public void setState(String state) {
        this.state = state;
    }

    /** Implementacion de Parcelable
     * Construye una instancia desde un Parcel
     * Requerido para pder usarse dentro de un Bundle
     * @see Parcel
     * @see android.os.Bundle
     */
    private ChangeRequest(Parcel in){
        this.id = in.readString();
        this.ownShift = in.readParcelable(Shift.class.getClassLoader());
        this.otherShift = in.readParcelable(Shift.class.getClassLoader());
        this.state = in.readString();
    }

    /** Implementacion de Parcelable
     * Creator necesario para crear listas
     * @see android.os.Parcelable.Creator
     */
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
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeParcelable(ownShift, flags);
        out.writeParcelable(otherShift, flags);
        out.writeString(state);
    }

}
