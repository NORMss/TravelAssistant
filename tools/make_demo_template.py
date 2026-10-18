#!/usr/bin/env python3
"""
Собирает демонстрационный шаблон маршрута из личного плана BeijingTrip.

Главный принцип — **белый список**. Скрипт не вычищает личное из исходника,
а копирует только те поля, которые заведомо безопасны. Разница принципиальная:
чёрный список ломается от одного незамеченного поля, белый — не ломается вовсе,
потому что незнакомое поле просто не попадает в результат.

Второй рубеж — `DemoTemplatePrivacyTest` в core:data: он читает готовый файл
и падает, если находит в нём следы личных данных. Скрипт можно сломать правкой,
тест — нет.

Запуск:
    python3 tools/make_demo_template.py ../BeijingTrip/app/src/main/assets/trip.json
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

OUT = Path("core/data/src/main/assets/templates/beijing-6-days.json")

# Что считаем следом личного. Совпадение в любом попавшем в результат тексте —
# повод выбросить весь элемент, а не «подчистить» его.
PERSONAL = re.compile(
    r"Bezborod|Сергей|Серёж|депозит|ПОПРАВКА|письменн"
    r"|брон\w*|предоплат|отмен\w* с \d|押金|预订"
    r"|вчетвер|на четвер|четвером|四位|四个人"
    r"|\bS7\b|Новосиб|Толмач|rsvn@|@hiexdz"
    r"|\b5600|9489|MVX9JRX|O6FQ86|4214123099879"
    r"|наш отел|ваш отел|в вашем отел",
    re.IGNORECASE,
)

# Отель — самый опасный объект: «где этот человек ночевал в эти даты».
# Выбрасываем сам отель и этапы заезда-выезда. Слово «отель» в тексте вроде
# «Didi до отеля» при этом безобидно — оно не называет конкретное место.
HOTEL_NAME = re.compile(r"智选假日|Holiday\s*Inn|hiexdz", re.IGNORECASE)
HOTEL_CATEGORIES = {"CHECKIN", "CHECKOUT"}

# Категории BeijingTrip → StopCategory универсального приложения.
CATEGORY = {
    "FLIGHT": "FLIGHT", "AIRPORT": "FLIGHT", "TAXI": "TRANSIT",
    "METRO": "TRANSIT", "TRANSIT": "TRANSIT", "WALK": "TRANSIT",
    "CHECKIN": "HOTEL", "CHECKOUT": "HOTEL",
    "BREAKFAST": "FOOD", "LUNCH": "FOOD", "DINNER": "FOOD", "COFFEE": "FOOD",
    "SHOPPING": "SHOPPING", "TAXFREE": "SHOPPING",
    "SIGHT": "SIGHT", "PARK_ENTRY": "TICKET", "TICKET": "TICKET",
    "PARK_RIDE": "ACTIVITY", "PARK_SHOW": "ACTIVITY", "ACTIVITY": "ACTIVITY",
    "WAKE": "FREE_TIME", "PACKING": "FREE_TIME", "DEADLINE": "MEETING",
}

# Свежие подсказки, написанные для шаблона. Исходные заметки не переносятся:
# они написаны про конкретную поездку конкретных людей.
# Названия, которые потерялись вместе с личным текстом. Восстановлены вручную.
TITLE_FIXES = {
    "d2s1": "Касса Мутяньюй",
}

TIPS = {
    "d2s1": "Билеты именные и по паспорту — без него на стену не пустят.",
    "d2s3": "Канатка наверх, тобогган вниз. В дождь жёлоб закрывают — спрашивайте на кассе.",
    "d3s1": "Гугун пускает по предварительной записи и по паспорту. Записываться заранее.",
    "d3s4": "Вход в парк Цзиншань напротив северных ворот Гугуна — лучшая панорама города.",
    "d4s1": "В парк выгоднее приходить к открытию: очереди набираются к полудню.",
    "d5s2": "Возврат налога оформляют в магазине, а штамп ставят в аэропорту — закладывайте время.",
    "d1s4": "Магазины открываются в 10:00, раньше приезжать незачем.",
    "d6s0": "Регистрация на международные рейсы закрывается за 45–60 минут до вылета.",
}


def clean(value):
    """Строка без следов личного — или пустая строка."""
    if not isinstance(value, str) or not value.strip():
        return ""
    return "" if PERSONAL.search(value) else value.strip()


def minutes(duration: str | None) -> int | None:
    """«1,5 часа», «20 мин», «1 ч 55» → минуты. Диапазон округляем вниз."""
    if not duration:
        return None
    text = duration.replace("–", "-").replace("—", "-").split("-")[0]
    hours = re.search(r"(\d+(?:[.,]\d+)?)\s*(?:ч|час)", text)
    mins = re.search(r"(\d+)\s*мин", text)
    total = 0
    if hours:
        total += int(float(hours.group(1).replace(",", ".")) * 60)
    if mins:
        total += int(mins.group(1))
    return total or None


def address(raw: dict | None) -> dict | None:
    if not raw:
        return None
    local = clean(raw.get("zh"))
    display = clean(raw.get("label"))
    # pinyin в исходнике иногда содержит реплику для стойки — с номером брони.
    translit = clean(raw.get("pinyin"))
    if len(translit) > 120:
        translit = ""
    if not (local or display):
        return None
    out = {"local": local, "display": display, "transliteration": translit}
    if raw.get("lat") is not None and raw.get("lng") is not None:
        out["point"] = {"lat": raw["lat"], "lng": raw["lng"]}
    return out


def convert(src: dict) -> tuple[dict, list[str]]:
    dropped: list[str] = []

    days = []
    for index, day in enumerate(src.get("days", []), start=1):
        stops = []
        for stop in day.get("stops", []):
            title = clean(stop.get("titleRu"))
            local = clean(stop.get("titleZh"))
            # Проверяем только те поля, которые действительно переносим:
            # заметку мы и так выбрасываем, и упоминание отеля в ней не причина
            # терять сам этап.
            kept = json.dumps(
                [title, local, stop.get("pinyin"), stop.get("tag"), stop.get("address")],
                ensure_ascii=False,
            )

            if stop.get("category") in HOTEL_CATEGORIES or HOTEL_NAME.search(kept):
                dropped.append(f"этап {stop.get('id')}: отель")
                continue
            if not (title or local):
                dropped.append(f"этап {stop.get('id')}: личное в названии")
                continue

            stops.append({
                "key": stop.get("id", f"d{index}s{len(stops)}"),
                "orderIndex": len(stops),
                "title": TITLE_FIXES.get(stop.get("id")) or title or local,
                "localTitle": local,
                "transliteration": clean(stop.get("pinyin"))[:120],
                "startTime": stop.get("time", ""),
                "durationMinutes": minutes(stop.get("duration")),
                "category": CATEGORY.get(stop.get("category", ""), "OTHER"),
                "address": address(stop.get("address")),
                # Заметка автора не переносится — только свежая подсказка.
                "tip": TIPS.get(stop.get("id"), ""),
                "tag": clean(stop.get("tag")),
            })

        forks = []
        for fork in day.get("planB", []):
            options = [
                {
                    "key": clean(opt.get("key")) or "вариант",
                    "kind": {"go": "AS_PLANNED", "alt": "DETOUR", "stop": "CANCEL"}
                        .get(opt.get("kind", "go"), "AS_PLANNED"),
                    "text": clean(opt.get("text")),
                    "localTerms": [t for t in opt.get("terms", []) if clean(t)],
                }
                for opt in fork.get("options", [])
            ]
            options = [o for o in options if o["text"]]
            if len(options) < 2:
                dropped.append(f"развилка {fork.get('id')}: личное в вариантах")
                continue
            forks.append({
                "key": fork.get("id", ""),
                "question": clean(fork.get("question")),
                "localQuestion": clean(fork.get("zh")),
                "options": options,
                "stopKeys": fork.get("stops", []),
                "categories": [
                    CATEGORY.get(c, "OTHER") for c in fork.get("categories", [])
                ],
            })

        days.append({
            "dayIndex": index,
            "title": clean(day.get("title")),
            "label": f"День {index}",
            # lede исходника — рассказ про конкретный день конкретной поездки.
            "summary": "",
            "stops": stops,
            "forks": [f for f in forks if f["question"]],
        })

    places = []
    for place in src.get("places", []):
        # Как и у этапов: смотрим только переносимые поля. Заметка автора про
        # бронирование столика остаётся в личном плане и в шаблон не идёт.
        kept = json.dumps(
            [place.get("name"), place.get("nameZh"), place.get("address")],
            ensure_ascii=False,
        )
        if HOTEL_NAME.search(kept) or PERSONAL.search(kept):
            dropped.append(f"место {place.get('name')}")
            continue
        addr = address(place.get("address"))
        if not addr:
            continue
        places.append({
            "id": f"tpl-place-{len(places)}",
            "tripId": None,
            "name": clean(place.get("name")) or clean(place.get("nameZh")),
            "localName": clean(place.get("nameZh")),
            "transliteration": "",
            "address": addr,
            "kind": "SIGHT",
            "note": "",
            "source": "PLAN",
            "visited": False,
        })

    groups = []
    for group in src.get("phraseGroups", []):
        phrases = [
            {
                "id": f"tpl-phrase-{len(groups)}-{i}",
                "source": p.get("ru", ""),
                "target": p.get("zh", ""),
                "transliteration": p.get("py", ""),
            }
            for i, p in enumerate(group.get("phrases", []))
            if not PERSONAL.search(json.dumps(p, ensure_ascii=False))
        ]
        if not phrases:
            continue
        groups.append({
            "key": group.get("key", ""),
            "title": clean(group.get("titleRu")),
            "localTitle": clean(group.get("titleZh")),
            "sourceLocale": "ru-RU",
            "targetLocale": "zh-CN",
            "categories": [],
            "phrases": phrases,
            "builtin": True,
        })

    checklists = []
    for lst in src.get("checklists", []):
        items = [
            {"id": f"tpl-item-{len(checklists)}-{i}", "text": it.get("text", ""), "checked": False}
            for i, it in enumerate(lst.get("items", []))
            if not PERSONAL.search(it.get("text", ""))
        ]
        if not items:
            continue
        checklists.append({
            "id": f"tpl-list-{len(checklists)}",
            "tripId": None,
            "title": clean(lst.get("title")),
            "dayId": None,
            "builtin": True,
            "items": items,
        })

    bingo = [
        {"id": f"tpl-bingo-{i}", "text": c.get("text", ""), "emoji": c.get("emoji", ""),
         "builtin": True, "done": False}
        for i, c in enumerate(src.get("bingo", []))
        if not PERSONAL.search(c.get("text", ""))
    ]

    template = {
        "id": "beijing-6-days",
        "title": "Пекин за шесть дней",
        "summary": (
            "Стена, Гугун, Universal и улица призраков. Маршрут рассчитан на шесть "
            "полных дней; адреса записаны иероглифами — их можно показать таксисту."
        ),
        "destinations": [{
            "name": "Пекин",
            "countryCode": "CN",
            "localeTag": "zh-CN",
            "zoneId": "Asia/Shanghai",
            "currency": "CNY",
            "center": {"lat": 39.9042, "lng": 116.4074},
        }],
        "days": days,
        "places": places,
        "phraseGroups": groups,
        "checklists": checklists,
        "bingo": bingo,
        "author": {"displayName": "NORMss", "link": "https://github.com/NORMss"},
        "regionPackIds": ["asia-v1"],
        "tags": ["Китай", "город", "неделя"],
        "license": "CC-BY-4.0",
        "formatVersion": 1,
        "revision": 1,
    }
    return template, dropped


def main() -> int:
    source = Path(sys.argv[1] if len(sys.argv) > 1
                  else "../BeijingTrip/app/src/main/assets/trip.json")
    template, dropped = convert(json.loads(source.read_text()))

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(template, ensure_ascii=False, indent=2) + "\n")

    leftovers = PERSONAL.findall(OUT.read_text())
    print(f"дней {len(template['days'])}, этапов {sum(len(d['stops']) for d in template['days'])}, "
          f"мест {len(template['places'])}, групп фраз {len(template['phraseGroups'])}, "
          f"списков {len(template['checklists'])}, бинго {len(template['bingo'])}")
    print(f"выброшено элементов: {len(dropped)}")
    for item in dropped[:12]:
        print("  -", item)
    print(f"размер {OUT.stat().st_size // 1024} КБ")
    if leftovers:
        print("ОСТАЛИСЬ СЛЕДЫ ЛИЧНОГО:", sorted(set(leftovers)))
        return 1
    print("следов личного не найдено")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
