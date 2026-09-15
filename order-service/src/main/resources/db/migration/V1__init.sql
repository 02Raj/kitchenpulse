create table if not exists app_users (
    id uuid primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    created_at timestamptz not null default now()
);

create table if not exists kitchen_orders (
    id uuid primary key,
    user_id uuid not null references app_users (id),
    idempotency_key varchar(128) not null unique,
    status varchar(32) not null,
    station varchar(32) not null,
    items_json jsonb not null,
    created_at timestamptz not null default now()
);

create table if not exists outbox_events (
    id uuid primary key,
    aggregate_id uuid not null,
    topic varchar(128) not null,
    payload jsonb not null,
    published_at timestamptz,
    created_at timestamptz not null default now()
);

create index if not exists idx_outbox_unpublished on outbox_events (created_at) where published_at is null;
