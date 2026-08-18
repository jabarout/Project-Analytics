package com.projectanalytics.synchronization.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspace_credential")
public class WorkspaceCredentialEntity extends BaseEntity {

    @Column(name = "workspace_id", nullable = false, unique = true)
    private UUID workspaceId;

    @Column(name = "auth_scheme", nullable = false, length = 30)
    private String authScheme;

    @Column(name = "secret_ciphertext", nullable = false, columnDefinition = "TEXT")
    private String secretCiphertext;

    @Column(name = "refresh_ciphertext", columnDefinition = "TEXT")
    private String refreshCiphertext;

    /** OpenProject OAuth application client_id for this workspace (multi-OP). */
    @Column(name = "oauth_client_id", length = 200)
    private String oauthClientId;

    /** Encrypted OpenProject OAuth application client_secret. */
    @Column(name = "oauth_client_secret_ciphertext", columnDefinition = "TEXT")
    private String oauthClientSecretCiphertext;

    @Column(name = "openproject_user_id")
    private Long openProjectUserId;

    @Column(name = "openproject_login", length = 200)
    private String openProjectLogin;

    @Column(name = "openproject_email", length = 255)
    private String openProjectEmail;

    @Column(name = "openproject_admin")
    private Boolean openProjectAdmin;

    @Column(name = "eligibility_snapshot", length = 500)
    private String eligibilitySnapshot;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    protected WorkspaceCredentialEntity() {
    }

    public WorkspaceCredentialEntity(UUID workspaceId, String authScheme, String secretCiphertext, UUID createdByUserId) {
        this.workspaceId = workspaceId;
        this.authScheme = authScheme;
        this.secretCiphertext = secretCiphertext;
        this.createdByUserId = createdByUserId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getAuthScheme() {
        return authScheme;
    }

    public void setAuthScheme(String authScheme) {
        this.authScheme = authScheme;
    }

    public String getSecretCiphertext() {
        return secretCiphertext;
    }

    public void setSecretCiphertext(String secretCiphertext) {
        this.secretCiphertext = secretCiphertext;
    }

    public String getRefreshCiphertext() {
        return refreshCiphertext;
    }

    public void setRefreshCiphertext(String refreshCiphertext) {
        this.refreshCiphertext = refreshCiphertext;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public String getOauthClientSecretCiphertext() {
        return oauthClientSecretCiphertext;
    }

    public void setOauthClientSecretCiphertext(String oauthClientSecretCiphertext) {
        this.oauthClientSecretCiphertext = oauthClientSecretCiphertext;
    }

    public Long getOpenProjectUserId() {
        return openProjectUserId;
    }

    public void setOpenProjectUserId(Long openProjectUserId) {
        this.openProjectUserId = openProjectUserId;
    }

    public String getOpenProjectLogin() {
        return openProjectLogin;
    }

    public void setOpenProjectLogin(String openProjectLogin) {
        this.openProjectLogin = openProjectLogin;
    }

    public String getOpenProjectEmail() {
        return openProjectEmail;
    }

    public void setOpenProjectEmail(String openProjectEmail) {
        this.openProjectEmail = openProjectEmail;
    }

    public Boolean getOpenProjectAdmin() {
        return openProjectAdmin;
    }

    public void setOpenProjectAdmin(Boolean openProjectAdmin) {
        this.openProjectAdmin = openProjectAdmin;
    }

    public String getEligibilitySnapshot() {
        return eligibilitySnapshot;
    }

    public void setEligibilitySnapshot(String eligibilitySnapshot) {
        this.eligibilitySnapshot = eligibilitySnapshot;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(Instant tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }
}
