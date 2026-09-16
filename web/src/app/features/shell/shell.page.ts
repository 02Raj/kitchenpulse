import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-shell-page',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <header class="topbar">
        <div class="brand">
          <div class="brand-mark" aria-hidden="true">KP</div>
          <span class="brand-name">KitchenPulse</span>
        </div>
        <nav class="topnav" aria-label="Main">
          <a class="nav-link" routerLink="/orders" routerLinkActive="active">Point of sale</a>
          <a class="nav-link" routerLink="/kitchen" routerLinkActive="active">Kitchen board</a>
        </nav>
        <div class="topbar-meta">
          <span class="user-chip" [title]="auth.email() ?? ''">
            {{ auth.email() }}
            @if (auth.roles().length) {
              · {{ auth.roles().join(', ') }}
            }
          </span>
          <button class="btn-ghost" type="button" (click)="auth.logout()">Sign out</button>
        </div>
      </header>
      <div class="app-body">
        <router-outlet />
      </div>
    </div>
  `
})
export class ShellPage {
  readonly auth = inject(AuthService);
}
