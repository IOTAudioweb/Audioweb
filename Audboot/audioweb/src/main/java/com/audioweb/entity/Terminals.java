/**
 *
 */
package com.audioweb.entity;

import java.util.Date;

/**
 * @author shuofang
 * 终端
 */
public class Terminals {
    private String TIDString;//终端编号
    private String TName;//终端名称
    private String TIP;//终端IP地址
    private String castType;//广播类型
    private Boolean ISCMIC = false;//寻呼话筒
    private Boolean ISAutoCast = false;//自动采播
    private Boolean isCast = false;//是否广播
    private Boolean isOnline = false;//是否在线
    private String DomainId;//终端所属分区
    private String Number;//批量添加终端数目
    private int TNameId = -1;//终端名称编号
    private String DomainName;//终端所属分区名字
    private String Precinct;//终端管理区
    private Date FinalOfflineDate;//终端最后离线时间

    public String getTIDString() {
        return TIDString;
    }

    public void setTIDString(String tIDString) {
        TIDString = tIDString;
    }

    public String getTName() {
        return TName;
    }

    public void setTName(String tName) {
        TName = tName;
    }

    public String getTIP() {
        return TIP;
    }

    public void setTIP(String tIP) {
        TIP = tIP;
    }

    public Boolean getISCMIC() {
        return ISCMIC;
    }

    public void setISCMIC(Boolean iSCMIC) {
        ISCMIC = iSCMIC;
    }

    public Boolean getISAutoCast() {
        return ISAutoCast;
    }

    public void setISAutoCast(Boolean iSAutoCast) {
        ISAutoCast = iSAutoCast;
    }

    public String getDomainId() {
        return DomainId;
    }

    public void setDomainId(String domainId) {
        DomainId = domainId;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    /**
     * @return the tNameId
     */
    public int getTNameId() {
        return TNameId;
    }

    /**
     * @param tNameId the tNameId to set
     */
    public void setTNameId(int tNameId) {
        TNameId = tNameId;
    }

    public String getDomainName() {
        return DomainName;
    }

    public void setDomainName(String domainName) {
        DomainName = domainName;
    }

    /**
     * @return the finalOfflineDate
     */
    public Date getFinalOfflineDate() {
        return FinalOfflineDate;
    }

    /**
     * @param finalOfflineDate the finalOfflineDate to set
     */
    public void setFinalOfflineDate(Date finalOfflineDate) {
        FinalOfflineDate = finalOfflineDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Terminals bean = (Terminals) o;

        return TIDString.equals(bean.TIDString);
    }

    /**
     * @return the castType
     */
    public String getCastType() {
        return castType;
    }

    /**
     * @param castType the castType to set
     */
    public void setCastType(String castType) {
        this.castType = castType;
    }

    /**
     * @return the isCast
     */
    public Boolean getIsCast() {
        return isCast;
    }

    /**
     * @param isCast the isCast to set
     */
    public void setIsCast(Boolean isCast) {
        this.isCast = isCast;
    }

    public String getPrecinct() {
        return Precinct;
    }

    public void setPrecinct(String precinct) {
        Precinct = precinct;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }
}
