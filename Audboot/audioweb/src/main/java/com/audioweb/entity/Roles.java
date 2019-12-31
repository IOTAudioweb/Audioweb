package com.audioweb.entity;

public class Roles {
    private String RoleId;
    private String RoleName;
    private String RoleLevel;
    private String MenuRights;
    private String Description;
    private Boolean isuse;

    /**
     * @return the roleId
     */
    public String getRoleId() {
        return RoleId;
    }

    /**
     * @param roleId the roleId to set
     */
    public void setRoleId(String roleId) {
        RoleId = roleId;
    }

    public String getRoleName() {
        return RoleName;
    }

    public void setRoleName(String roleName) {
        RoleName = roleName;
    }

    public String getRoleLevel() {
        return RoleLevel;
    }

    public void setRoleLevel(String roleLevel) {
        RoleLevel = roleLevel;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getMenuRights() {
        return MenuRights;
    }

    public void setMenuRights(String menuRights) {
        MenuRights = menuRights;
    }

    public Boolean getIsuse() {
        return isuse;
    }

    public void setIsuse(Boolean isuse) {
        this.isuse = isuse;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Description == null) ? 0 : Description.hashCode());
        result = prime * result + ((MenuRights == null) ? 0 : MenuRights.hashCode());
        result = prime * result + ((RoleId == null) ? 0 : RoleId.hashCode());
        result = prime * result + ((RoleLevel == null) ? 0 : RoleLevel.hashCode());
        result = prime * result + ((RoleName == null) ? 0 : RoleName.hashCode());
        result = prime * result + ((isuse == null) ? 0 : isuse.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Roles other = (Roles) obj;
        if (Description == null) {
            if (other.Description != null)
                return false;
        } else if (!Description.equals(other.Description))
            return false;
        if (MenuRights == null) {
            if (other.MenuRights != null)
                return false;
        } else if (!MenuRights.equals(other.MenuRights))
            return false;
        if (RoleId == null) {
            if (other.RoleId != null)
                return false;
        } else if (!RoleId.equals(other.RoleId))
            return false;
        if (RoleLevel == null) {
            if (other.RoleLevel != null)
                return false;
        } else if (!RoleLevel.equals(other.RoleLevel))
            return false;
        if (RoleName == null) {
            if (other.RoleName != null)
                return false;
        } else if (!RoleName.equals(other.RoleName))
            return false;
        if (isuse == null) {
            if (other.isuse != null)
                return false;
        } else if (!isuse.equals(other.isuse))
            return false;
        return true;
    }

}
