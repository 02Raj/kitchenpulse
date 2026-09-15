import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-shell-page',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="layout row">
      <strong>KitchenPulse</strong>
      <a routerLink="/orders" routerLinkActive="active">POS</a>
      <a routerLink="/kitchen" routerLinkActive="active">Kitchen board</a>
      <span class="muted">{{ auth.email() }}</span>
      <button class="ghost" type="button" (click)="auth.logout()">Logout</button>
    </header>
    <router-outlet />
  `
})
export class ShellPage {
  readonly auth = inject(AuthService);
}
