/**
 *
 */
package com.audioweb.entity;

import java.util.Date;

/**
 * @author shuofang 定时任务
 */
public class ScheTask {
    private String TaskId;
    private String ScheId;
    private String LastingSeconds;
    private String Vols;
    private String TaskName;
    private String StartDateTime;
    private String EndDateTime = "";
    private String UpDateTime;
    private String FilesInfo;
    private String Weeks;
    private String DomainsId;
    private String ExecTime;
    private Boolean Status;
    private String ScheName;
    private String note;
    private int tasktype;//广播类型 0 顺序播放，1 循环播放， 2 随机播放
    private Date nextFireTime;
    private String jobStatus;
    private String SingleDate;
    private String castlevel;//广播优先级

    public String getTaskId() {
        return TaskId;
    }

    public void setTaskId(String taskId) {
        TaskId = taskId;
    }

    public String getScheId() {
        return ScheId;
    }

    public void setScheId(String scheId) {
        ScheId = scheId;
    }

    public String getLastingSeconds() {
        return LastingSeconds;
    }

    public void setLastingSeconds(String lastingSeconds) {
        LastingSeconds = lastingSeconds;
    }

    public String getVols() {
        return Vols;
    }

    public void setVols(String vols) {
        Vols = vols;
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

    public String getStartDateTime() {
        return StartDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        StartDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return EndDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        EndDateTime = endDateTime;
    }

    public String getFilesInfo() {
        return FilesInfo;
    }

    public void setFilesInfo(String filesInfo) {
        FilesInfo = filesInfo;
    }

    public String getWeeks() {
        return Weeks;
    }

    public void setWeeks(String weeks) {
        Weeks = weeks;
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

    public String getExecTime() {
        return ExecTime;
    }

    public void setExecTime(String execTime) {
        ExecTime = execTime;
    }

    public Boolean getStatus() {
        return Status;
    }

    public void setStatus(Boolean status) {
        Status = status;
    }

    /**
     * @return the scheName
     */
    public String getScheName() {
        return ScheName;
    }

    /**
     * @param scheName the scheName to set
     */
    public void setScheName(String scheName) {
        ScheName = scheName;
    }

    /**
     * @return the upDateTime
     */
    public String getUpDateTime() {
        return UpDateTime;
    }

    /**
     * @param upDateTime the upDateTime to set
     */
    public void setUpDateTime(String upDateTime) {
        UpDateTime = upDateTime;
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
     * @return the singleDate
     */
    public String getSingleDate() {
        return SingleDate;
    }

    /**
     * @param singleDate the singleDate to set
     */
    public void setSingleDate(String singleDate) {
        SingleDate = singleDate;
    }

    /**
     * @return the tasktype
     */
    public int getTasktype() {
        return tasktype;
    }

    /**
     * @param tasktype the tasktype to set
     */
    public void setTasktype(int tasktype) {
        this.tasktype = tasktype;
    }

    public String getCastlevel() {
        return castlevel;
    }

    public void setCastlevel(String castlevel) {
        this.castlevel = castlevel;
    }
}
