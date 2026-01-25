-- Broadcast to per-user channel on like insert
create or replace function public.broadcast_like_insert()
returns trigger as $$
declare
  owner text;
begin
  -- Derive the post owner from posts table
  select p.owner_id::text into owner from public.posts p where p.id = new.post_id;
  if owner is null then
    return new; -- No owner found; skip broadcast
  end if;

  -- Skip broadcast if the liker is the post owner
  if owner = new.user_id::text then
    return new;
  end if;

  perform realtime.send(
    payload => jsonb_build_object(
      'id', new.id,
      'post_id', new.post_id,
      'user_id', new.user_id
    ),
    event => 'insert',
    topic => 'likes:' || owner,
    private => true
  );
  return new;
end;
$$ language plpgsql security definer set search_path='';

drop trigger if exists trg_broadcast_like_insert on public.likes;
create trigger trg_broadcast_like_insert
after insert on public.likes
for each row execute function public.broadcast_like_insert();


-- Ensure RLS is enabled (usually enabled by default)
alter table realtime.messages enable row level security;

-- Replace existing policy if present
drop policy if exists "Allow listening like broadcasts from own likes channel" on realtime.messages;

create policy "Allow listening like broadcasts from own likes channel"
  on realtime.messages
  as permissive
  for select
  to authenticated
  using (
    realtime.messages.extension = 'broadcast'
    and realtime.topic() = ('likes:' || auth.uid()::text)
  );
