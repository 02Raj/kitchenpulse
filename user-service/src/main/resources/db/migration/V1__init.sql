create table if not exists app_roles (
    id uuid primary key,
    name varchar(64) not null unique
);

create table if not exists app_users (
    id uuid primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    created_at timestamptz not null default now()
);

create table if not exists app_user_roles (
    user_id uuid not null references app_users (id) on delete cascade,
    role_id uuid not null references app_roles (id) on delete cascade,
    primary key (user_id, role_id)
);

insert into app_roles (id, name) values
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'STAFF'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'KITCHEN'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'ADMIN')
on conflict do nothing;
