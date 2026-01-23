set check_function_bodies = off;

CREATE OR REPLACE FUNCTION public.get_timeline(start_idx integer, end_idx integer)
 RETURNS TABLE(id bigint, created_at timestamp with time zone, description text, owner_id uuid, content character varying[], username text, avatar_url text, likes_count bigint, comments_count bigint, user_has_liked boolean, score numeric)
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
  RETURN QUERY
  WITH temp_posts AS (
    SELECT
      p.*,
      pr.username,
      pr.avatar_url,
      COALESCE(likes_ct.count, 0) as likes_count,
      COALESCE(comments_ct.count, 0) as comments_count,
      -- Check if the current logged-in user has liked the post
      EXISTS (
        SELECT 1 FROM likes l
        WHERE l.post_id = p.id AND l.user_id = auth.uid()
      ) AS user_has_liked
    FROM
      posts p
    LEFT JOIN
      profiles pr ON p.owner_id = pr.id
    LEFT JOIN (
      SELECT post_id, COUNT(*) as count
      FROM likes
      GROUP BY post_id
    ) likes_ct ON p.id = likes_ct.post_id
    LEFT JOIN (
      SELECT post_id, COUNT(*) as count
      FROM comments
      GROUP BY post_id
    ) comments_ct ON p.id = comments_ct.post_id
    ORDER BY
      p.created_at DESC
    LIMIT 1000
  )
  SELECT
    tp.id,
    tp.created_at,
    tp.description,
    tp.owner_id,
    tp.content,
    tp.username,
    tp.avatar_url,
    tp.likes_count,
    tp.comments_count,
    tp.user_has_liked,
    (tp.likes_count + tp.comments_count * 2 + GREATEST(0, 24 - EXTRACT(EPOCH FROM (NOW() - tp.created_at))/3600)/24 * 10)::numeric as score
  FROM
    temp_posts tp
  ORDER BY
    score DESC,
    created_at DESC
  LIMIT (end_idx - start_idx + 1) OFFSET start_idx;
END;
$function$
;

CREATE OR REPLACE FUNCTION public.handle_new_user()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
begin
  insert into public.profiles (id, username)
  values (new.id, COALESCE(new.raw_user_meta_data->>'username', new.email));
  return new;
end;
$function$
;


