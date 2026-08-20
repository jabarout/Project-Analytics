import { Routes } from '@angular/router';
import { ContactPage } from './contact.page';
import { PrivacyPage } from './privacy.page';
import { TermsPage } from './terms.page';

export const LEGAL_ROUTES: Routes = [
  { path: 'privacy', component: PrivacyPage },
  { path: 'contact', component: ContactPage },
  { path: 'terms', component: TermsPage },
];
