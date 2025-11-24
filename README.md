# Cliché - An instagram-like app

## Getting Started

You will need to create a `local.properties` file in the root directory of the project with the following content:

```properties
SUPABASE_URL=<your-supabase-url>
SUPABASE_ANON_KEY=<your-supabase-anon-key>
```

You will also need supabase cli installed. You can find the installation instructions [here](https://supabase.com/docs/guides/local-development/cli/getting-started).

```bash
supabase login
supabase link --project-ref <your-project-ref>
supabase start
```