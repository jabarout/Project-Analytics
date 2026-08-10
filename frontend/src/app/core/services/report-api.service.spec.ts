import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ReportApiService } from './report-api.service';
import { ConfigurationService } from './configuration.service';

describe('ReportApiService', () => {
  let service: ReportApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ReportApiService,
        {
          provide: ConfigurationService,
          useValue: { apiBaseUrl: '/api/v1' },
        },
      ],
    });
    service = TestBed.inject(ReportApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('posts generate report requests', () => {
    service
      .generate({ reportType: 'EXECUTIVE', format: 'PDF' })
      .subscribe((report) => {
        expect(report.reportType).toBe('EXECUTIVE');
      });

    const req = httpMock.expectOne('/api/v1/reports');
    expect(req.request.method).toBe('POST');
    req.flush({
      success: true,
      data: {
        id: 'r1',
        title: 'Executive Report',
        reportType: 'EXECUTIVE',
        format: 'PDF',
        status: 'COMPLETED',
        scopeType: null,
        scopeId: null,
        generatedBy: 'u1',
        fileName: 'exec.pdf',
        contentType: 'application/pdf',
        fileSizeBytes: 10,
        errorMessage: null,
        generatedAt: '2026-01-01T00:00:00Z',
        createdAt: '2026-01-01T00:00:00Z',
      },
      timestamp: '2026-01-01T00:00:00Z',
    });
  });

  it('lists report history', () => {
    service.listHistory().subscribe((items) => {
      expect(items.length).toBe(1);
    });
    const req = httpMock.expectOne('/api/v1/reports');
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, data: [{ id: 'r1' }], timestamp: '2026-01-01T00:00:00Z' });
  });
});
