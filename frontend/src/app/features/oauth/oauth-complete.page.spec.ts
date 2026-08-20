import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OAuthCompletePage } from './oauth-complete.page';

describe('OAuthCompletePage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    Object.defineProperty(window, 'opener', { value: null, configurable: true });
  });

  async function create(query: Record<string, string>): Promise<ComponentFixture<OAuthCompletePage>> {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [OAuthCompletePage],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(query) } },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(OAuthCompletePage);
    fixture.detectChanges();
    return fixture;
  }

  it('shows the success message and does not require login', async () => {
    const fixture = await create({ oauth: 'success' });
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Connected to OpenProject');
    expect(text).toContain('Connected via OpenProject OAuth');
    expect(text).toContain('Continue to Connections');
    expect(text.toLowerCase()).not.toContain('sign in');
  });

  it('shows the backend error message from the query string', async () => {
    const fixture = await create({
      oauth: 'error',
      message: 'OpenProject OAuth token request failed (HTTP 401).',
    });
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('OpenProject connection failed');
    expect(text).toContain('OpenProject OAuth token request failed (HTTP 401).');
  });

  it('posts the existing message contract to a same-origin opener and closes', async () => {
    const postMessage = vi.fn();
    Object.defineProperty(window, 'opener', {
      value: { closed: false, postMessage },
      configurable: true,
    });
    const close = vi.spyOn(window, 'close').mockImplementation(() => undefined);

    await create({ oauth: 'success' });

    expect(postMessage).toHaveBeenCalledTimes(1);
    expect(postMessage).toHaveBeenCalledWith(
      {
        source: 'pa-openproject-oauth',
        oauth: 'success',
        message:
          'Connected via OpenProject OAuth. Eligibility verified — you are Workspace Admin. Synchronize to load data.',
      },
      window.location.origin
    );
    expect(close).toHaveBeenCalled();
  });

  it('does not postMessage when there is no opener', async () => {
    Object.defineProperty(window, 'opener', { value: null, configurable: true });
    const close = vi.spyOn(window, 'close').mockImplementation(() => undefined);
    const fixture = await create({ oauth: 'error', message: 'OAuth state expired. Start connect again.' });
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('OAuth state expired. Start connect again.');
    expect(text).toContain('Continue to Connections');
    expect(close).not.toHaveBeenCalled();
  });
});
