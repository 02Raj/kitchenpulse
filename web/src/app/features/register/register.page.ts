import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <aside class="auth-hero">
        <div class="brand">
          <div class="brand-mark" aria-hidden="true">KP</div>
          <span class="brand-name">KitchenPulse</span>
        </div>
        <h1>Join the line.</h1>
        <p class="tagline">
          Register a staff account, then place orders from the menu and watch tickets appear on the
          kitchen board in near real time.
        </p>
      </aside>
      <section class="auth-panel-wrap">
        <div class="auth-panel">
          <h2>Create account</h2>
          <p class="sub">Use a work email and a password of at least 8 characters.</p>
          <form class="panel" [formGroup]="form" (ngSubmit)="submit()">
            <div class="field">
              <label for="reg-email">Email</label>
              <input id="reg-email" type="email" autocomplete="email" formControlName="email" />
            </div>
            <div class="field">
              <label for="reg-password">Password</label>
              <input id="reg-password" type="password" autocomplete="new-password" formControlName="password" />
            </div>
            <button class="btn-primary btn-block" type="submit" [disabled]="pending()">
              {{ pending() ? 'Creating…' : 'Create account' }}
            </button>
            @if (error()) {
              <p class="error">{{ error() }}</p>
            }
          </form>
          <p class="auth-footer">
            Already have access? <a routerLink="/login">Sign in</a>
          </p>
        </div>
      </section>
    </div>
  `
})
export class RegisterPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  readonly error = signal<string | null>(null);
  readonly pending = signal(false);
  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    const { email, password } = this.form.getRawValue();
    this.auth.register(email, password).subscribe({
      next: () => {
        this.pending.set(false);
        void this.router.navigateByUrl('/orders');
      },
      error: (err) => {
        this.pending.set(false);
        this.error.set(err?.error?.message ?? 'Register failed');
      }
    });
  }
}
