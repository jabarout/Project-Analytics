package com.projectanalytics.authentication.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * JPA entity for user UI preferences.
 */
@Entity
@Table(name = "user_preference")
public class UserPreferenceEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "theme", nullable = false, length = 50)
    private String theme = "dark";

    @Column(name = "language", nullable = false, length = 20)
    private String language = "en";

    @Column(name = "dashboard_configuration")
    private String dashboardConfiguration;

    protected UserPreferenceEntity() {
    }

    public UserPreferenceEntity(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDashboardConfiguration() {
        return dashboardConfiguration;
    }

    public void setDashboardConfiguration(String dashboardConfiguration) {
        this.dashboardConfiguration = dashboardConfiguration;
    }
}
