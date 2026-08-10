import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ConfigurationService } from './configuration.service';
import { PortfolioApiService } from './portfolio-api.service';

describe('PortfolioApiService', () => {
  let service: PortfolioApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), PortfolioApiService, ConfigurationService],
    });
    service = TestBed.inject(PortfolioApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists portfolios from the local API envelope', () => {
    let result: unknown;
    service.listPortfolios().subscribe((data) => {
      result = data;
    });

    const request = httpMock.expectOne((req) => req.url.endsWith('/portfolios'));
    expect(request.request.method).toBe('GET');
    request.flush({
      success: true,
      data: [
        {
          id: 'p1',
          workspaceId: 'w1',
          name: 'Core',
          description: null,
          healthScore: null,
          attentionScore: null,
          totalProjects: 2,
          activeProjects: 1,
        },
      ],
      timestamp: '2026-07-30T00:00:00Z',
    });

    expect(result).toMatchObject([{ name: 'Core', totalProjects: 2 }]);
  });
});
