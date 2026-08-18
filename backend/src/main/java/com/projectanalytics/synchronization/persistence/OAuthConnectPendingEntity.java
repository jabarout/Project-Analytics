package com.projectanalytics.synchronization.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oauth_connect_pending")
public class OAuthConnectPendingEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "state_token", nullable = false, unique = true, length = 64)
    private String stateToken;

    @Column(name = "code_verifier", nullable = false, length = 128)
    private String codeVerifier;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(name = "workspace_name", length = 200)
    private String workspaceName;

    @Column(name = "oauth_client_id", nullable = false, length = 200)
    private String oauthClientId;

    @Column(name = "oauth_client_secret_ciphertext", nullable = false, columnDefinition = "TEXT")
    private String oauthClientSecretCiphertext;

    /** UTC epoch seconds — avoids H2 timestamptz Instant quirks. */
    @Column(name = "expires_at_epoch", nullable = false)
    private long expiresAtEpoch;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OAuthConnectPendingEntity() {
    }

    public OAuthConnectPendingEntity(
            String stateToken,
            String codeVerifier,
            UUID userId,
            String baseUrl,
            String workspaceName,
            String oauthClientId,
            String oauthClientSecretCiphertext,
            Instant expiresAt
    ) {
        this.stateToken = stateToken;
        this.codeVerifier = codeVerifier;
        this.userId = userId;
        this.baseUrl = baseUrl;
        this.workspaceName = workspaceName;
        this.oauthClientId = oauthClientId;
        this.oauthClientSecretCiphertext = oauthClientSecretCiphertext;
        this.expiresAtEpoch = expiresAt.getEpochSecond();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getStateToken() {
        return stateToken;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getOauthClientSecretCiphertext() {
        return oauthClientSecretCiphertext;
    }

    public Instant getExpiresAt() {
        return Instant.ofEpochSecond(expiresAtEpoch);
    }

    public long getExpiresAtEpoch() {
        return expiresAtEpoch;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired(Instant now) {
        return expiresAtEpoch < now.getEpochSecond();
    }
}
