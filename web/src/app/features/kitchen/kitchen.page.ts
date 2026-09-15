import { Component, inject, resource, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';

interface Ticket {
  orderId: string;
  station: string;
  itemsJson: string;
  status: string;
  createdAt: string;
}

@Component({
  selector: 'app-kitchen-page',
  template: `
    <main class="layout">
      <div class="row">
        <h1>Kitchen board</h1>
        <button class="ghost" type="button" (click)="tickets.reload()">Refresh</button>
      </div>
      <p class="muted">Tickets land here after the notify-service consumes kitchen.orders.created.</p>
      <div class="grid">
        @for (ticket of tickets.value() ?? []; track ticket.orderId) {
          <article class="panel">
            <p class="muted">{{ ticket.station }} · {{ ticket.status }}</p>
            <h2>{{ ticket.orderId }}</h2>
            <pre>{{ ticket.itemsJson }}</pre>
          </article>
        }
      </div>
    </main>
  `
})
export class KitchenPage {
  private readonly http = inject(HttpClient);
  readonly now = signal(Date.now());

  readonly tickets = resource({
    request: () => this.now(),
    loader: () => firstValueFrom(this.http.get<Ticket[]>(`${environment.apiUrl}/api/kitchen/tickets`))
  });

  constructor() {
    setInterval(() => this.now.set(Date.now()), 2500);
  }
}
