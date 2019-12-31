/**
 *
 */
package com.audioweb.entity;

/**
 * @author shuofang
 * 分区
 */
public class Domains implements Comparable<Domains> {
    private int DomainId;
    private int ParentDomainId;
    private String DomainName;

    private String parentDomainName;

    /**
     * @return the domainId
     */
    public int getDomainId() {
        return DomainId;
    }

    /**
     * @param domainId the domainId to set
     */
    public void setDomainId(int domainId) {
        DomainId = domainId;
    }

    /**
     * @return the domainName
     */
    public String getDomainName() {
        return DomainName;
    }

    /**
     * @param domainName the domainName to set
     */
    public void setDomainName(String domainName) {
        DomainName = domainName;
    }

    public int getParentDomainId() {
        return ParentDomainId;
    }

    public void setParentDomainId(int parentDomainId) {
        ParentDomainId = parentDomainId;
    }

    public String getParentDomainName() {
        return parentDomainName;
    }

    public void setParentDomainName(String parentDomainName) {
        this.parentDomainName = parentDomainName;
    }

    @Override
    public int compareTo(Domains o) {
        //排序
        int i = indexnum(this.getDomainId()) - indexnum(o.getDomainId());
        return i;
    }

    private int indexnum(int num) {
        while (num > 100) {
            num = num / 100;
        }
        return num;
    }
}
