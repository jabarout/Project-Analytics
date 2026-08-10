import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ConfigurationService } from './configuration.service';
import { DashboardApiService } from './dashboard-api.service';

describe('DashboardApiService', () => {
  let service: DashboardApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), DashboardApiService, ConfigurationService],
    });
    service = TestBed.inject(DashboardApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads executive dashboard envelope', () => {
    let result: unknown;
    service.getExecutiveDashboard().subscribe((data) => {
      result = data;
    });

    const request = httpMock.expectOne((req) => req.url.endsWith('/dashboards/executive'));
    request.flush({
      success: true,
      data: {
        workspaceCount: 1,
        portfolioCount: 1,
        totalProjects: 2,
        criticalProjects: 0,
        highAttentionProjects: 1,
        workspaces: [],
        topAttentionProjects: [],
        insights: ['ok'],
        workspaceKpis: [],
      },
      timestamp: '2026-07-31T00:00:00Z',
    });

    expect(result).toMatchObject({ workspaceCount: 1, totalProjects: 2 });
  });
});
