<div align="center">
  <img src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Cliché">
  <h1>Cliché - An instagram-like app</h1>
</div>

## Getting Started

You will need to create a `local.properties` file in the root directory of the project with the following content:

```properties
SUPABASE_URL=<your-supabase-url>
SUPABASE_ANON_KEY=<your-supabase-anon-key>
```

You will also need supabase-cli installed. You can find the installation instructions [here](https://supabase.com/docs/guides/local-development/cli/getting-started).

```bash
supabase login
supabase link --project-ref <your-project-ref>
supabase start
supabase functions start
```

And set `SUPABASE_URL` and `SUPABASE_ANON_KEY` with the values from the local supabase instance.

Example (from `supabase start`):

```
Started supabase local development setup.

╭──────────────────────────────────────╮
│ 🔧 Development Tools                 │
├─────────┬────────────────────────────┤
│ Studio  │ http://127.0.0.1:54323     │
│ Mailpit │ http://127.0.0.1:54324     │
│ MCP     │ http://127.0.0.1:54321/mcp │
╰─────────┴────────────────────────────╯

╭──────────────────────────────────────────────────────╮
│ 🌐 APIs                                              │
├────────────────┬─────────────────────────────────────┤
│ Project URL    │ http://127.0.0.1:54321              │
│ REST           │ http://127.0.0.1:54321/rest/v1      │
│ GraphQL        │ http://127.0.0.1:54321/graphql/v1   │
│ Edge Functions │ http://127.0.0.1:54321/functions/v1 │
╰────────────────┴─────────────────────────────────────╯

... other output ...
```

set the `local.properties` like this:

```properties
SUPABASE_URL=http://localhost:54321 (the Project URL from above)
SUPABASE_ANON_KEY=<your-local-supabase-anon-key> (the Publishable API Key in Authentication Keys section of supabase cli output)
```
