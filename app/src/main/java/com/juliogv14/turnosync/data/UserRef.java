package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

/**
 * La clase UserRef representa una referencia de un usuario dentro de la colección de usuarios
 * bajo un GlobalWorkgroup en Cloud Firestore
 * Esta clase se usa como plantilla para cargar y guardar objetos desde Cloud Firestore
 * Implementa Parcelable para poder ser incluida en Bundles
 *
 * @author Julio García
 * @see com.juliogv14.turnosync.data.Shift
 * @see Parcelable
 * @see android.os.Bundle
 * @see GlobalWorkgroup
 */
public class UserRef implements Parcelable {

    /** Identificador unico de usuario */
    private String uid;
    /** Abreviación para mostrar en el calendario */
    private String shortName;
    /** Indica si está actualmente activo o solo está en el historico */
    private boolean active;

    /** Constructor vacío requerido para la carga de datos desde Cloud Firestore */
    public UserRef() {
    }

    /** Construye una instancia de la clase
     * @param uid Identificador del usuario
     */
    public UserRef(String uid) {
        this.uid = uid;
    }

    /** Getter uid */
    public String getUid() {
        return uid;
    }

    /** Setter uid */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /** Getter shortName */
    public String getShortName() {
        return shortName;
    }

    /** Setter shortName */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /** Getter active */
    public boolean isActive() {
        return active;
    }

    /** Setter active */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** Implementacion de Parcelable
     * Construye una instancia desde un Parcel
     * Requerido para pder usarse dentro de un Bundle
     * @see Parcel
     * @see android.os.Bundle
     */
    private UserRef(Parcel in) {
        this.uid = in.readString();
        this.shortName = in.readString();
        this.active = in.readByte() != 0;
    }

    /** Implementacion de Parcelable
     * Creator necesario para crear listas
     * @see android.os.Parcelable.Creator
     */
    @Exclude
    public static final Creator<UserRef> CREATOR = new Creator<UserRef>() {
        @Override
        public UserRef createFromParcel(Parcel in) {
            return new UserRef(in);
        }

        @Override
        public UserRef[] newArray(int size) {
            return new UserRef[size];
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
        out.writeString(uid);
        out.writeString(shortName);
        out.writeByte((byte)(active ? 1 : 0 ));
    }
}
