drop function if exists "public"."get_timeline"(start_idx integer, end_idx integer);

alter table "public"."posts" drop column "content";

alter table "public"."posts" drop column "description";

alter table "public"."posts" add column "caption" text;

alter table "public"."posts" add column "media_paths" text[] not null default '{}';

alter table "public"."posts" alter column "owner_id" set not null;

set check_function_bodies = off;

create or replace view "public"."timeline"
    with (security_invoker=on) as
    SELECT p.id,
    p.created_at,
    p.caption,
    p.owner_id,
    pr.username AS owner_username,
    pr.avatar_url AS owner_avatar_url,
    p.media_paths,
    count(DISTINCT l.id) AS likes_count,
    count(DISTINCT c.id) AS comments_count,
    (EXISTS ( SELECT 1
           FROM public.likes l2
          WHERE ((l2.post_id = p.id) AND (l2.user_id = auth.uid())))) AS liked_by_user
   FROM (((public.posts p
     LEFT JOIN public.likes l ON ((l.post_id = p.id)))
     LEFT JOIN public.comments c ON ((c.post_id = p.id)))
     LEFT JOIN public.profiles pr ON ((p.owner_id = pr.id)))
  GROUP BY p.id, pr.username, pr.avatar_url;


CREATE OR REPLACE FUNCTION public.handle_new_user()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
begin
  insert into public.profiles (id, username)
  values (new.id, new.raw_user_meta_data->>'username');
  return new;
end;
$function$
;


  create policy "Post media is publicly readable"
  on "storage"."objects"
  as permissive
  for select
  to public
using ((bucket_id = 'posts'::text));



  create policy "Users can delete their own avatar"
  on "storage"."objects"
  as permissive
  for delete
  to public
using (((bucket_id = 'avatars'::text) AND ((auth.uid())::text = (storage.foldername(name))[1])));



  create policy "Users can delete their own post media"
  on "storage"."objects"
  as permissive
  for delete
  to public
using (((bucket_id = 'posts'::text) AND ((auth.uid())::text = (storage.foldername(name))[1])));



  create policy "Users can insert their own avatar"
  on "storage"."objects"
  as permissive
  for insert
  to public
with check (((bucket_id = 'avatars'::text) AND ((auth.uid())::text = (storage.foldername(name))[1])));



  create policy "Users can manage their own post media"
  on "storage"."objects"
  as permissive
  for update
  to public
using (((bucket_id = 'posts'::text) AND ((auth.uid())::text = (storage.foldername(name))[1])));



  create policy "Users can update their own avatar"
  on "storage"."objects"
  as permissive
  for update
  to public
using (((bucket_id = 'avatars'::text) AND ((auth.uid())::text = (storage.foldername(name))[1])));



  create policy "Users can upload post media"
  on "storage"."objects"
  as permissive
  for insert
  to public
with check (((bucket_id = 'posts'::text) AND ((auth.uid())::text = (storage.foldername(name))[1])));



