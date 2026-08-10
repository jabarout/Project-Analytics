import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { ConfigurationService } from './configuration.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthService,
        ConfigurationService,
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('stores token after successful login', () => {
    service.login({ username: 'admin', password: 'Admin123!' }).subscribe();

    const request = httpMock.expectOne((req) => req.url.endsWith('/auth/login'));
    expect(request.request.method).toBe('POST');
    request.flush({
      success: true,
      data: {
        token: 'test-token',
        expiresAt: '2026-07-30T18:00:00Z',
      },
      timestamp: '2026-07-30T17:00:00Z',
    });

    expect(service.token()).toBe('test-token');
    expect(service.isAuthenticated()).toBe(true);
    expect(localStorage.getItem('pa.auth.token')).toBe('test-token');
  });
});
