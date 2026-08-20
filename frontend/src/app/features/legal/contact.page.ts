import { Component } from '@angular/core';

@Component({
  selector: 'app-contact-page',
  standalone: true,
  template: `
    <article class="legal">
      <h1>Contact</h1>
      <p class="legal__lede">
        For product or deployment questions about this Project Analytics instance, email us.
      </p>

      <div class="legal__card">
        <h2>Email</h2>
        <p>
          <a class="legal__mail" href="mailto:projectanalytics.contact@gmail.com"
            >projectanalytics.contact&#64;gmail.com</a
          >
        </p>
      </div>

      <h2>Helpful to include</h2>
      <ul>
        <li>Your account email</li>
        <li>When the issue happened</li>
        <li>Workspace or OpenProject URL if relevant</li>
      </ul>
    </article>
  `,
})
export class ContactPage {}
