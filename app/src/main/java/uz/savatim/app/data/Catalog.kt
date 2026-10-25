package uz.savatim.app.data

import uz.savatim.app.i18n.Lang

data class Category(val key: String, val emoji: String, val uz: String, val ru: String, val en: String) {
    fun name(lang: Lang) = when (lang) {
        Lang.UZ -> uz
        Lang.RU -> ru
        Lang.EN -> en
    }
}

data class Product(
    val key: String,
    val category: String,
    val emoji: String,
    val uz: String,
    val ru: String,
    val en: String,
) {
    fun name(lang: Lang) = when (lang) {
        Lang.UZ -> uz
        Lang.RU -> ru
        Lang.EN -> en
    }

    fun toActivityItem() = ActivityItem(key = key, uz = uz, ru = ru, en = en)
}

/** Built-in trilingual product catalog. Custom items typed by users are stored with catalog_key = null. */
object Catalog {

    val categories = listOf(
        Category("veg", "🥕", "Sabzavotlar", "Овощи", "Vegetables"),
        Category("fruit", "🍎", "Mevalar", "Фрукты", "Fruits"),
        Category("bread", "🍞", "Non", "Хлеб", "Bakery"),
        Category("dairy", "🥛", "Sut va tuxum", "Молочное", "Dairy & eggs"),
        Category("meat", "🥩", "Goʻsht va baliq", "Мясо и рыба", "Meat & fish"),
        Category("pantry", "🍚", "Bakaleya", "Бакалея", "Pantry"),
        Category("drinks", "🧃", "Ichimliklar", "Напитки", "Drinks"),
        Category("sweets", "🍫", "Shirinliklar", "Сладости", "Sweets"),
        Category("household", "🧽", "Roʻzgʻor", "Для дома", "Household"),
        Category("hygiene", "🧴", "Gigiyena", "Гигиена", "Personal care"),
        Category("baby", "🍼", "Bolalar uchun", "Детское", "Baby"),
        Category("other", "📦", "Boshqa", "Другое", "Other"),
    )

    private fun p(key: String, cat: String, emoji: String, uz: String, ru: String, en: String) =
        Product(key, cat, emoji, uz, ru, en)

    val products: List<Product> = listOf(
        // Vegetables
        p("carrot", "veg", "🥕", "Sabzi", "Морковь", "Carrot"),
        p("potato", "veg", "🥔", "Kartoshka", "Картофель", "Potatoes"),
        p("onion", "veg", "🧅", "Piyoz", "Лук", "Onion"),
        p("tomato", "veg", "🍅", "Pomidor", "Помидоры", "Tomatoes"),
        p("cucumber", "veg", "🥒", "Bodring", "Огурцы", "Cucumbers"),
        p("cabbage", "veg", "🥬", "Karam", "Капуста", "Cabbage"),
        p("garlic", "veg", "🧄", "Sarimsoq", "Чеснок", "Garlic"),
        p("bell_pepper", "veg", "🫑", "Bolgar qalampiri", "Болгарский перец", "Bell pepper"),
        p("chili", "veg", "🌶️", "Achchiq qalampir", "Острый перец", "Chili pepper"),
        p("eggplant", "veg", "🍆", "Baqlajon", "Баклажаны", "Eggplant"),
        p("zucchini", "veg", "🥒", "Qovoqcha", "Кабачки", "Zucchini"),
        p("pumpkin", "veg", "🎃", "Qovoq", "Тыква", "Pumpkin"),
        p("beet", "veg", "🟣", "Lavlagi", "Свёкла", "Beetroot"),
        p("radish", "veg", "🔴", "Turp", "Редис", "Radish"),
        p("turnip", "veg", "⚪", "Sholgʻom", "Репа", "Turnip"),
        p("corn", "veg", "🌽", "Makkajoʻxori", "Кукуруза", "Corn"),
        p("mushrooms", "veg", "🍄", "Qoʻziqorin", "Грибы", "Mushrooms"),
        p("greens", "veg", "🌿", "Koʻkat", "Зелень", "Herbs"),
        p("dill", "veg", "🌿", "Ukrop", "Укроп", "Dill"),
        p("cilantro", "veg", "🌿", "Kashnich", "Кинза", "Cilantro"),
        p("parsley", "veg", "🌿", "Petrushka", "Петрушка", "Parsley"),
        p("green_onion", "veg", "🌱", "Koʻk piyoz", "Зелёный лук", "Green onion"),
        p("lettuce", "veg", "🥬", "Salat bargi", "Салат", "Lettuce"),
        p("lemon", "veg", "🍋", "Limon", "Лимон", "Lemon"),

        // Fruits
        p("apple", "fruit", "🍎", "Olma", "Яблоки", "Apples"),
        p("banana", "fruit", "🍌", "Banan", "Бананы", "Bananas"),
        p("orange", "fruit", "🍊", "Apelsin", "Апельсины", "Oranges"),
        p("mandarin", "fruit", "🍊", "Mandarin", "Мандарины", "Tangerines"),
        p("pear", "fruit", "🍐", "Nok", "Груши", "Pears"),
        p("grapes", "fruit", "🍇", "Uzum", "Виноград", "Grapes"),
        p("pomegranate", "fruit", "🔴", "Anor", "Гранат", "Pomegranate"),
        p("persimmon", "fruit", "🟠", "Xurmo", "Хурма", "Persimmon"),
        p("melon", "fruit", "🍈", "Qovun", "Дыня", "Melon"),
        p("watermelon", "fruit", "🍉", "Tarvuz", "Арбуз", "Watermelon"),
        p("peach", "fruit", "🍑", "Shaftoli", "Персики", "Peaches"),
        p("apricot", "fruit", "🍑", "Oʻrik", "Абрикосы", "Apricots"),
        p("cherry", "fruit", "🍒", "Gilos", "Черешня", "Cherries"),
        p("strawberry", "fruit", "🍓", "Qulupnay", "Клубника", "Strawberries"),
        p("kiwi", "fruit", "🥝", "Kivi", "Киви", "Kiwi"),
        p("dried_fruit", "fruit", "🍇", "Quruq mevalar", "Сухофрукты", "Dried fruit"),
        p("raisins", "fruit", "🍇", "Mayiz", "Изюм", "Raisins"),
        p("nuts", "fruit", "🥜", "Yongʻoq", "Орехи", "Nuts"),

        // Bakery
        p("bread", "bread", "🍞", "Non", "Хлеб", "Bread"),
        p("lepeshka", "bread", "🫓", "Obi non", "Лепёшка", "Flatbread"),
        p("baton", "bread", "🥖", "Baton", "Батон", "Baguette"),
        p("lavash", "bread", "🫓", "Lavash", "Лаваш", "Lavash"),
        p("samsa", "bread", "🥟", "Somsa", "Самса", "Samsa"),
        p("cookies", "bread", "🍪", "Pechenye", "Печенье", "Cookies"),
        p("cake", "bread", "🎂", "Tort", "Торт", "Cake"),

        // Dairy & eggs
        p("milk", "dairy", "🥛", "Sut", "Молоко", "Milk"),
        p("eggs", "dairy", "🥚", "Tuxum", "Яйца", "Eggs"),
        p("kefir", "dairy", "🥛", "Kefir", "Кефир", "Kefir"),
        p("qatiq", "dairy", "🥛", "Qatiq", "Катык", "Qatiq (yogurt drink)"),
        p("yogurt", "dairy", "🥣", "Yogurt", "Йогурт", "Yogurt"),
        p("smetana", "dairy", "🥣", "Smetana", "Сметана", "Sour cream"),
        p("tvorog", "dairy", "🧀", "Tvorog", "Творог", "Cottage cheese"),
        p("cheese", "dairy", "🧀", "Pishloq", "Сыр", "Cheese"),
        p("butter", "dairy", "🧈", "Saryogʻ", "Сливочное масло", "Butter"),
        p("qaymoq", "dairy", "🥣", "Qaymoq", "Каймак", "Clotted cream"),
        p("suzma", "dairy", "🥣", "Suzma", "Сузьма", "Suzma"),

        // Meat & fish
        p("beef", "meat", "🥩", "Mol goʻshti", "Говядина", "Beef"),
        p("lamb", "meat", "🍖", "Qoʻy goʻshti", "Баранина", "Lamb"),
        p("chicken", "meat", "🍗", "Tovuq", "Курица", "Chicken"),
        p("minced_meat", "meat", "🥩", "Qiyma", "Фарш", "Minced meat"),
        p("sausage", "meat", "🌭", "Kolbasa", "Колбаса", "Sausage"),
        p("hot_dogs", "meat", "🌭", "Sosiska", "Сосиски", "Hot dogs"),
        p("fish", "meat", "🐟", "Baliq", "Рыба", "Fish"),
        p("kazy", "meat", "🥓", "Qazi", "Казы", "Kazy"),

        // Pantry
        p("rice", "pantry", "🍚", "Guruch", "Рис", "Rice"),
        p("flour", "pantry", "🌾", "Un", "Мука", "Flour"),
        p("sugar", "pantry", "🍬", "Shakar", "Сахар", "Sugar"),
        p("salt", "pantry", "🧂", "Tuz", "Соль", "Salt"),
        p("oil", "pantry", "🫒", "Yogʻ", "Растительное масло", "Cooking oil"),
        p("cotton_oil", "pantry", "🫙", "Paxta yogʻi", "Хлопковое масло", "Cottonseed oil"),
        p("pasta", "pantry", "🍝", "Makaron", "Макароны", "Pasta"),
        p("buckwheat", "pantry", "🥣", "Grechka", "Гречка", "Buckwheat"),
        p("oats", "pantry", "🥣", "Suli yormasi", "Овсянка", "Oats"),
        p("mung", "pantry", "🫘", "Mosh", "Маш", "Mung beans"),
        p("chickpeas", "pantry", "🫘", "Noʻxat", "Нут", "Chickpeas"),
        p("beans", "pantry", "🫘", "Loviya", "Фасоль", "Beans"),
        p("lentils", "pantry", "🫘", "Yasmiq", "Чечевица", "Lentils"),
        p("tea", "pantry", "🍵", "Choy", "Чай", "Tea"),
        p("green_tea", "pantry", "🍵", "Koʻk choy", "Зелёный чай", "Green tea"),
        p("coffee", "pantry", "☕", "Qahva", "Кофе", "Coffee"),
        p("tomato_paste", "pantry", "🥫", "Tomat pastasi", "Томатная паста", "Tomato paste"),
        p("mayonnaise", "pantry", "🥫", "Mayonez", "Майонез", "Mayonnaise"),
        p("ketchup", "pantry", "🥫", "Ketchup", "Кетчуп", "Ketchup"),
        p("spices", "pantry", "🧂", "Ziravorlar", "Специи", "Spices"),
        p("cumin", "pantry", "🧂", "Zira", "Зира", "Cumin"),
        p("black_pepper", "pantry", "🧂", "Qora murch", "Чёрный перец", "Black pepper"),
        p("vinegar", "pantry", "🫙", "Sirka", "Уксус", "Vinegar"),
        p("honey", "pantry", "🍯", "Asal", "Мёд", "Honey"),
        p("jam", "pantry", "🍓", "Murabbo", "Варенье", "Jam"),
        p("canned", "pantry", "🥫", "Konserva", "Консервы", "Canned food"),
        p("yeast", "pantry", "🧫", "Xamirturush", "Дрожжи", "Yeast"),
        p("baking_soda", "pantry", "🧂", "Soda", "Сода", "Baking soda"),

        // Drinks
        p("water", "drinks", "💧", "Suv", "Вода", "Water"),
        p("sparkling_water", "drinks", "🫧", "Gazli suv", "Газированная вода", "Sparkling water"),
        p("juice", "drinks", "🧃", "Sharbat", "Сок", "Juice"),
        p("soda", "drinks", "🥤", "Gazli ichimlik", "Газировка", "Soft drink"),
        p("compote", "drinks", "🍹", "Kompot", "Компот", "Compote"),
        p("ayran", "drinks", "🥛", "Ayron", "Айран", "Ayran"),

        // Sweets
        p("chocolate", "sweets", "🍫", "Shokolad", "Шоколад", "Chocolate"),
        p("candy", "sweets", "🍬", "Konfet", "Конфеты", "Candy"),
        p("ice_cream", "sweets", "🍦", "Muzqaymoq", "Мороженое", "Ice cream"),
        p("halva", "sweets", "🍯", "Holva", "Халва", "Halva"),
        p("navat", "sweets", "🍬", "Novvot", "Набат", "Navat (rock sugar)"),
        p("chips", "sweets", "🍟", "Chips", "Чипсы", "Chips"),
        p("seeds", "sweets", "🌻", "Pista (kungaboqar)", "Семечки", "Sunflower seeds"),

        // Household
        p("dish_soap", "household", "🧽", "Idish yuvish vositasi", "Средство для посуды", "Dish soap"),
        p("laundry", "household", "🧺", "Kir yuvish kukuni", "Стиральный порошок", "Laundry detergent"),
        p("bleach", "household", "🧴", "Oqartiruvchi", "Отбеливатель", "Bleach"),
        p("sponges", "household", "🧽", "Gubka", "Губки", "Sponges"),
        p("trash_bags", "household", "🗑️", "Axlat paketi", "Мусорные пакеты", "Trash bags"),
        p("paper_towels", "household", "🧻", "Qogʻoz sochiq", "Бумажные полотенца", "Paper towels"),
        p("napkins", "household", "🧻", "Salfetka", "Салфетки", "Napkins"),
        p("foil", "household", "🧾", "Folga", "Фольга", "Foil"),
        p("light_bulb", "household", "💡", "Lampochka", "Лампочка", "Light bulb"),
        p("batteries", "household", "🔋", "Batareya", "Батарейки", "Batteries"),
        p("matches", "household", "🔥", "Gugurt", "Спички", "Matches"),

        // Personal care
        p("toilet_paper", "hygiene", "🧻", "Tualet qogʻozi", "Туалетная бумага", "Toilet paper"),
        p("soap", "hygiene", "🧼", "Sovun", "Мыло", "Soap"),
        p("shampoo", "hygiene", "🧴", "Shampun", "Шампунь", "Shampoo"),
        p("toothpaste", "hygiene", "🪥", "Tish pastasi", "Зубная паста", "Toothpaste"),
        p("toothbrush", "hygiene", "🪥", "Tish choʻtkasi", "Зубная щётка", "Toothbrush"),
        p("shower_gel", "hygiene", "🧴", "Dush geli", "Гель для душа", "Shower gel"),
        p("deodorant", "hygiene", "🧴", "Dezodorant", "Дезодорант", "Deodorant"),
        p("pads", "hygiene", "🩹", "Gigiyenik prokladka", "Прокладки", "Pads"),
        p("wet_wipes", "hygiene", "🧻", "Nam salfetka", "Влажные салфетки", "Wet wipes"),

        // Baby
        p("diapers", "baby", "👶", "Tagliklar", "Подгузники", "Diapers"),
        p("baby_food", "baby", "🍼", "Bolalar ovqati", "Детское питание", "Baby food"),
        p("formula", "baby", "🍼", "Bolalar aralashmasi", "Детская смесь", "Baby formula"),
        p("baby_wipes", "baby", "🧻", "Bolalar salfetkasi", "Детские салфетки", "Baby wipes"),
    )

    private val byKey = products.associateBy { it.key }
    private val categoryByKey = categories.associateBy { it.key }

    fun product(key: String?): Product? = key?.let { byKey[it] }
    fun category(key: String): Category = categoryByKey[key] ?: categories.last()

    /** Emoji for an item in the list: the product's own, else its category's. */
    fun emojiFor(item: Item): String = product(item.catalogKey)?.emoji ?: category(item.category).emoji

    /** Name of a list item in the viewer's language (catalog items translate, custom items stay as typed). */
    fun displayName(item: Item, lang: Lang): String = product(item.catalogKey)?.name(lang) ?: item.name

    fun normalize(s: String): String = s.lowercase()
        .replace('ʻ', '\'').replace('ʼ', '\'').replace('‘', '\'').replace('’', '\'').replace('`', '\'')
        .replace('ё', 'е')
        .trim()

    /** Searches all three languages, so "sabzi", "морковь" and "carrot" all find the carrot. */
    fun search(query: String): List<Product> {
        val q = normalize(query)
        if (q.isEmpty()) return products
        return products.filter { pr ->
            normalize(pr.uz).contains(q) || normalize(pr.ru).contains(q) || normalize(pr.en).contains(q)
        }.sortedBy { pr ->
            val starts = listOf(pr.uz, pr.ru, pr.en).any { normalize(it).startsWith(q) }
            if (starts) 0 else 1
        }
    }
}
