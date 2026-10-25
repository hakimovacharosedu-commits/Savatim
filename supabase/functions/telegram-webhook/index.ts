// Savatim — "telegram-webhook" edge function
// Telegram sends every message written to the bot here.
//   /start <code>  → links this Telegram chat to the Savatim account that created <code>
//   /stop          → turns Telegram notifications off
//
// IMPORTANT: turn OFF "Enforce JWT verification" for this function (Telegram can't send a Supabase JWT).
// Secrets needed (Dashboard → Edge Functions → Secrets):
//   TELEGRAM_BOT_TOKEN       – from @BotFather
//   TELEGRAM_WEBHOOK_SECRET  – any random text (letters/numbers); also passed to Telegram's setWebhook as secret_token

import { createClient } from "jsr:@supabase/supabase-js@2";

type Lang = "uz" | "ru" | "en";
function asLang(v: string | null | undefined): Lang {
  return v === "ru" || v === "en" ? v : "uz";
}
function guessLang(code: string | undefined): Lang {
  if (!code) return "uz";
  if (code.startsWith("ru")) return "ru";
  if (code.startsWith("uz")) return "uz";
  return "en";
}

export const BOT_TEXTS: Record<Lang, Record<string, string>> = {
  uz: {
    linked: "✅ Telegram ulandi! Endi uy roʻyxatidagi oʻzgarishlar shu yerga keladi.",
    bad_code: "❌ Kod notoʻgʻri yoki eskirgan. Savatim ilovasida qaytadan «Telegramni ulash» tugmasini bosing.",
    hello: "👋 Salom! Men Savatim botiman. Ulash uchun ilovada: Profil → Telegramni ulash.",
    stopped: "🔕 Telegram bildirishnomalari oʻchirildi.",
  },
  ru: {
    linked: "✅ Telegram подключён! Теперь изменения в списке дома будут приходить сюда.",
    bad_code: "❌ Код неверный или устарел. Нажмите «Подключить Telegram» в приложении Savatim ещё раз.",
    hello: "👋 Привет! Я бот Savatim. Чтобы подключиться, в приложении: Профиль → Подключить Telegram.",
    stopped: "🔕 Уведомления в Telegram отключены.",
  },
  en: {
    linked: "✅ Telegram connected! Changes to your home's list will now arrive here.",
    bad_code: "❌ The code is wrong or expired. Tap “Connect Telegram” in the Savatim app again.",
    hello: "👋 Hi! I'm the Savatim bot. To connect, in the app: Profile → Connect Telegram.",
    stopped: "🔕 Telegram notifications are turned off.",
  },
};

export async function sendTelegram(token: string, chatId: number | string, text: string) {
  const res = await fetch(`https://api.telegram.org/bot${token}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ chat_id: chatId, text, disable_web_page_preview: true }),
  });
  if (!res.ok) console.error("telegram error", res.status, await res.text());
}

// ---------------------------------------------------------------------------

Deno.serve(async (req) => {
  if (req.headers.get("x-telegram-bot-api-secret-token") !== Deno.env.get("TELEGRAM_WEBHOOK_SECRET")) {
    return new Response("forbidden", { status: 403 });
  }
  const token = Deno.env.get("TELEGRAM_BOT_TOKEN")!;
  const update = await req.json();
  const msg = update?.message;
  const text: string = msg?.text ?? "";
  const chatId: number | undefined = msg?.chat?.id;
  if (!chatId || msg?.chat?.type !== "private") return new Response("ok");

  const db = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
  );
  const fallbackLang = guessLang(msg?.from?.language_code);

  if (text.startsWith("/start")) {
    const code = text.split(/\s+/)[1]?.trim().toLowerCase();
    if (!code) {
      await sendTelegram(token, chatId, BOT_TEXTS[fallbackLang].hello);
      return new Response("ok");
    }
    const { data: profile } = await db
      .from("profiles")
      .update({ telegram_chat_id: chatId, telegram_link_code: null, notify_telegram: true })
      .eq("telegram_link_code", code)
      .select("language")
      .maybeSingle();
    const lang = profile ? asLang(profile.language) : fallbackLang;
    await sendTelegram(token, chatId, profile ? BOT_TEXTS[lang].linked : BOT_TEXTS[lang].bad_code);
  } else if (text.startsWith("/stop")) {
    const { data: profiles } = await db
      .from("profiles")
      .update({ telegram_chat_id: null, notify_telegram: false })
      .eq("telegram_chat_id", chatId)
      .select("language");
    const lang = profiles?.[0] ? asLang(profiles[0].language) : fallbackLang;
    await sendTelegram(token, chatId, BOT_TEXTS[lang].stopped);
  } else {
    await sendTelegram(token, chatId, BOT_TEXTS[fallbackLang].hello);
  }
  return new Response("ok");
});
