import { Component } from '@angular/core';

@Component({
  selector: 'app-terms-page',
  standalone: true,
  template: `
    <article class="legal">
      <h1>Terms of use</h1>
      <p class="legal__lede">
        By using Project Analytics you agree to use it responsibly within your organisation’s
        policies and applicable law.
      </p>

      <h2>Service</h2>
      <p>
        Project Analytics shows decision-intelligence views over data synchronized from OpenProject.
        OpenProject remains the system of record for project work.
      </p>

      <h2>Accounts</h2>
      <p>
        Keep your credentials safe. Email confirmation may be required before sign-in.
      </p>

      <h2>Acceptable use</h2>
      <ul>
        <li>Do not access workspaces or data you are not authorized to use</li>
        <li>Do not abuse authentication, rate limits, or shared infrastructure</li>
      </ul>

      <h2>Scores &amp; recommendations</h2>
      <p>
        Analytics scores and recommendations are decision support only — not a substitute for
        professional judgment.
      </p>
    </article>
  `,
})
export class TermsPage {}
