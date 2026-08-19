package com.projectanalytics.authentication.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationTokenEntity, UUID> {

    Optional<EmailConfirmationTokenEntity> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("delete from EmailConfirmationTokenEntity t where t.userId = :userId and t.usedAt is null")
    void deleteUnusedByUserId(@Param("userId") UUID userId);
}
