import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="layout">
      <p class="muted">KitchenPulse</p>
      <h1>Sign in</h1>
      <form class="panel" [formGroup]="form" (ngSubmit)="submit()">
        <label>Email <input formControlName="email" /></label>
        <p></p>
        <label>Password <input type="password" formControlName="password" /></label>
        <p></p>
        <button [disabled]="pending()">Login</button>
        @if (error()) { <p class="error">{{ error() }}</p> }
      </form>
      <p><a routerLink="/register">Create account</a></p>
      <p class="muted">Demo: chef@kitchenpulse.dev / chef12345</p>
    </main>
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
