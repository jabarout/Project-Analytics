package com.projectanalytics.recommendation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<RecommendationEntity, UUID> {

    List<RecommendationEntity> findByProjectIdOrderByPriorityRankAscGeneratedAtDesc(UUID projectId);

    List<RecommendationEntity> findByProjectIdInOrderByPriorityRankAscGeneratedAtDesc(Collection<UUID> projectIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RecommendationEntity r where r.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
