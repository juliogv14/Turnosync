package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

/**
 * La clase User representa a un usuario de la aplicación
 * Esta clase se usa como plantilla para cargar y guardar objetos desde Cloud Firestore
 * Implementa Parcelable para poder ser incluida en Bundles
 *
 * @author Julio García
 * @see Parcelable
 * @see android.os.Bundle
 */

public class User implements Parcelable {

    /** Identificador unico de usuario */
    private String uid;
    /** Dirección de email */
    private String email;
    /** Nombre de usuario*/
    private String displayname;

    /** Constructor vacío requerido para la carga de datos desde Cloud Firestore */
    public User() {
    }

    /** Construye una instancia de la clase
     * @param uid Identificador del usuario
     * @param email Dirección de email del usuario
     * @param displayname Nombre del usuario
     */
    public User(String uid, String email, String displayname) {
        this.uid = uid;
        this.email = email;
        this.displayname = displayname;
    }

    /** Getter uid */
    public String getUid() {
        return uid;
    }

    /** Setter uid */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /** Getter email */
    public String getEmail() {
        return email;
    }

    /** Setter email */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Getter displayName */
    public String getDisplayname() {
        return displayname;
    }

    /** Setter displayName */
    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    /** Implementacion de Parcelable
     * Construye una instancia desde un Parcel
     * Requerido para pder usarse dentro de un Bundle
     * @see Parcel
     * @see android.os.Bundle
     */
    private User(Parcel in) {
        this.uid = in.readString();
        this.email = in.readString();
        this.displayname = in.readString();
    }

    /** Implementacion de Parcelable
     * Creator necesario para crear listas
     * @see android.os.Parcelable.Creator
     */
    @Exclude
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
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
        out.writeString(email);
        out.writeString(displayname);
    }
}
