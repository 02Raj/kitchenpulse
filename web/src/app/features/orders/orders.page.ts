import { Component, inject, resource, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';

interface MenuItem {
  sku: string;
  name: string;
  station: string;
  stock: number;
}

@Component({
  selector: 'app-orders-page',
  template: `
    <main class="layout">
      <h1>Place order</h1>
      <p class="muted">Sync Feign reserve, then outbox to Kafka. Same Idempotency-Key returns the same order.</p>
      <div class="grid">
        @for (item of menu.value() ?? []; track item.sku) {
          <article class="panel">
            <h2>{{ item.name }}</h2>
            <p class="muted">{{ item.sku }} · {{ item.station }} · stock {{ item.stock }}</p>
            <button type="button" (click)="order(item)" [disabled]="pending()">Order 1</button>
          </article>
        }
      </div>
      @if (message()) { <p>{{ message() }}</p> }
      @if (error()) { <p class="error">{{ error() }}</p> }
    </main>
  `
})
export class OrdersPage {
  private readonly http = inject(HttpClient);
  readonly pending = signal(false);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly menu = resource({
    loader: () => firstValueFrom(this.http.get<MenuItem[]>(`${environment.apiUrl}/api/inventory/items`))
  });

  order(item: MenuItem) {
    this.pending.set(true);
    this.error.set(null);
    this.http
      .post(`${environment.apiUrl}/api/orders`, {
        station: item.station,
        items: [{ sku: item.sku, quantity: 1, name: item.name }]
      }, {
        headers: { 'Idempotency-Key': crypto.randomUUID() }
      })
      .subscribe({
        next: () => {
          this.pending.set(false);
          this.message.set(`Accepted ${item.name}. Check the kitchen board.`);
          this.menu.reload();
        },
        error: (err) => {
          this.pending.set(false);
          this.error.set(err?.error?.message ?? 'Order failed');
        }
      });
  }
}
