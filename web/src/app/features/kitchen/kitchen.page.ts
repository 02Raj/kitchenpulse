import { DatePipe } from '@angular/common';
import { Component, inject, OnDestroy, resource } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
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
  imports: [DatePipe],
  template: `
    <header class="page-header">
      <div class="row">
        <div>
          <h1>Kitchen board</h1>
          <p class="lead">Live tickets from new orders. Updates automatically every few seconds.</p>
        </div>
        <button class="btn-secondary" type="button" (click)="tickets.reload()" [disabled]="tickets.isLoading()">
          {{ tickets.isLoading() ? 'Refreshing…' : 'Refresh now' }}
        </button>
      </div>
    </header>

    @if (tickets.isLoading() && !tickets.hasValue()) {
      <div class="skeleton-grid" aria-busy="true">
        @for (i of [1, 2, 3]; track i) {
          <div class="skeleton-card"></div>
        }
      </div>
    } @else if (tickets.error()) {
      <div class="toast error">
        Could not load tickets. Sign in again if your session expired, and ensure notify-service and Kafka are running.
      </div>
    } @else if ((tickets.value() ?? []).length === 0) {
      <div class="empty-state">
        <strong>No tickets yet</strong>
        Place an order from the POS — it usually appears here within a second after Kafka publishes the event.
      </div>
    } @else {
      <div class="grid">
        @for (ticket of tickets.value() ?? []; track ticket.orderId) {
          <article class="panel ticket-card" [class.ticket-card--highlight]="ticket.orderId === highlightId">
            <div class="ticket-meta">
              <span [class]="stationBadgeClass(ticket.station)">{{ ticket.station }}</span>
              <span [class]="statusBadgeClass(ticket.status)">{{ ticket.status }}</span>
              <span class="muted">{{ ticket.createdAt | date: 'short' }}</span>
            </div>
            <h2 class="ticket-id" [title]="ticket.orderId">{{ shortId(ticket.orderId) }}</h2>
            <ul class="ticket-items">
              @for (line of itemLines(ticket.itemsJson); track line) {
                <li>{{ line }}</li>
              }
            </ul>
          </article>
        }
      </div>
    }
  `
})
export class KitchenPage implements OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly pollTimer = setInterval(() => this.tickets.reload(), 2500);

  readonly highlightId = this.route.snapshot.queryParamMap.get('highlight');

  readonly tickets = resource({
    loader: () => firstValueFrom(this.http.get<Ticket[]>(`${environment.apiUrl}/api/kitchen/tickets`))
  });

  ngOnDestroy(): void {
    clearInterval(this.pollTimer);
  }

  shortId(id: string): string {
    return id.length > 12 ? `${id.slice(0, 8)}…` : id;
  }

  stationBadgeClass(station: string): string {
    const key = station.toLowerCase();
    if (key.includes('grill') || key.includes('hot')) {
      return 'badge badge--grill';
    }
    if (key.includes('salad') || key.includes('cold')) {
      return 'badge badge--salad';
    }
    if (key.includes('dessert') || key.includes('sweet')) {
      return 'badge badge--dessert';
    }
    return 'badge';
  }

  statusBadgeClass(status: string): string {
    const key = status.toLowerCase();
    if (key.includes('done') || key.includes('complete')) {
      return 'badge badge--status-done';
    }
    return 'badge badge--status-new';
  }

  itemLines(itemsJson: string): string[] {
    try {
      const parsed = JSON.parse(itemsJson) as unknown;
      if (Array.isArray(parsed)) {
        return parsed.map((entry) => {
          if (typeof entry === 'string') {
            return entry;
          }
          if (entry && typeof entry === 'object') {
            const row = entry as Record<string, unknown>;
            const name = row['name'] ?? row['sku'] ?? 'Item';
            const qty = row['quantity'];
            return qty != null ? `${qty}× ${name}` : String(name);
          }
          return String(entry);
        });
      }
    } catch {
      /* fall through */
    }
    return [itemsJson];
  }
}
