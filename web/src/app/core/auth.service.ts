import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  roles: string[];
}

const TOKEN_KEY = 'kitchenpulse.token';
const EMAIL_KEY = 'kitchenpulse.email';
const ROLES_KEY = 'kitchenpulse.roles';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenState = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly emailState = signal<string | null>(localStorage.getItem(EMAIL_KEY));
  private readonly rolesState = signal<string[]>(this.readRoles());

  readonly token = this.tokenState.asReadonly();
  readonly email = this.emailState.asReadonly();
  readonly roles = this.rolesState.asReadonly();
  readonly loggedIn = computed(() => !!this.tokenState());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(email: string, password: string) {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/api/auth/login`, { email, password })
      .pipe(tap((res) => this.store(res)));
  }

  register(email: string, password: string) {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/api/auth/register`, { email, password })
      .pipe(tap((res) => this.store(res)));
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
    localStorage.removeItem(ROLES_KEY);
    this.tokenState.set(null);
    this.emailState.set(null);
    this.rolesState.set([]);
    void this.router.navigateByUrl('/login');
  }

  private store(res: AuthResponse) {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(EMAIL_KEY, res.email);
    localStorage.setItem(ROLES_KEY, JSON.stringify(res.roles ?? []));
    this.tokenState.set(res.token);
    this.emailState.set(res.email);
    this.rolesState.set(res.roles ?? []);
  }

  private readRoles(): string[] {
    try {
      return JSON.parse(localStorage.getItem(ROLES_KEY) ?? '[]') as string[];
    } catch {
      return [];
    }
  }
}
