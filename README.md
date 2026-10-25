# Savatim 🧺

A shared grocery list for families. Uzbek / Russian / English.
Kotlin + Jetpack Compose, with Supabase as the free backend and a Telegram bot.

## Project map

```
app/src/main/java/uz/savatim/app/
  MainActivity.kt, SavatimApp.kt
  AppViewModel.kt          ← all app state + actions (login, homes, list, realtime)
  data/                    ← Supabase client (Supa), Repo (all queries), Models, Catalog (≈140 products × 3 languages), Prefs
  i18n/Lang.kt             ← language switching, family roles, notification sentences
  notify/                  ← phone notifications + background check every ~15 min
  ui/theme/Theme.kt        ← colors & Inter font from the Figma files
  ui/components/           ← buttons, fields, chips (Kangsayur / Nectar style)
  ui/screens/              ← every screen
supabase/
  schema.sql               ← tables, security rules, functions — run once in the SQL editor
  functions/notify/        ← edge function: sends Telegram messages when the list changes
  functions/telegram-webhook/ ← edge function: the bot itself (/start <code>, /stop)
tools/strings.py           ← all UI texts in 3 languages → generates strings.xml
```

## Secrets go in `local.properties` (never commit it)

```
SUPABASE_URL=https://xxxxxxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
TELEGRAM_BOT_USERNAME=SavatimBot
```

## Editing texts

Edit the table in `tools/strings.py`, then run `python3 tools/strings.py`.
Don't edit `res/values*/strings.xml` by hand — they are generated.

## Notes

- Free Supabase projects pause after 7 days with no activity. Open the dashboard and click "Restore" if that happens.
- Background notifications run about every 15 minutes. Instant push while the app is closed needs Firebase Cloud Messaging (planned).
- The Anor mascot (`res/drawable/anor.xml`) is placeholder art.
