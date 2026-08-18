package com.projectanalytics.synchronization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OAuthConnectPendingRepository extends JpaRepository<OAuthConnectPendingEntity, UUID> {

    Optional<OAuthConnectPendingEntity> findByStateToken(String stateToken);

    @Modifying(clearAutomatically = true)
    @Query("delete from OAuthConnectPendingEntity e where e.expiresAtEpoch < :cutoffEpoch")
    int deleteExpired(@Param("cutoffEpoch") long cutoffEpoch);
}
