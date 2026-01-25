DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'profiles'
          AND column_name = 'bio'
    ) THEN
        ALTER TABLE public.profiles
        ADD COLUMN bio text NULL,
        ADD CONSTRAINT profiles_bio_check CHECK ((length(bio) <= 300));
    END IF;
END;
$$;