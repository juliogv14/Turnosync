package com.juliogv14.turnosync.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * La clase GlobalWorkgroup representa a un grupo sin estar relacionado con ningun usuario
 * Esta clase se usa como plantilla para cargar y guardar objetos desde Cloud Firestore
 * Implementa Parcelable para poder ser incluida en Bundles
 *
 * @author Julio García
 * @see Parcelable
 * @see android.os.Bundle
 */
public class GlobalWorkgroup implements Parcelable {

    /** Identificador del grupo */
    private String workgroupId;
    /** Nombre del grupo */
    private String displayName;
    /** Informacion sobre el grupo */
    private String info;
    /** Horas maximas semanales */
    private Long weeklyHours;
    /** Identificador del manager del grupo */
    private String manager;

    /** Constructor vacío requerido para la carga de datos desde Cloud Firestore */
    public GlobalWorkgroup() {
    }

    /** Construye una instancia de la clase
     * @param workgroupId Identificador del grupo
     * @param displayName Nombre del grupo
     * @param info Informacion sobre el grupo
     * @param weeklyHours Horas maximas semanales
     * @param manager Identificador del manager del grupo
     */
    public GlobalWorkgroup(String workgroupId, String displayName, String info, Long weeklyHours, String manager) {
        this.workgroupId = workgroupId;
        this.displayName = displayName;
        this.info = info;
        this.weeklyHours = weeklyHours;
        this.manager = manager;
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
    public String getDisplayname() {
        return displayName;
    }

    /** Setter displayName */
    public void setDisplayname(String displayname) {
        this.displayName = displayname;
    }

    /** Getter info */
    public String getInfo() {
        return info;
    }

    /** Setter info */
    public void setInfo(String info) {
        this.info = info;
    }

    /** Getter weeklyHours */
    public Long getWeeklyHours() {
        return weeklyHours;
    }

    /** Setter weeklyHours */
    public void setWeeklyHours(Long weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    /** Getter manager */
    public String getManager() {
        return manager;
    }

    /** Setter manager */
    public void setManager(String manager) {
        this.manager = manager;
    }

    /** Implementacion de Parcelable
     * Construye una instancia desde un Parcel
     * Requerido para pder usarse dentro de un Bundle
     * @see Parcel
     * @see android.os.Bundle
     */
    private GlobalWorkgroup(Parcel in) {
        workgroupId = in.readString();
        displayName = in.readString();
        info = in.readString();
        weeklyHours = in.readLong();
        manager = in.readString();
    }

    /** Implementacion de Parcelable
     * Creator necesario para crear listas
     * @see android.os.Parcelable.Creator
     */
    public static final Creator<GlobalWorkgroup> CREATOR = new Creator<GlobalWorkgroup>() {
        @Override
        public GlobalWorkgroup createFromParcel(Parcel in) {
            return new GlobalWorkgroup(in);
        }

        @Override
        public GlobalWorkgroup[] newArray(int size) {
            return new GlobalWorkgroup[size];
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
        out.writeLong(weeklyHours);
        out.writeString(manager);
    }
}
