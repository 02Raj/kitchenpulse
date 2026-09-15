import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="layout">
      <h1>Register</h1>
      <form class="panel" [formGroup]="form" (ngSubmit)="submit()">
        <label>Email <input formControlName="email" /></label>
        <p></p>
        <label>Password <input type="password" formControlName="password" /></label>
        <p></p>
        <button [disabled]="pending()">Create</button>
        @if (error()) { <p class="error">{{ error() }}</p> }
      </form>
      <p><a routerLink="/login">Back to login</a></p>
    </main>
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
