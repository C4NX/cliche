-- Drop the existing constraint that is blocking user deletion
ALTER TABLE public.profiles
DROP CONSTRAINT IF EXISTS profiles_id_fkey;

-- Recreate the constraint with ON DELETE CASCADE
-- so deleting a user automatically deletes their profile
ALTER TABLE public.profiles
ADD CONSTRAINT profiles_id_fkey
FOREIGN KEY (id) REFERENCES auth.users(id)
ON DELETE CASCADE;