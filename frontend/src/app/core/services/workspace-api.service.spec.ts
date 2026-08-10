import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ConfigurationService } from './configuration.service';
import { WorkspaceApiService } from './workspace-api.service';

describe('WorkspaceApiService', () => {
  let service: WorkspaceApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), WorkspaceApiService, ConfigurationService],
    });
    service = TestBed.inject(WorkspaceApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists workspaces from the standard envelope', () => {
    let result: unknown;
    service.listWorkspaces().subscribe((data) => {
      result = data;
    });

    const request = httpMock.expectOne((req) => req.url.endsWith('/workspaces'));
    request.flush({
      success: true,
      data: [
        {
          id: 'w1',
          name: 'Main',
          baseUrl: 'https://op.example',
          version: '14',
          synchronizationStatus: 'SUCCESS',
          createdAt: '2026-07-30T00:00:00Z',
          updatedAt: '2026-07-30T00:00:00Z',
        },
      ],
      timestamp: '2026-07-30T00:00:00Z',
    });

    expect(result).toHaveLength(1);
  });
});
