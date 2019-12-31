package com.audioweb.entity;

import java.util.Date;

/**
 * @author shuofang 终端采播
 */
public class TermTask {
    private int TaskId;
    private String TaskName;
    private Boolean Status;
    private Boolean isOnline = false;
    private Boolean isJuris = false;//源终端是否有权限
    private Date upDateTime;
    private Date nextFireTime;
    private String jobStatus;
    private String note;
    private String ExecTime;
    private String DomainsId;
    private String TIDString;
    private String Weeks;
    private String LastingSeconds;
    private String TName;
    private int Vols;
    private int CastLevel;
    private String type;

    /**
     * @return the taskId
     */
    public int getTaskId() {
        return TaskId;
    }

    /**
     * @param taskId the taskId to set
     */
    public void setTaskId(int taskId) {
        TaskId = taskId;
    }

    /**
     * @return the taskName
     */
    public String getTaskName() {
        return TaskName;
    }

    /**
     * @param taskName the taskName to set
     */
    public void setTaskName(String taskName) {
        TaskName = taskName;
    }

    /**
     * @return the status
     */
    public Boolean getStatus() {
        return Status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Boolean status) {
        Status = status;
    }

    /**
     * @return the upDateTime
     */
    public Date getUpDateTime() {
        return upDateTime;
    }

    /**
     * @param upDateTime the upDateTime to set
     */
    public void setUpDateTime(Date upDateTime) {
        this.upDateTime = upDateTime;
    }

    /**
     * @return the nextFireTime
     */
    public Date getNextFireTime() {
        return nextFireTime;
    }

    /**
     * @param nextFireTime the nextFireTime to set
     */
    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    /**
     * @return the jobStatus
     */
    public String getJobStatus() {
        return jobStatus;
    }

    /**
     * @param jobStatus the jobStatus to set
     */
    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * @return the execTime
     */
    public String getExecTime() {
        return ExecTime;
    }

    /**
     * @param execTime the execTime to set
     */
    public void setExecTime(String execTime) {
        ExecTime = execTime;
    }

    /**
     * @return the domainsId
     */
    public String getDomainsId() {
        return DomainsId;
    }

    /**
     * @param domainsId the domainsId to set
     */
    public void setDomainsId(String domainsId) {
        DomainsId = domainsId;
    }

    /**
     * @return the tIDString
     */
    public String getTIDString() {
        return TIDString;
    }

    /**
     * @param tIDString the tIDString to set
     */
    public void setTIDString(String tIDString) {
        TIDString = tIDString;
    }

    /**
     * @return the weeks
     */
    public String getWeeks() {
        return Weeks;
    }

    /**
     * @param weeks the weeks to set
     */
    public void setWeeks(String weeks) {
        Weeks = weeks;
    }

    /**
     * @return the vols
     */
    public int getVols() {
        return Vols;
    }

    /**
     * @param vols the vols to set
     */
    public void setVols(int vols) {
        Vols = vols;
    }

    /**
     * @return the isOnline
     */
    public Boolean getIsOnline() {
        return isOnline;
    }

    /**
     * @param isOnline the isOnline to set
     */
    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    /**
     * @return the tName
     */
    public String getTName() {
        return TName;
    }

    /**
     * @param tName the tName to set
     */
    public void setTName(String tName) {
        TName = tName;
    }

    /**
     * @return the castLevel
     */
    public int getCastLevel() {
        return CastLevel;
    }

    /**
     * @param castLevel the castLevel to set
     */
    public void setCastLevel(int castLevel) {
        CastLevel = castLevel;
    }

    /**
     * @return the lastingSeconds
     */
    public String getLastingSeconds() {
        return LastingSeconds;
    }

    /**
     * @param lastingSeconds the lastingSeconds to set
     */
    public void setLastingSeconds(String lastingSeconds) {
        LastingSeconds = lastingSeconds;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsJuris() {
        return isJuris;
    }

    public void setIsJuris(Boolean isJuris) {
        this.isJuris = isJuris;
    }

}
