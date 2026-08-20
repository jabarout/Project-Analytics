import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-privacy-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <article class="legal">
      <h1>Privacy</h1>
      <p class="legal__lede">
        How this Project Analytics instance handles account and project data.
      </p>

      <h2>What we store</h2>
      <ul>
        <li>Your account (email, username, password hash)</li>
        <li>Workspace connection details you configure</li>
        <li>Analytics derived from synchronized OpenProject data</li>
      </ul>

      <h2>How it’s used</h2>
      <ul>
        <li>To sign you in and show decision-intelligence views you request</li>
        <li>For transactional email when enabled (confirmation, password reset)</li>
      </ul>

      <h2>Your control</h2>
      <p>
        On a self-hosted deployment, the operator controls retention, backups, and deletion.
        Questions: <a routerLink="/contact">Contact</a>.
      </p>
    </article>
  `,
})
export class PrivacyPage {}
