package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

/**
 * La clase UserWorkgroup representa un grupo asociado con un usuario. Se encuentran en una colección
 * bajo usuarios User en Cloud Firestore
 * Esta clase se usa como plantilla para cargar y guardar objetos desde Cloud Firestore
 * Implementa Parcelable para poder ser incluida en Bundles
 *
 * @author Julio García
 * @see Parcelable
 * @see android.os.Bundle
 */

public class UserWorkgroup implements Parcelable {
    /** Identificador del grupo */
    private String workgroupId;
    /** Nombre del grupo */
    private String displayName;
    /** Informacion sobre el grupo */
    private String info;
    /** Rol del usuario */
    private String role;
    /** Indica si esta actualmente seleccionado. Necesario en la aplicacion. Se excluye de base de datos */
    @Exclude
    private boolean selected;

    /** Constructor vacío requerido para la carga de datos desde Cloud Firestore */
    public UserWorkgroup() {
    }

    /** Construye una instancia de la clase
     * @param workgroupId Identificador del grupo
     * @param displayName Nombre del grupo
     * @param info Nombre del usuario
     * @param role Rol asignado al usuario
     */
    public UserWorkgroup(String workgroupId, String displayName, String info, String role) {
        this.workgroupId = workgroupId;
        this.displayName = displayName;
        this.info = info;
        this.role = role;
    }

    /** Getter workgroupId */
    public String getWorkgroupId() {
        return workgroupId;
    }

    /** Setter workgroupId */
    public void setWorkgroupId(String workgroupId) {
        this.workgroupId = workgroupId;
    }

    /** Getter displayName */
    public String getDisplayName() {
        return displayName;
    }

    /** Setter displayName */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** Getter info */
    public String getInfo() {
        return info;
    }

    /** Setter info */
    public void setInfo(String info) {
        this.info = info;
    }

    /** Getter role */
    public String getRole() {
        return role;
    }

    /** Setter role */
    public void setRole(String role) {
        this.role = role;
    }

    /** Getter selected */
    @Exclude
    public boolean isSelected() {
        return selected;
    }

    /** Setter role */
    @Exclude
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /** Implementacion de Parcelable
     * Construye una instancia desde un Parcel
     * Requerido para pder usarse dentro de un Bundle
     * @see Parcel
     * @see android.os.Bundle
     */
    private UserWorkgroup(Parcel in) {
        this.workgroupId = in.readString();
        this.displayName = in.readString();
        this.info = in.readString();
        this.role = in.readString();
        this.selected = in.readByte() != 0;
    }

    /** Implementacion de Parcelable
     * Creator necesario para crear listas
     * @see android.os.Parcelable.Creator
     */
    public static final Parcelable.Creator<UserWorkgroup> CREATOR = new Parcelable.Creator<UserWorkgroup>() {
        @Override
        public UserWorkgroup createFromParcel(Parcel in) {
            return new UserWorkgroup(in);
        }

        @Override
        public UserWorkgroup[] newArray(int size) {
            return new UserWorkgroup[size];
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
        out.writeString(workgroupId);
        out.writeString(displayName);
        out.writeString(info);
        out.writeString(role);
        out.writeByte((byte) (selected ? 1 : 0));
    }
}
