/**
 *
 */
package com.audioweb.entity;

/**
 * @author shuofang 定时方案
 */
public class Schedules {
    private String ScheId;
    private String ScheName;
    private String Priority;//方案优先级
    private String TaskNum;//方案下任务总数
    private String Description;
    private Boolean IsExecSchd;

    /**
     * @return the scheId
     */
    public String getScheId() {
        return ScheId;
    }

    /**
     * @param scheId the scheId to set
     */
    public void setScheId(String scheId) {
        ScheId = scheId;
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
     * @return the priority
     */
    public String getPriority() {
        return Priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(String priority) {
        Priority = priority;
    }


    /**
     * @return the taskNum
     */
    public String getTaskNum() {
        return TaskNum;
    }

    /**
     * @param taskNum the taskNum to set
     */
    public void setTaskNum(String taskNum) {
        TaskNum = taskNum;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return Description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        Description = description;
    }

    /**
     * @return the isExecSchd
     */
    public Boolean getIsExecSchd() {
        return IsExecSchd;
    }

    /**
     * @param isExecSchd the isExecSchd to set
     */
    public void setIsExecSchd(Boolean isExecSchd) {
        IsExecSchd = isExecSchd;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Schedules [ScheId=" + ScheId + ", ScheName=" + ScheName + ", Priority=" + Priority + ", TaskNum="
                + TaskNum + ", Description=" + Description + ", IsExecSchd=" + IsExecSchd + "]";
    }

}
