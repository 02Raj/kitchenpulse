import { Component, inject, resource, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { firstValueFrom, timer } from 'rxjs';
import { finalize, switchMap, take, takeWhile } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

interface MenuItem {
  sku: string;
  name: string;
  station: string;
  stock: number;
}

interface OrderResponse {
  id: string;
  status: string;
  station: string;
}

interface KitchenTicket {
  orderId: string;
}

@Component({
  selector: 'app-orders-page',
  imports: [RouterLink],
  template: `
    <header class="page-header">
      <h1>Point of sale</h1>
      <p class="lead">Pick items from the menu — stock updates after each order and the kitchen gets a ticket automatically.</p>
    </header>

    @if (menu.isLoading()) {
      <div class="skeleton-grid" aria-busy="true">
        @for (i of [1, 2, 3, 4, 5, 6]; track i) {
          <div class="skeleton-card"></div>
        }
      </div>
    } @else if (menu.error()) {
      <div class="toast error">Could not load the menu. Check that the gateway and inventory service are running.</div>
    } @else if ((menu.value() ?? []).length === 0) {
      <div class="empty-state">
        <strong>Menu is empty</strong>
        Seed inventory or restart the inventory service to load demo items.
      </div>
    } @else {
      <div class="grid">
        @for (item of menu.value() ?? []; track item.sku) {
          <article class="panel menu-card">
            <div class="menu-card__top">
              <span [class]="stationBadgeClass(item.station)">{{ item.station }}</span>
              <span class="menu-card__sku">{{ item.sku }}</span>
            </div>
            <h2>{{ item.name }}</h2>
            <p class="stock" [class.stock--low]="item.stock <= 3" [class.stock--ok]="item.stock > 3">
              {{ item.stock <= 3 ? 'Low stock' : 'In stock' }} · {{ item.stock }} left
            </p>
            <div class="menu-card__actions">
              <button
                class="btn-primary"
                type="button"
                (click)="order(item)"
                [disabled]="isOrdering(item.sku) || item.stock < 1"
              >
                @if (item.stock < 1) {
                  Sold out
                } @else if (isOrdering(item.sku)) {
                  Sending to kitchen…
                } @else {
                  Add to kitchen
                }
              </button>
            </div>
          </article>
        }
      </div>
    }

    @if (message()) {
      <div class="toast">
        {{ message() }}
        @if (lastTicketId()) {
          <div class="toast-actions">
            <a
              class="btn-secondary toast-link"
              [routerLink]="['/kitchen']"
              [queryParams]="{ highlight: lastTicketId() }"
            >
              Open kitchen ticket
            </a>
          </div>
        }
      </div>
    }
    @if (error()) {
      <div class="toast error">{{ error() }}</div>
    }
  `
})
export class OrdersPage {
  private readonly http = inject(HttpClient);
  readonly orderingSku = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly lastTicketId = signal<string | null>(null);

  readonly menu = resource({
    loader: () => firstValueFrom(this.http.get<MenuItem[]>(`${environment.apiUrl}/api/inventory/items`))
  });

  isOrdering(sku: string): boolean {
    return this.orderingSku() === sku;
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

  order(item: MenuItem) {
    this.orderingSku.set(item.sku);
    this.error.set(null);
    this.message.set(null);
    this.lastTicketId.set(null);
    this.http
      .post<OrderResponse>(
        `${environment.apiUrl}/api/orders`,
        {
          station: item.station,
          items: [{ sku: item.sku, quantity: 1, name: item.name }]
        },
        {
          headers: { 'Idempotency-Key': crypto.randomUUID() }
        }
      )
      .subscribe({
        next: (order) => {
          this.menu.reload();
          this.waitForKitchenTicket(order.id, item.name);
        },
        error: (err) => {
          this.orderingSku.set(null);
          this.error.set(err?.error?.message ?? 'Order failed');
        }
      });
  }

  private waitForKitchenTicket(orderId: string, itemName: string) {
    timer(0, 600)
      .pipe(
        take(20),
        switchMap(() => this.http.get<KitchenTicket[]>(`${environment.apiUrl}/api/kitchen/tickets`)),
        takeWhile((tickets) => {
          const found = tickets.some((t) => t.orderId === orderId);
          if (found) {
            this.orderingSku.set(null);
            this.lastTicketId.set(orderId);
            this.message.set(
              `“${itemName}” is on the kitchen board — ticket ${this.shortId(orderId)} is live.`
            );
          }
          return !found;
        }, true),
        finalize(() => {
          if (this.orderingSku() === null) {
            return;
          }
          this.orderingSku.set(null);
          this.lastTicketId.set(orderId);
          this.message.set(
            `Order placed. Kafka may still be syncing — check the kitchen board shortly (ticket ${this.shortId(orderId)}).`
          );
        })
      )
      .subscribe({
        error: () => {
          this.orderingSku.set(null);
          this.lastTicketId.set(orderId);
          this.message.set(
            `Order accepted. Open the kitchen board — the ticket should appear within a few seconds.`
          );
        }
      });
  }

  private shortId(id: string): string {
    return id.length > 8 ? id.slice(0, 8) : id;
  }
}
