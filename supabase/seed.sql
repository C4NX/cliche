BEGIN;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS "pgcrypto" WITH SCHEMA extensions;

-- =================Users==================

WITH users AS (
  SELECT * FROM (VALUES
    ('11111111-1111-1111-1111-111111111111'::uuid, 'stefan@example.com', '{"username": "Stefan 1234"}'::jsonb),
    ('22222222-2222-2222-2222-222222222222'::uuid, 'alex@example.com',   '{"username": "Alexisky"}'::jsonb),
    ('33333333-3333-3333-3333-333333333333'::uuid, 'maton@example.com', '{"username": "Big Maton"}'::jsonb)
  ) AS t(id, email, raw_user_meta_data)
)
INSERT INTO auth.users (id, email, raw_user_meta_data)
SELECT id, email, raw_user_meta_data FROM users
ON CONFLICT (id) DO NOTHING;

-- =================Profiles==================

UPDATE public.profiles p
SET avatar_url = COALESCE(avatar_url, 'avatars/' || p.id::text || '/avatar.png')
WHERE p.id IN (
  '11111111-1111-1111-1111-111111111111'::uuid,
  '22222222-2222-2222-2222-222222222222'::uuid,
  '33333333-3333-3333-3333-333333333333'::uuid
);

-- =================Posts==================

INSERT INTO public.posts (owner_id, caption, media_paths, latitude, longitude)
VALUES 
  ('11111111-1111-1111-1111-111111111111', 'Hello from Stefan 1234 #seed-1', ARRAY['11111111-1111-1111-1111-111111111111/hello-1.jpg'], 37.7749, -122.4194),
  ('22222222-2222-2222-2222-222222222222', 'Alexisky''s first post #seed-2', ARRAY['22222222-2222-2222-2222-222222222222/sunset.jpg'], NULL, NULL),
  ('33333333-3333-3333-3333-333333333333', 'Big Maton''s coffee #seed-3',   ARRAY['33333333-3333-3333-3333-333333333333/coffee.png'], 34.0522, -118.2437),
  ('11111111-1111-1111-1111-111111111111', 'Stefan 1234 again #seed-4',      ARRAY['11111111-1111-1111-1111-111111111111/again.jpg'], NULL, NULL)
ON CONFLICT DO NOTHING;

-- =================Likes==================

INSERT INTO public.likes (post_id, user_id)
SELECT p.id, '11111111-1111-1111-1111-111111111111'::uuid
FROM public.posts p
WHERE p.caption = 'Alexisky''s first post #seed-2'
ON CONFLICT (post_id, user_id) DO NOTHING;

INSERT INTO public.likes (post_id, user_id)
SELECT p.id, '11111111-1111-1111-1111-111111111111'::uuid
FROM public.posts p
WHERE p.caption = 'Big Maton''s coffee #seed-3'
ON CONFLICT (post_id, user_id) DO NOTHING;

INSERT INTO public.likes (post_id, user_id)
SELECT p.id, '22222222-2222-2222-2222-222222222222'::uuid
FROM public.posts p
WHERE p.caption = 'Hello from Stefan 1234 #seed-1'
ON CONFLICT (post_id, user_id) DO NOTHING;

INSERT INTO public.likes (post_id, user_id)
SELECT p.id, '33333333-3333-3333-3333-333333333333'::uuid
FROM public.posts p
WHERE p.caption = 'Hello from Stefan 1234 #seed-1'
ON CONFLICT (post_id, user_id) DO NOTHING;

INSERT INTO public.likes (post_id, user_id)
SELECT p.id, '33333333-3333-3333-3333-333333333333'::uuid
FROM public.posts p
WHERE p.caption = 'Alexisky''s first post #seed-2'
ON CONFLICT (post_id, user_id) DO NOTHING;

-- =================Comments==================

INSERT INTO public.comments (post_id, user_id, content)
SELECT p.id, '22222222-2222-2222-2222-222222222222'::uuid, 'Nice shot, Stefan 1234!'
FROM public.posts p WHERE p.caption = 'Hello from Stefan 1234 #seed-1'
ON CONFLICT DO NOTHING;

INSERT INTO public.comments (post_id, user_id, content)
SELECT p.id, '33333333-3333-3333-3333-333333333333'::uuid, 'Love this coffee!'
FROM public.posts p WHERE p.caption = 'Big Maton''s coffee #seed-3'
ON CONFLICT DO NOTHING;

INSERT INTO public.comments (post_id, user_id, content)
SELECT p.id, '11111111-1111-1111-1111-111111111111'::uuid, 'Great vibes, Stefan 1234.'
FROM public.posts p WHERE p.caption = 'Stefan 1234''s first post #seed-2'
ON CONFLICT DO NOTHING;

--================Bookmarks==================

INSERT INTO public.bookmarks (post_id, profile_id)
SELECT p.id, '11111111-1111-1111-1111-111111111111'::uuid
FROM public.posts p WHERE p.caption = 'Big Maton''s coffee #seed-3'
ON CONFLICT (post_id, profile_id) DO NOTHING;

INSERT INTO public.bookmarks (post_id, profile_id)
SELECT p.id, '22222222-2222-2222-2222-222222222222'::uuid
FROM public.posts p WHERE p.caption = 'Stefan 1234 again #seed-4'
ON CONFLICT (post_id, profile_id) DO NOTHING;

COMMIT;