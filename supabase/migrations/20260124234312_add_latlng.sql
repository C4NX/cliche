ALTER TABLE public.posts
  ADD COLUMN latitude double precision NULL DEFAULT NULL,
  ADD COLUMN longitude double precision NULL DEFAULT NULL;

DROP VIEW public.timeline;
create view public.timeline with (security_invoker=on) as
select
  p.id,
  p.created_at,
  p.caption,
  p.owner_id,
  pr.username as owner_username,
  pr.avatar_url as owner_avatar_url,
  p.media_paths,
  p.latitude,
  p.longitude,
  count(distinct l.id) as likes_count,
  count(distinct c.id) as comments_count,
  (
    exists (
      select
        1
      from
        likes l2
      where
        l2.post_id = p.id
        and l2.user_id = auth.uid ()
    )
  ) as liked_by_user
from
  posts p
  left join likes l on l.post_id = p.id
  left join comments c on c.post_id = p.id
  left join profiles pr on p.owner_id = pr.id
group by
  p.id,
  pr.username,
  pr.avatar_url;