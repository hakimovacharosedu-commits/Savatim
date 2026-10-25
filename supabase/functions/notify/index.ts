// Savatim — "notify" edge function
// Called by a Database Webhook every time a row is inserted into public.activity.
// Sends a Telegram message to every member of that home who connected Telegram
// (except the person who did the action), in each member's own language.
//
// Secrets needed (Dashboard → Edge Functions → Secrets):
//   TELEGRAM_BOT_TOKEN   – from @BotFather
//   NOTIFY_SECRET        – any random text; the same value goes into the webhook header "x-notify-secret"
// SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY are provided automatically.

import { createClient } from "jsr:@supabase/supabase-js@2";

// Shared message texts for Savatim edge functions (uz / ru / en).
// Keep in sync with app/src/main/java/uz/savatim/app/notify/ActivityText.kt

export type Lang = "uz" | "ru" | "en";

export interface Member {
  id: string;
  name: string;
  role_label: string;
  gender: string; // "m" | "f"
}

export interface ActivityItem {
  key?: string | null;
  name?: string | null;
  uz?: string | null;
  ru?: string | null;
  en?: string | null;
}

const ROLE_LABELS: Record<Lang, Record<string, string>> = {
  uz: {
    mom: "Ona", dad: "Dada", grandma: "Buvi", grandpa: "Bobo",
    sister_older: "Opa", brother_older: "Aka", sister_younger: "Singil", brother_younger: "Uka",
  },
  ru: {
    mom: "Мама", dad: "Папа", grandma: "Бабушка", grandpa: "Дедушка",
    sister_older: "Старшая сестра", brother_older: "Старший брат",
    sister_younger: "Младшая сестра", brother_younger: "Младший брат",
  },
  en: {
    mom: "Mom", dad: "Dad", grandma: "Grandma", grandpa: "Grandpa",
    sister_older: "Older sister", brother_older: "Older brother",
    sister_younger: "Younger sister", brother_younger: "Younger brother",
  },
};

export function asLang(v: string | null | undefined): Lang {
  return v === "ru" || v === "en" ? v : "uz";
}

/** "Ona", or "Ona (Malika)" when two members share the label, or just the name for "other". */
export function actorName(actor: Member | undefined, members: Member[], lang: Lang): string {
  if (!actor) return lang === "ru" ? "Кто-то" : lang === "en" ? "Someone" : "Kimdir";
  const label = ROLE_LABELS[lang][actor.role_label];
  if (!label) return actor.name || "?";
  const duplicates = members.filter((m) => m.role_label === actor.role_label).length > 1;
  return duplicates && actor.name ? `${label} (${actor.name})` : label;
}

export function itemName(item: ActivityItem, lang: Lang): string {
  const localized = item[lang];
  if (item.key && localized) return localized.toLowerCase();
  return item.name || localized || "?";
}

function ruCount(n: number): string {
  const m10 = n % 10, m100 = n % 100;
  if (m10 === 1 && m100 !== 11) return `${n} товар`;
  if (m10 >= 2 && m10 <= 4 && (m100 < 12 || m100 > 14)) return `${n} товара`;
  return `${n} товаров`;
}

export function activityText(
  type: string,
  actor: Member | undefined,
  members: Member[],
  items: ActivityItem[],
  lang: Lang,
): string {
  const a = actorName(actor, members, lang);
  const names = items.map((i) => itemName(i, lang));
  const n = names.length;
  const one = names[0] ?? "";
  const list = names.join(", ");
  const f = actor?.gender === "f";

  if (lang === "ru") {
    const v = (m: string, w: string) => (f ? w : m);
    switch (type) {
      case "added":
        return n <= 1 ? `${a} ${v("добавил", "добавила")} в список: ${one}`
          : `${a} ${v("добавил", "добавила")} в список ${ruCount(n)}: ${list}`;
      case "bought":
        return n <= 1 ? `${a} ${v("купил", "купила")}: ${one}`
          : `${a} ${v("купил", "купила")} ${ruCount(n)}: ${list}`;
      case "removed":
        return n <= 1 ? `${a} ${v("удалил", "удалила")} из списка: ${one}`
          : `${a} ${v("удалил", "удалила")} из списка ${ruCount(n)}: ${list}`;
      case "joined": return `${a} ${v("присоединился", "присоединилась")} к дому`;
      case "left": return `${a} ${v("вышел", "вышла")} из дома`;
      case "all_bought": return `${a} ${v("купил", "купила")} всё! Список пуст 🎉`;
    }
  } else if (lang === "en") {
    switch (type) {
      case "added": return n <= 1 ? `${a} added ${one} to the list` : `${a} added ${n} items to the list: ${list}`;
      case "bought": return n <= 1 ? `${a} bought ${one}` : `${a} bought ${n} items: ${list}`;
      case "removed": return n <= 1 ? `${a} removed ${one} from the list` : `${a} removed ${n} items from the list: ${list}`;
      case "joined": return `${a} joined the home`;
      case "left": return `${a} left the home`;
      case "all_bought": return `${a} bought everything! The list is empty 🎉`;
    }
  } else {
    switch (type) {
      case "added": return n <= 1 ? `${a} roʻyxatga ${one} qoʻshdi` : `${a} roʻyxatga ${n} ta mahsulot qoʻshdi: ${list}`;
      case "bought": return n <= 1 ? `${a} ${one} sotib oldi` : `${a} ${n} ta mahsulot sotib oldi: ${list}`;
      case "removed": return n <= 1 ? `${a} roʻyxatdan ${one}ni oʻchirdi` : `${a} roʻyxatdan ${n} ta mahsulotni oʻchirdi: ${list}`;
      case "joined": return `${a} uyga qoʻshildi`;
      case "left": return `${a} uydan chiqdi`;
      case "all_bought": return `${a} hammasini sotib oldi! Roʻyxat boʻsh 🎉`;
    }
  }
  return a;
}

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
  if (req.headers.get("x-notify-secret") !== Deno.env.get("NOTIFY_SECRET")) {
    return new Response("forbidden", { status: 403 });
  }
  const token = Deno.env.get("TELEGRAM_BOT_TOKEN");
  if (!token) return new Response("no bot token", { status: 500 });

  const payload = await req.json();
  const record = payload?.record;
  if (!record?.home_id) return new Response("ignored");

  const db = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
  );

  const { data: home } = await db.from("homes").select("name").eq("id", record.home_id).single();
  const { data: rows, error } = await db
    .from("home_members")
    .select("user_id, profiles(id, name, role_label, gender, language, notify_telegram, telegram_chat_id)")
    .eq("home_id", record.home_id);
  if (error) {
    console.error(error);
    return new Response("db error", { status: 500 });
  }

  // deno-lint-ignore no-explicit-any
  const profiles = (rows ?? []).map((r: any) => r.profiles).filter(Boolean);
  const members: Member[] = profiles.map((p: Member) => p);

  let actor: Member | undefined = members.find((m) => m.id === record.actor_id);
  if (!actor && record.actor_id) {
    // The actor may have just left the home ("left" event) – load the profile directly.
    const { data } = await db.from("profiles").select("id, name, role_label, gender").eq("id", record.actor_id).maybeSingle();
    if (data) actor = data as Member;
  }

  const items: ActivityItem[] = Array.isArray(record.items) ? record.items : [];
  const sends = profiles
    // deno-lint-ignore no-explicit-any
    .filter((p: any) => p.id !== record.actor_id && p.notify_telegram && p.telegram_chat_id)
    // deno-lint-ignore no-explicit-any
    .map((p: any) => {
      const lang = asLang(p.language);
      const text = `🏠 ${home?.name ?? "Savatim"}\n${activityText(record.type, actor, members, items, lang)}`;
      return sendTelegram(token, p.telegram_chat_id, text);
    });
  await Promise.all(sends);

  return new Response(JSON.stringify({ sent: sends.length }), {
    headers: { "Content-Type": "application/json" },
  });
});
