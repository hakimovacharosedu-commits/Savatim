-- =====================================================================
-- Savatim — Supabase schema
-- Run this whole file once in: Supabase Dashboard → SQL Editor → New query → Run
-- =====================================================================

create extension if not exists pgcrypto;

-- ---------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------

create table if not exists public.profiles (
  id                  uuid primary key references auth.users(id) on delete cascade,
  name                text not null default '',
  role_label          text not null default 'other',   -- mom, dad, grandma, grandpa, sister, brother, daughter, son, wife, husband, other
  gender              text not null default 'f' check (gender in ('m','f')),
  language            text not null default 'uz' check (language in ('uz','ru','en')),
  notify_app          boolean not null default true,
  notify_telegram     boolean not null default false,
  telegram_chat_id    bigint,
  telegram_link_code  text unique,
  current_home_id     uuid,
  created_at          timestamptz not null default now()
);

create table if not exists public.homes (
  id           uuid primary key default gen_random_uuid(),
  name         text not null,
  invite_code  text not null unique,
  owner_id     uuid not null references public.profiles(id),
  created_at   timestamptz not null default now()
);

create table if not exists public.home_members (
  home_id    uuid not null references public.homes(id) on delete cascade,
  user_id    uuid not null references public.profiles(id) on delete cascade,
  role       text not null default 'member' check (role in ('owner','admin','member')),
  joined_at  timestamptz not null default now(),
  primary key (home_id, user_id)
);

create table if not exists public.items (
  id           uuid primary key default gen_random_uuid(),
  home_id      uuid not null references public.homes(id) on delete cascade,
  name         text not null,
  catalog_key  text,
  category     text not null default 'other',
  quantity     text not null default '',
  note         text not null default '',
  added_by     uuid references public.profiles(id) on delete set null,
  bought       boolean not null default false,
  bought_by    uuid references public.profiles(id) on delete set null,
  bought_at    timestamptz,
  created_at   timestamptz not null default now()
);
create index if not exists items_home_idx on public.items(home_id);

-- type: added | bought | removed | joined | left | all_bought
-- items: [{ "key": "carrot" | null, "name": "custom name", "uz": "...", "ru": "...", "en": "..." }]
create table if not exists public.activity (
  id          bigint generated always as identity primary key,
  home_id     uuid not null references public.homes(id) on delete cascade,
  actor_id    uuid references public.profiles(id) on delete set null,
  type        text not null,
  items       jsonb not null default '[]'::jsonb,
  created_at  timestamptz not null default now()
);
create index if not exists activity_home_idx on public.activity(home_id, id desc);

-- ---------------------------------------------------------------------
-- Helper functions (security definer → no RLS recursion)
-- ---------------------------------------------------------------------

create or replace function public.is_member(h uuid) returns boolean
language sql security definer stable set search_path = public as $$
  select exists(select 1 from home_members where home_id = h and user_id = auth.uid());
$$;

create or replace function public.member_role(h uuid) returns text
language sql security definer stable set search_path = public as $$
  select role from home_members where home_id = h and user_id = auth.uid();
$$;

create or replace function public.shares_home(other uuid) returns boolean
language sql security definer stable set search_path = public as $$
  select exists(
    select 1 from home_members a join home_members b on a.home_id = b.home_id
    where a.user_id = auth.uid() and b.user_id = other
  );
$$;

create or replace function public.gen_code(len int) returns text
language plpgsql volatile as $$
declare
  chars text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  result text := '';
begin
  for i in 1..len loop
    result := result || substr(chars, 1 + floor(random() * length(chars))::int, 1);
  end loop;
  return result;
end;
$$;

-- Create a profile automatically when someone signs up
create or replace function public.handle_new_user() returns trigger
language plpgsql security definer set search_path = public as $$
begin
  insert into public.profiles (id, name, language)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'name', ''),
    coalesce(nullif(new.raw_user_meta_data->>'language', ''), 'uz')
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------------
-- Row Level Security
-- ---------------------------------------------------------------------

alter table public.profiles     enable row level security;
alter table public.homes        enable row level security;
alter table public.home_members enable row level security;
alter table public.items        enable row level security;
alter table public.activity     enable row level security;

drop policy if exists profiles_select on public.profiles;
create policy profiles_select on public.profiles for select
  using (id = auth.uid() or public.shares_home(id));
drop policy if exists profiles_update on public.profiles;
create policy profiles_update on public.profiles for update
  using (id = auth.uid()) with check (id = auth.uid());

drop policy if exists homes_select on public.homes;
create policy homes_select on public.homes for select using (public.is_member(id));
drop policy if exists homes_update on public.homes;
create policy homes_update on public.homes for update
  using (public.member_role(id) in ('owner','admin'));

drop policy if exists members_select on public.home_members;
create policy members_select on public.home_members for select using (public.is_member(home_id));
drop policy if exists members_delete on public.home_members;
create policy members_delete on public.home_members for delete
  using (
    (public.member_role(home_id) = 'owner' and role <> 'owner')
    or (public.member_role(home_id) = 'admin' and role = 'member')
  );

drop policy if exists items_select on public.items;
create policy items_select on public.items for select using (public.is_member(home_id));
drop policy if exists items_insert on public.items;
create policy items_insert on public.items for insert
  with check (public.is_member(home_id) and added_by = auth.uid());
drop policy if exists items_update on public.items;
create policy items_update on public.items for update using (public.is_member(home_id));
drop policy if exists items_delete on public.items;
create policy items_delete on public.items for delete
  using (added_by = auth.uid() or public.member_role(home_id) in ('owner','admin'));

drop policy if exists activity_select on public.activity;
create policy activity_select on public.activity for select using (public.is_member(home_id));
drop policy if exists activity_insert on public.activity;
create policy activity_insert on public.activity for insert
  with check (public.is_member(home_id) and actor_id = auth.uid());

-- ---------------------------------------------------------------------
-- RPC functions called from the app
-- ---------------------------------------------------------------------

create or replace function public.create_home(p_name text) returns public.homes
language plpgsql security definer set search_path = public as $$
declare
  h homes;
  code text;
begin
  if auth.uid() is null then raise exception 'not_authenticated'; end if;
  loop
    code := gen_code(6);
    exit when not exists(select 1 from homes where invite_code = code);
  end loop;
  insert into homes(name, invite_code, owner_id) values (trim(p_name), code, auth.uid()) returning * into h;
  insert into home_members(home_id, user_id, role) values (h.id, auth.uid(), 'owner');
  update profiles set current_home_id = h.id where id = auth.uid();
  return h;
end;
$$;

create or replace function public.join_home(p_code text) returns public.homes
language plpgsql security definer set search_path = public as $$
declare
  h homes;
begin
  if auth.uid() is null then raise exception 'not_authenticated'; end if;
  select * into h from homes where invite_code = upper(trim(p_code));
  if h.id is null then raise exception 'invalid_code'; end if;
  if not exists(select 1 from home_members where home_id = h.id and user_id = auth.uid()) then
    insert into home_members(home_id, user_id, role) values (h.id, auth.uid(), 'member');
    insert into activity(home_id, actor_id, type) values (h.id, auth.uid(), 'joined');
  end if;
  update profiles set current_home_id = h.id where id = auth.uid();
  return h;
end;
$$;

create or replace function public.set_member_role(p_home uuid, p_user uuid, p_role text) returns void
language plpgsql security definer set search_path = public as $$
begin
  if member_role(p_home) <> 'owner' then raise exception 'not_allowed'; end if;
  if p_role not in ('admin','member') then raise exception 'bad_role'; end if;
  update home_members set role = p_role where home_id = p_home and user_id = p_user and role <> 'owner';
end;
$$;

create or replace function public.transfer_ownership(p_home uuid, p_user uuid) returns void
language plpgsql security definer set search_path = public as $$
begin
  if member_role(p_home) <> 'owner' then raise exception 'not_allowed'; end if;
  if not exists(select 1 from home_members where home_id = p_home and user_id = p_user) then
    raise exception 'not_member';
  end if;
  update home_members set role = 'admin' where home_id = p_home and user_id = auth.uid();
  update home_members set role = 'owner' where home_id = p_home and user_id = p_user;
  update homes set owner_id = p_user where id = p_home;
end;
$$;

-- Leave a home. If the owner leaves, the oldest admin/member becomes owner.
-- If nobody is left, the home is deleted.
create or replace function public.leave_home(p_home uuid) returns void
language plpgsql security definer set search_path = public as $$
declare
  was_owner boolean;
  next_owner uuid;
begin
  select role = 'owner' into was_owner from home_members where home_id = p_home and user_id = auth.uid();
  if was_owner is null then return; end if;
  delete from home_members where home_id = p_home and user_id = auth.uid();
  if was_owner then
    select user_id into next_owner from home_members where home_id = p_home
      order by case role when 'admin' then 0 else 1 end, joined_at limit 1;
    if next_owner is null then
      delete from homes where id = p_home;
    else
      update home_members set role = 'owner' where home_id = p_home and user_id = next_owner;
      update homes set owner_id = next_owner where id = p_home;
    end if;
  end if;
  if exists(select 1 from homes where id = p_home) then
    insert into activity(home_id, actor_id, type) values (p_home, auth.uid(), 'left');
  end if;
  update profiles set current_home_id =
    (select home_id from home_members where user_id = auth.uid() order by joined_at limit 1)
  where id = auth.uid();
end;
$$;

create or replace function public.delete_home(p_home uuid) returns void
language plpgsql security definer set search_path = public as $$
begin
  if member_role(p_home) <> 'owner' then raise exception 'not_allowed'; end if;
  delete from homes where id = p_home;
end;
$$;

create or replace function public.regenerate_invite_code(p_home uuid) returns text
language plpgsql security definer set search_path = public as $$
declare code text;
begin
  if member_role(p_home) not in ('owner','admin') then raise exception 'not_allowed'; end if;
  loop
    code := gen_code(6);
    exit when not exists(select 1 from homes where invite_code = code);
  end loop;
  update homes set invite_code = code where id = p_home;
  return code;
end;
$$;

create or replace function public.create_telegram_link_code() returns text
language plpgsql security definer set search_path = public as $$
declare code text;
begin
  loop
    code := lower(gen_code(10));
    exit when not exists(select 1 from profiles where telegram_link_code = code);
  end loop;
  update profiles set telegram_link_code = code where id = auth.uid();
  return code;
end;
$$;

create or replace function public.disconnect_telegram() returns void
language sql security definer set search_path = public as $$
  update profiles set telegram_chat_id = null, notify_telegram = false, telegram_link_code = null
  where id = auth.uid();
$$;

-- Google Play requires in-app account deletion
create or replace function public.delete_account() returns void
language plpgsql security definer set search_path = public as $$
declare r record;
begin
  for r in select home_id from home_members where user_id = auth.uid() loop
    perform leave_home(r.home_id);
  end loop;
  delete from auth.users where id = auth.uid();
end;
$$;

grant execute on function public.create_home(text) to authenticated;
grant execute on function public.join_home(text) to authenticated;
grant execute on function public.set_member_role(uuid, uuid, text) to authenticated;
grant execute on function public.transfer_ownership(uuid, uuid) to authenticated;
grant execute on function public.leave_home(uuid) to authenticated;
grant execute on function public.delete_home(uuid) to authenticated;
grant execute on function public.regenerate_invite_code(uuid) to authenticated;
grant execute on function public.create_telegram_link_code() to authenticated;
grant execute on function public.disconnect_telegram() to authenticated;
grant execute on function public.delete_account() to authenticated;

-- ---------------------------------------------------------------------
-- Realtime (live list updates)
-- ---------------------------------------------------------------------
do $$
begin
  begin alter publication supabase_realtime add table public.items;        exception when duplicate_object then null; end;
  begin alter publication supabase_realtime add table public.activity;     exception when duplicate_object then null; end;
  begin alter publication supabase_realtime add table public.home_members; exception when duplicate_object then null; end;
end $$;
