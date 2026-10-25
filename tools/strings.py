# Generates app/src/main/res/values{,-ru,-en}/strings.xml from one table, so all three languages always have every key.
# values/ = Uzbek (default/fallback). Run: python3 tools/strings.py
import os, html
S = {
 "app_name": ("Savatim", "Savatim", "Savatim"),
 # onboarding / welcome
 "onb1_title": ("Oila uchun umumiy roʻyxat", "Общий список для семьи", "One list for the whole family"),
 "onb1_text": ("Uyingizni yarating, oila aʼzolarini taklif qiling va xaridlarni birga rejalashtiring.", "Создайте дом, пригласите семью и планируйте покупки вместе.", "Create your home, invite your family and plan shopping together."),
 "onb2_title": ("Kim nima qoʻshdi — darhol bilasiz", "Сразу видно, кто что добавил", "See who added what, instantly"),
 "onb2_text": ("«Ona roʻyxatga sabzi qoʻshdi», «Dada non sotib oldi» — hammasi bir joyda.", "«Мама добавила морковь», «Папа купил хлеб» — всё в одном месте.", "“Mom added carrots”, “Dad bought bread” — all in one place."),
 "onb3_title": ("Telegram orqali ham", "Уведомления и в Telegram", "Telegram notifications too"),
 "onb3_text": ("Har kim oʻzi tanlaydi: xabarlar ilovada, Telegram botda yoki ikkalasida.", "Каждый выбирает сам: уведомления в приложении, в Telegram-боте или и там, и там.", "Everyone chooses: notifications in the app, in the Telegram bot, or both."),
 "next": ("Keyingi", "Далее", "Next"),
 "get_started": ("Boshlash", "Начать", "Get started"),
 "welcome_text": ("Savatim — oilangiz uchun umumiy xarid roʻyxati", "Savatim — общий список покупок для вашей семьи", "Savatim is a shared shopping list for your family"),
 "not_configured_title": ("Supabase kalitlari kiritilmagan", "Ключи Supabase не указаны", "Supabase keys are missing"),
 # auth
 "login": ("Kirish", "Войти", "Login"),
 "register": ("Roʻyxatdan oʻtish", "Регистрация", "Register"),
 "email": ("Email", "Email", "Email"),
 "password": ("Parol", "Пароль", "Password"),
 "password_hint": ("Parolingiz", "Ваш пароль", "Your password"),
 "password_min": ("Kamida 6 ta belgi", "Минимум 6 символов", "At least 6 characters"),
 "no_account": ("Akkauntingiz yoʻqmi?", "Нет аккаунта?", "Don't have an account?"),
 "have_account": ("Akkauntingiz bormi?", "Уже есть аккаунт?", "Already have an account?"),
 "your_name": ("Ismingiz", "Ваше имя", "Your name"),
 "your_name_hint": ("Masalan, Malika", "Например, Малика", "e.g. Malika"),
 "terms_note": ("Davom etish orqali siz Maxfiylik siyosatiga rozilik bildirasiz.", "Продолжая, вы соглашаетесь с Политикой конфиденциальности.", "By continuing you agree to the Privacy Policy."),
 "profile_setup_title": ("Oilada kimsiz?", "Кто вы в семье?", "Who are you in the family?"),
 "profile_setup_text": ("Bildirishnomalarda shunday koʻrinasiz: «Ona sabzi qoʻshdi».", "Так вас увидят в уведомлениях: «Мама добавила морковь».", "This is how you'll appear in notifications: “Mom added carrots”."),
 "who_are_you": ("Rolingiz", "Ваша роль", "Your role"),
 "gender": ("Jins", "Пол", "Gender"),
 "female": ("Ayol", "Женский", "Female"),
 "male": ("Erkak", "Мужской", "Male"),
 "gender_why": ("Rus tilidagi xabarlar uchun kerak (добавил / добавила).", "Нужно для правильных окончаний: добавил / добавила.", "Needed for correct Russian grammar (добавил / добавила)."),
 "continue_": ("Davom etish", "Продолжить", "Continue"),
 "save": ("Saqlash", "Сохранить", "Save"),
 "cancel": ("Bekor qilish", "Отмена", "Cancel"),
 "delete": ("Oʻchirish", "Удалить", "Delete"),
 "logout": ("Chiqish", "Выйти", "Log out"),
 # home setup
 "home_setup_title": ("Uyingizni sozlang", "Настройте ваш дом", "Set up your home"),
 "home_setup_text": ("Yangi uy yarating yoki oila aʼzosi yuborgan kod bilan qoʻshiling.", "Создайте новый дом или присоединитесь по коду от члена семьи.", "Create a new home or join with a code from a family member."),
 "create_home": ("Uy yaratish", "Создать дом", "Create home"),
 "join_home": ("Qoʻshilish", "Присоединиться", "Join"),
 "home_name": ("Uy nomi", "Название дома", "Home name"),
 "home_name_hint": ("Masalan, Bizning uy", "Например, Наш дом", "e.g. Our home"),
 "invite_code": ("Taklif kodi", "Код приглашения", "Invite code"),
 "invite_code_where": ("Kodni uy egasidan soʻrang: Profil → Uy aʼzolari.", "Спросите код у владельца дома: Профиль → Участники.", "Ask the home owner for the code: Profile → Members."),
 # main / tabs
 "tab_list": ("Roʻyxat", "Список", "List"),
 "tab_add": ("Qoʻshish", "Добавить", "Add"),
 "tab_activity": ("Faollik", "События", "Activity"),
 "tab_profile": ("Profil", "Профиль", "Profile"),
 "my_homes": ("Mening uylarim", "Мои дома", "My homes"),
 "add_or_join_home": ("Yangi uy yoki kod bilan qoʻshilish", "Новый дом или вход по коду", "New home or join with a code"),
 "homes_hint": ("Masalan: oʻz uyingiz va buvingizning uyi", "Например: ваш дом и дом бабушки", "e.g. your home and your grandparents' home"),
 "banner_title": ("Sotib olish kerak", "Нужно купить", "Still to buy"),
 "banner_count": ("%1$d ta mahsulot", "товаров: %1$d", "%1$d items"),
 "banner_sub": ("Sotib olindi: %1$d · Aʼzolar: %2$d", "Куплено: %1$d · Участников: %2$d", "Bought: %1$d · Members: %2$d"),
 "list_empty_title": ("Roʻyxat boʻsh", "Список пуст", "The list is empty"),
 "list_empty_text": ("Uyga nima kerak? Mahsulot qoʻshing — oila aʼzolari darhol koʻradi.", "Что нужно домой? Добавьте товар — семья сразу увидит.", "What does home need? Add a product — your family sees it right away."),
 "add_products": ("Mahsulot qoʻshish", "Добавить товары", "Add products"),
 "all": ("Hammasi", "Все", "All"),
 "to_buy_count": ("Sotib olish kerak (%1$d)", "Купить (%1$d)", "To buy (%1$d)"),
 "bought_count": ("Sotib olindi (%1$d)", "Куплено (%1$d)", "Bought (%1$d)"),
 "clear": ("Tozalash", "Очистить", "Clear"),
 "added_by": ("Qoʻshdi: %1$s", "Добавил(а): %1$s", "Added by %1$s"),
 "item_name": ("Nomi", "Название", "Name"),
 "quantity": ("Miqdori", "Количество", "Quantity"),
 "quantity_hint": ("Masalan, 2 kg", "Например, 2 кг", "e.g. 2 kg"),
 "note": ("Izoh", "Заметка", "Note"),
 "note_hint": ("Masalan, faqat Nestlé", "Например, только Nestlé", "e.g. Nestlé only"),
 "mark_bought": ("Sotib olindi deb belgilash", "Отметить купленным", "Mark as bought"),
 "mark_not_bought": ("Roʻyxatga qaytarish", "Вернуть в список", "Move back to list"),
 # add tab
 "add_title": ("Mahsulot qoʻshish", "Добавить товары", "Add products"),
 "search_hint": ("Sabzi, морковь, carrot…", "Морковь, sabzi, carrot…", "Carrot, sabzi, морковь…"),
 "add_custom": ("Oʻz mahsulotingizni qoʻshish", "Добавить свой товар", "Add your own item"),
 "custom_item": ("Oʻz mahsulotingiz", "Свой товар", "Custom item"),
 "already_in_list": ("Roʻyxatda bor", "Уже в списке", "Already in list"),
 "add_to_list": ("Roʻyxatga qoʻshish", "Добавить в список", "Add to list"),
 # activity
 "activity_empty_title": ("Hozircha jim", "Пока тихо", "All quiet"),
 "activity_empty_text": ("Kim nima qoʻshgani va sotib olgani shu yerda koʻrinadi.", "Здесь будет видно, кто что добавил и купил.", "Who added and bought what will show up here."),
 "today": ("Bugun", "Сегодня", "Today"),
 "yesterday": ("Kecha", "Вчера", "Yesterday"),
 # profile
 "members": ("Uy aʼzolari", "Участники дома", "Home members"),
 "notifications": ("Bildirishnomalar", "Уведомления", "Notifications"),
 "notify_in_app": ("Ilovada", "В приложении", "In the app"),
 "notify_in_app_desc": ("Telefoningizga bildirishnoma", "Уведомления на телефоне", "Phone notifications"),
 "telegram": ("Telegram", "Telegram", "Telegram"),
 "telegram_connected": ("Bot ulangan — xabarlar Telegramga keladi", "Бот подключён — сообщения приходят в Telegram", "Bot connected — messages arrive in Telegram"),
 "telegram_disconnect": ("Telegramni uzish", "Отключить Telegram", "Disconnect Telegram"),
 "telegram_connect": ("Telegramni ulash", "Подключить Telegram", "Connect Telegram"),
 "telegram_connect_desc": ("Xabarlarni Telegram bot orqali oling", "Получайте уведомления через Telegram-бота", "Get notifications from the Telegram bot"),
 "app_settings": ("Ilova", "Приложение", "App"),
 "language": ("Til", "Язык", "Language"),
 "logout_confirm": ("Akkauntdan chiqasizmi?", "Выйти из аккаунта?", "Log out of your account?"),
 "delete_account": ("Akkauntni oʻchirish", "Удалить аккаунт", "Delete account"),
 "delete_account_confirm": ("Akkauntingiz va barcha maʼlumotlaringiz butunlay oʻchiriladi. Buni qaytarib boʻlmaydi.", "Аккаунт и все ваши данные будут удалены навсегда. Это нельзя отменить.", "Your account and all your data will be permanently deleted. This can't be undone."),
 # members
 "invite_title": ("Oilani taklif qiling", "Пригласите семью", "Invite your family"),
 "invite_text": ("Ular Savatimʼni oʻrnatib, «Qoʻshilish» boʻlimiga shu kodni kiritadi.", "Они устанавливают Savatim и вводят этот код в разделе «Присоединиться».", "They install Savatim and enter this code under “Join”."),
 "invite_share_text": ("Savatim ilovasida «%1$s» uyimizga qoʻshiling! Taklif kodi: %2$s", "Присоединяйся к нашему дому «%1$s» в приложении Savatim! Код: %2$s", "Join our home “%1$s” in the Savatim app! Invite code: %2$s"),
 "copy": ("Nusxa olish", "Копировать", "Copy"),
 "share": ("Ulashish", "Поделиться", "Share"),
 "new_code": ("Yangi kod", "Новый код", "New code"),
 "members_count": ("Aʼzolar (%1$d)", "Участники (%1$d)", "Members (%1$d)"),
 "you": ("(siz)", "(вы)", "(you)"),
 "role_owner": ("Ega", "Владелец", "Owner"),
 "role_admin": ("Admin", "Админ", "Admin"),
 "make_admin": ("Admin qilish", "Сделать админом", "Make admin"),
 "remove_admin": ("Adminlikdan olish", "Снять админа", "Remove admin"),
 "make_owner": ("Egalikni berish", "Передать владение", "Make owner"),
 "make_owner_confirm": ("Uy egaligi %1$s ga oʻtadi, siz admin boʻlasiz. Davom etasizmi?", "Владельцем станет %1$s, а вы станете админом. Продолжить?", "%1$s will become the owner and you'll become an admin. Continue?"),
 "remove_member": ("Uydan chiqarish", "Удалить из дома", "Remove from home"),
 "remove_member_confirm": ("%1$s uydan chiqarilsinmi?", "Удалить %1$s из дома?", "Remove %1$s from the home?"),
 "rename_home": ("Uy nomini oʻzgartirish", "Переименовать дом", "Rename home"),
 "leave_home": ("Uydan chiqish", "Покинуть дом", "Leave home"),
 "leave_home_confirm": ("Bu uydan chiqasizmi? Qaytish uchun yangi kod kerak boʻladi.", "Покинуть этот дом? Чтобы вернуться, понадобится код.", "Leave this home? You'll need a code to come back."),
 "leave_home_owner_confirm": ("Siz uy egasisiz. Chiqsangiz, egalik boshqa aʼzoga oʻtadi. Davom etasizmi?", "Вы владелец. Если выйдете, владение перейдёт другому участнику. Продолжить?", "You're the owner. If you leave, ownership passes to another member. Continue?"),
 "delete_home": ("Uyni oʻchirish", "Удалить дом", "Delete home"),
 "delete_home_confirm": ("Uy, roʻyxat va tarix hamma uchun oʻchiriladi. Buni qaytarib boʻlmaydi.", "Дом, список и история будут удалены для всех. Это нельзя отменить.", "The home, its list and history will be deleted for everyone. This can't be undone."),
 # messages
 "msg_check_email": ("Emailingizga tasdiqlash xati yuborildi. Tasdiqlab, keyin kiring.", "Мы отправили письмо для подтверждения. Подтвердите и войдите.", "We sent you a confirmation email. Confirm it, then log in."),
 "msg_already_in_list": ("Bu mahsulotlar roʻyxatda bor", "Эти товары уже в списке", "These items are already in the list"),
 "err_login": ("Email yoki parol notoʻgʻri", "Неверный email или пароль", "Wrong email or password"),
 "err_user_exists": ("Bu email bilan akkaunt bor. Kirish tugmasini bosing.", "Аккаунт с этим email уже есть. Войдите.", "An account with this email already exists. Please log in."),
 "err_weak_password": ("Parol juda oddiy. Kamida 6 ta belgi kiriting.", "Слишком простой пароль. Минимум 6 символов.", "Password is too weak. Use at least 6 characters."),
 "err_invalid_code": ("Kod notoʻgʻri. Uy egasidan qayta soʻrang.", "Неверный код. Уточните у владельца дома.", "Wrong code. Ask the home owner again."),
 "err_not_allowed": ("Bunga ruxsatingiz yoʻq", "У вас нет прав на это действие", "You're not allowed to do that"),
 "err_network": ("Internet aloqasi yoʻq", "Нет подключения к интернету", "No internet connection"),
 "err_generic": ("Xatolik yuz berdi. Qayta urinib koʻring.", "Что-то пошло не так. Попробуйте ещё раз.", "Something went wrong. Please try again."),
 # notifications
 "notif_channel_name": ("Oila roʻyxati", "Семейный список", "Family list"),
 "notif_channel_desc": ("Oila aʼzolari roʻyxatni oʻzgartirganda", "Когда семья меняет список", "When family members change the list"),
}

def esc(s):
    s = html.escape(s, quote=False)
    s = s.replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"')
    if s.startswith("@") or s.startswith("?"):
        s = "\\" + s
    return s

root = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")
for idx, folder in enumerate(["values", "values-ru", "values-en"]):
    os.makedirs(os.path.join(root, folder), exist_ok=True)
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<!-- Generated by tools/strings.py — edit the table there, then re-run. -->", "<resources>"]
    for k, v in S.items():
        attrs = ' translatable="false"' if k == "app_name" and idx == 0 else ""
        if k == "app_name" and idx > 0:
            continue
        lines.append(f'    <string name="{k}"{attrs}>{esc(v[idx])}</string>')
    lines.append("</resources>")
    open(os.path.join(root, folder, "strings.xml"), "w", encoding="utf-8").write("\n".join(lines) + "\n")
print(len(S), "strings")
