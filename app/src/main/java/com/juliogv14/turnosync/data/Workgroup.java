package com.juliogv14.turnosync.data;

/**
 * Created by Julio on 20/01/2018.
 * Workgroup
 */

public class Workgroup {
    private String workgroupID;
    private String displayname;

    public Workgroup() {
    }

    public Workgroup(String workgroupID, String displayname) {
        this.workgroupID = workgroupID;
        this.displayname = displayname;
    }

    public String getWorkgroupID() {
        return workgroupID;
    }

    public void setWorkgroupID(String workgroupID) {
        this.workgroupID = workgroupID;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

}
