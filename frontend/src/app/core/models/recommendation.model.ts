export type RecommendationSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export interface SupportingMetric {
  readonly code: string;
  readonly label: string;
  readonly value: string;
}

export interface Recommendation {
  readonly id: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly analyticsId: string;
  readonly ruleCode: string;
  readonly title: string;
  readonly description: string;
  readonly severity: RecommendationSeverity;
  readonly explanation: string;
  readonly suggestedAction: string | null;
  readonly priorityRank: number;
  readonly supportingMetrics: readonly SupportingMetric[];
  readonly generatedAt: string;
}

export interface RecommendationBundle {
  readonly scopeId: string | null;
  readonly scopeType: string;
  readonly scopeName: string;
  readonly executiveSummary: string;
  readonly recommendations: readonly Recommendation[];
}
