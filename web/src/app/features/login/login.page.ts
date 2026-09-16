import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <aside class="auth-hero">
        <div class="brand">
          <div class="brand-mark" aria-hidden="true">KP</div>
          <span class="brand-name">KitchenPulse</span>
        </div>
        <h1>Run the floor and the kitchen from one pulse.</h1>
        <p class="tagline">
          A microservices demo for orders, inventory, and live kitchen tickets — built for
          interviews and real-world architecture conversations.
        </p>
        <div class="auth-stats">
          <div class="auth-stat">
            <span>Stack</span>
            <strong>Angular · Spring · Kafka</strong>
          </div>
          <div class="auth-stat">
            <span>Flow</span>
            <strong>POS → Events → Board</strong>
          </div>
        </div>
      </aside>
      <section class="auth-panel-wrap">
        <div class="auth-panel">
          <h2>Welcome back</h2>
          <p class="sub">Sign in to open the POS and kitchen board.</p>
          <form class="panel" [formGroup]="form" (ngSubmit)="submit()">
            <div class="field">
              <label for="email">Email</label>
              <input id="email" type="email" autocomplete="email" formControlName="email" />
            </div>
            <div class="field">
              <label for="password">Password</label>
              <input id="password" type="password" autocomplete="current-password" formControlName="password" />
            </div>
            <button class="btn-primary btn-block" type="submit" [disabled]="pending()">
              {{ pending() ? 'Signing in…' : 'Sign in' }}
            </button>
            @if (error()) {
              <p class="error">{{ error() }}</p>
            }
          </form>
          <p class="auth-footer">
            New here? <a routerLink="/register">Create an account</a>
          </p>
          <div class="demo-hint">
            <strong>Demo chef account</strong><br />
            <code>chef&#64;kitchenpulse.dev</code> · <code>chef12345</code>
          </div>
        </div>
      </section>
    </div>
  `
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  readonly error = signal<string | null>(null);
  readonly pending = signal(false);
  readonly form = this.fb.nonNullable.group({
    email: ['chef@kitchenpulse.dev', [Validators.required, Validators.email]],
    password: ['chef12345', [Validators.required]]
  });

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    this.error.set(null);
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => {
        this.pending.set(false);
        void this.router.navigateByUrl('/orders');
      },
      error: (err) => {
        this.pending.set(false);
        this.error.set(err?.error?.message ?? 'Login failed');
      }
    });
  }
}
