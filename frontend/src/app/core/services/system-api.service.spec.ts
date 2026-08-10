import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SystemApiService } from './system-api.service';
import { ConfigurationService } from './configuration.service';

describe('SystemApiService', () => {
  let service: SystemApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), SystemApiService, ConfigurationService],
    });
    service = TestBed.inject(SystemApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('maps system info from the standard API envelope', () => {
    let result: unknown;
    service.getSystemInfo().subscribe((data) => {
      result = data;
    });

    const request = httpMock.expectOne((req) => req.url.endsWith('/system/info'));
    expect(request.request.method).toBe('GET');
    request.flush({
      success: true,
      data: {
        application: 'project-analytics-backend',
        version: '1.0.0',
        environment: 'test',
        apiVersion: 'v1',
      },
      timestamp: '2026-07-30T00:00:00Z',
    });

    expect(result).toEqual({
      application: 'project-analytics-backend',
      version: '1.0.0',
      environment: 'test',
      apiVersion: 'v1',
    });
  });
});
