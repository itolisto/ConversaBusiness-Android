package ee.app.conversamanager.model.parse;

import java.util.Date;

/**
 * Created by edgargomez on 4/15/16.
 */
public class Account {

    private String username;
    private String email;
    private boolean emailVerified;
    private int userType;
    private boolean active;
    private Date createdAt;
    private Date modifiedAt;

    private String conversaId;
    private boolean verified;
    private String displayName;
    private String about;
    private int status;
    private int categoriesCount;
    private String planId;
    private String countryId;
    private String tags;
    private String avatarUrl;
    private Date lastStatus;

    public Account() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getConversaId() {
        return conversaId;
    }

    public void setConversaId(String conversaId) {
        this.conversaId = conversaId;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCategoriesCount() {
        return categoriesCount;
    }

    public void setCategoriesCount(int categoriesCount) {
        this.categoriesCount = categoriesCount;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(Date lastStatus) {
        this.lastStatus = lastStatus;
    }

}
