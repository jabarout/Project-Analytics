import { authGuard } from './core/guards/auth.guard';
import { routes } from './app.routes';

describe('app routes', () => {
  it('exposes /oauth/complete as a public route (no authGuard)', () => {
    const complete = routes.find((route) => route.path === 'oauth/complete');
    expect(complete).toBeTruthy();
    expect(complete?.canActivate).toBeUndefined();
    expect(complete?.canActivateChild).toBeUndefined();
  });

  it('keeps application pages including /workspaces behind authGuard', () => {
    const authed = routes.find((route) => route.path === '');
    expect(authed?.canActivate).toEqual([authGuard]);
    const workspaces = authed?.children?.find((route) => route.path === 'workspaces');
    expect(workspaces).toBeTruthy();
  });
});
