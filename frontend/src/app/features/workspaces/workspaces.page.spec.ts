import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { WorkspacesPage } from './workspaces.page';

describe('WorkspacesPage OAuth form', () => {
  let fixture: ComponentFixture<WorkspacesPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkspacesPage],
      providers: [
        provideRouter([]),
        {
          provide: WorkspaceApiService,
          useValue: {
            oauthStatus: () =>
              of({
                enabled: true,
                redirectUri: 'https://example.test/api/v1/workspaces/oauth/callback',
                globalClientDefaultsAvailable: false,
              }),
            listWorkspaces: () => of([]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WorkspacesPage);
    fixture.detectChanges();
  });

  it('does not present OAuth client fields as a username/password login form', () => {
    const form = fixture.nativeElement.querySelector('form.workspaces__form') as HTMLFormElement;
    const clientId = fixture.nativeElement.querySelector(
      'input[formControlName="clientId"]'
    ) as HTMLInputElement;
    const clientSecret = fixture.nativeElement.querySelector(
      'input[formControlName="clientSecret"]'
    ) as HTMLInputElement;

    expect(form.autocomplete).toBe('off');
    expect(clientId).toBeTruthy();
    expect(clientSecret).toBeTruthy();
    expect(clientSecret.type).toBe('text');
    expect(clientSecret.type).not.toBe('password');
    expect(clientSecret.autocomplete).toBe('off');
    expect(clientId.autocomplete).toBe('off');
    expect(clientSecret.getAttribute('name')).toBe('openproject-oauth-client-secret');
    expect(clientId.getAttribute('name')).toBe('openproject-oauth-client-id');
    expect(clientSecret.getAttribute('name')).not.toMatch(/password/i);
    expect(clientId.getAttribute('name')).not.toMatch(/user|email/i);
  });
});
