import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { RecommendationApiService } from './recommendation-api.service';
import { ConfigurationService } from './configuration.service';

describe('RecommendationApiService', () => {
  let service: RecommendationApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        RecommendationApiService,
        { provide: ConfigurationService, useValue: { apiBaseUrl: '/api/v1' } },
      ],
    });
    service = TestBed.inject(RecommendationApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads project recommendations', () => {
    service.getProjectRecommendations('p1').subscribe((bundle) => {
      expect(bundle.scopeType).toBe('PROJECT');
      expect(bundle.recommendations.length).toBe(1);
    });
    const req = httpMock.expectOne('/api/v1/projects/p1/recommendations');
    expect(req.request.method).toBe('GET');
    req.flush({
      success: true,
      data: {
        scopeId: 'p1',
        scopeType: 'PROJECT',
        scopeName: 'Demo',
        executiveSummary: '1 recommendation',
        recommendations: [
          {
            id: 'r1',
            projectId: 'p1',
            projectName: 'Demo',
            analyticsId: 'a1',
            ruleCode: 'CRITICAL_HEALTH',
            title: 'Fix health',
            description: 'desc',
            severity: 'CRITICAL',
            explanation: 'why',
            suggestedAction: 'act',
            priorityRank: 1,
            supportingMetrics: [],
            generatedAt: '2026-01-01T00:00:00Z',
          },
        ],
      },
      timestamp: '2026-01-01T00:00:00Z',
    });
  });
});
