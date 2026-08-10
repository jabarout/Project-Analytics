package com.projectanalytics.analytics.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable scoring weights and thresholds (Analytics Engine — externalize, do not hard-code).
 */
@ConfigurationProperties(prefix = "projectanalytics.analytics")
public class AnalyticsScoringProperties {

    private boolean healthScoreEnabled = true;
    private boolean riskScoreEnabled = true;
    private boolean attentionScoreEnabled = true;

    private double healthScheduleWeight = 0.35;
    private double healthDeliveryWeight = 0.35;
    private double healthOverdueWeight = 0.30;

    private double riskOverdueWorkPackageWeight = 0.40;
    private double riskScheduleWeight = 0.35;
    private double riskCompletionWeight = 0.25;

    private double attentionHealthWeight = 0.45;
    private double attentionRiskWeight = 0.40;
    private double attentionOverdueWeight = 0.15;

    /**
     * Delete historical analytics snapshots older than this many days (when purge enabled).
     * Latest analytics row per project is never purged (separate table).
     */
    private int snapshotRetentionDays = 90;

    /**
     * When true, scheduled purge removes snapshots older than {@link #snapshotRetentionDays}.
     */
    private boolean snapshotPurgeEnabled = true;

    public boolean isHealthScoreEnabled() {
        return healthScoreEnabled;
    }

    public void setHealthScoreEnabled(boolean healthScoreEnabled) {
        this.healthScoreEnabled = healthScoreEnabled;
    }

    public boolean isRiskScoreEnabled() {
        return riskScoreEnabled;
    }

    public void setRiskScoreEnabled(boolean riskScoreEnabled) {
        this.riskScoreEnabled = riskScoreEnabled;
    }

    public boolean isAttentionScoreEnabled() {
        return attentionScoreEnabled;
    }

    public void setAttentionScoreEnabled(boolean attentionScoreEnabled) {
        this.attentionScoreEnabled = attentionScoreEnabled;
    }

    public double getHealthScheduleWeight() {
        return healthScheduleWeight;
    }

    public void setHealthScheduleWeight(double healthScheduleWeight) {
        this.healthScheduleWeight = healthScheduleWeight;
    }

    public double getHealthDeliveryWeight() {
        return healthDeliveryWeight;
    }

    public void setHealthDeliveryWeight(double healthDeliveryWeight) {
        this.healthDeliveryWeight = healthDeliveryWeight;
    }

    public double getHealthOverdueWeight() {
        return healthOverdueWeight;
    }

    public void setHealthOverdueWeight(double healthOverdueWeight) {
        this.healthOverdueWeight = healthOverdueWeight;
    }

    public double getRiskOverdueWorkPackageWeight() {
        return riskOverdueWorkPackageWeight;
    }

    public void setRiskOverdueWorkPackageWeight(double riskOverdueWorkPackageWeight) {
        this.riskOverdueWorkPackageWeight = riskOverdueWorkPackageWeight;
    }

    public double getRiskScheduleWeight() {
        return riskScheduleWeight;
    }

    public void setRiskScheduleWeight(double riskScheduleWeight) {
        this.riskScheduleWeight = riskScheduleWeight;
    }

    public double getRiskCompletionWeight() {
        return riskCompletionWeight;
    }

    public void setRiskCompletionWeight(double riskCompletionWeight) {
        this.riskCompletionWeight = riskCompletionWeight;
    }

    public double getAttentionHealthWeight() {
        return attentionHealthWeight;
    }

    public void setAttentionHealthWeight(double attentionHealthWeight) {
        this.attentionHealthWeight = attentionHealthWeight;
    }

    public double getAttentionRiskWeight() {
        return attentionRiskWeight;
    }

    public void setAttentionRiskWeight(double attentionRiskWeight) {
        this.attentionRiskWeight = attentionRiskWeight;
    }

    public double getAttentionOverdueWeight() {
        return attentionOverdueWeight;
    }

    public void setAttentionOverdueWeight(double attentionOverdueWeight) {
        this.attentionOverdueWeight = attentionOverdueWeight;
    }

    public int getSnapshotRetentionDays() {
        return snapshotRetentionDays;
    }

    public void setSnapshotRetentionDays(int snapshotRetentionDays) {
        this.snapshotRetentionDays = snapshotRetentionDays;
    }

    public boolean isSnapshotPurgeEnabled() {
        return snapshotPurgeEnabled;
    }

    public void setSnapshotPurgeEnabled(boolean snapshotPurgeEnabled) {
        this.snapshotPurgeEnabled = snapshotPurgeEnabled;
    }
}
