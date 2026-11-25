#!/usr/bin/env python3
"""
Draws the README's picture pieces - banner, buttons, badges, section headings and feature cards -
in the "map print" look of the Play listing: teal ground, coral sun, cream League Gothic, a
Pacifico line, Quicksand for everything small. Each piece is written twice, to docs/readme/light
and docs/readme/dark, and the README picks one with <picture> and prefers-color-scheme.

    uv run --with pillow scripts/readme/make_readme_art.py

PNG at twice the size it is shown, so it stays sharp on a retina screen. The text is drawn here
rather than set in an SVG, because GitHub shows an SVG as an image and an image cannot load fonts.
"""
import os

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
FONTS = os.path.join(os.path.dirname(__file__), "fonts")
OUT = os.path.join(ROOT, "docs", "readme")
SHOTS = os.path.join(ROOT, "docs", "screenshots")
ICON = os.path.join(ROOT, "docs", "icon-512.png")

S = 2  # drawn at 2x
TEAL, DEEP, CREAM, CORAL, SKY = "#00373E", "#002A30", "#F4EEDF", "#E8875B", "#CDE7EC"

THEMES = {
    "light": dict(
        head_bg=TEAL, head_fg=CREAM, head_sub=CORAL,
        card_bg="#F4EEDF", card_title=TEAL, card_text="#3D4A4D",
        badge_bg="#E3F1F2", badge_fg=TEAL,
        btn=[(TEAL, CREAM, None), (CORAL, TEAL, None), (None, TEAL, TEAL)],
    ),
    "dark": dict(
        head_bg="#0F3C43", head_fg=CREAM, head_sub=CORAL,
        card_bg="#13262A", card_title=CREAM, card_text="#B9C7CA",
        badge_bg="#12383D", badge_fg=SKY,
        btn=[(CREAM, TEAL, None), (CORAL, DEEP, None), (None, CREAM, CREAM)],
    ),
}


def font(name, size, weight=None):
    f = ImageFont.truetype(os.path.join(FONTS, name), size * S)
    if weight is not None:
        f.set_variation_by_axes([weight])
    return f


def gothic(size):
    return font("League_Gothic_Regular.ttf", size)


def script(size):
    return font("Pacifico_Regular.ttf", size)


def quick(size, weight=700):
    return font("Quicksand.ttf", size, weight)


def canvas(w, h):
    return Image.new("RGBA", (w * S, h * S), (0, 0, 0, 0))


def rounded(img, radius):
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, img.width - 1, img.height - 1), radius * S, fill=255)
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    out.paste(img, (0, 0), mask)
    return out


def text_w(d, txt, f, spacing=0):
    w = d.textlength(txt, font=f)
    return w + spacing * S * (len(txt) - 1)


def spaced(d, xy, txt, f, fill, spacing):
    x, y = xy
    for ch in txt:
        d.text((x, y), ch, font=f, fill=fill)
        x += d.textlength(ch, font=f) + spacing * S


def save(img, theme, name):
    os.makedirs(os.path.join(OUT, theme), exist_ok=True)
    img.save(os.path.join(OUT, theme, name), optimize=True)


def mountains(d, w, h, top, color):
    pts = [(0, 0.6), (0.18, 0.2), (0.34, 0.55), (0.52, 0.1), (0.7, 0.5), (0.86, 0.25), (1, 0.55)]
    poly = [(x * w * S, (top + y * (h - top)) * S) for x, y in pts] + [(w * S, h * S), (0, h * S)]
    d.polygon(poly, fill=color)


# --- banner: one for both themes, it brings its own ground -------------------------------------

def banner():
    w, h = 880, 260
    img = canvas(w, h)
    bg = Image.new("RGBA", img.size, TEAL)
    d = ImageDraw.Draw(bg)
    d.ellipse(((w - 330) * S, 24 * S, (w - 110) * S, 244 * S), fill=CORAL)
    mountains(d, w, h, h - 84, DEEP)
    icon = Image.open(ICON).convert("RGBA").resize((92 * S, 92 * S), Image.LANCZOS)
    bg.paste(rounded(icon, 22), (40 * S, 66 * S), rounded(icon, 22))
    spaced(d, (152 * S, 50 * S), "ADVENTURELOG", gothic(76), CREAM, 5)
    d.text((156 * S, 138 * S), "every trip, in one journal", font=script(26), fill=CORAL)
    d.text((158 * S, 194 * S), "for your self-hosted AdventureLog server", font=quick(15, 600), fill=SKY)
    for name, width, x, y, angle in (("calendar.png", 128, w - 300, 30, -6), ("home.png", 140, w - 160, 14, 5)):
        phone = Image.open(os.path.join(SHOTS, name)).convert("RGBA")
        phone = phone.resize((width * S, int(phone.height * width / phone.width) * S), Image.LANCZOS)
        phone = phone.rotate(angle, resample=Image.BICUBIC, expand=True)
        bg.alpha_composite(phone, (x * S, y * S))
    img = rounded(bg, 18)
    for theme in THEMES:
        save(img, theme, "banner.png")


# --- buttons and badges ------------------------------------------------------------------------

def button(theme, name, label, kind):
    fill, fg, outline = THEMES[theme]["btn"][kind]
    f = quick(16)
    probe = ImageDraw.Draw(canvas(1, 1))
    w = int(text_w(probe, label, f) / S) + 48
    h = 48
    img = canvas(w, h)
    d = ImageDraw.Draw(img)
    box = (S, S, w * S - S, h * S - S)
    if fill:
        d.rounded_rectangle(box, h * S, fill=fill)
    if outline:
        d.rounded_rectangle(box, h * S, outline=outline, width=2 * S)
    d.text((w * S / 2, h * S / 2), label, font=f, fill=fg, anchor="mm")
    save(img, theme, name)


def badge(theme, name, label, construction=False):
    t = THEMES[theme]
    f = quick(12)
    probe = ImageDraw.Draw(canvas(1, 1))
    w = int(text_w(probe, label, f) / S) + (30 if construction else 22)
    h = 26
    img = canvas(w, h)
    d = ImageDraw.Draw(img)
    if construction:
        stripes = Image.new("RGBA", img.size, "#1D1D1F")
        sd = ImageDraw.Draw(stripes)
        for x in range(-h * S, w * S + h * S, 12 * S):
            sd.polygon([(x, h * S), (x + 6 * S, h * S), (x + 6 * S + h * S, 0), (x + h * S, 0)], fill="#F6C445")
        img = rounded(stripes, 13)
        d = ImageDraw.Draw(img)
        d.rounded_rectangle((4 * S, 4 * S, (w - 4) * S, (h - 4) * S), 9 * S, fill="#1D1D1F")
        d.text((w * S / 2, h * S / 2), label, font=quick(11), fill="#FFFFFF", anchor="mm")
    else:
        d.rounded_rectangle((0, 0, w * S - 1, h * S - 1), h * S, fill=t["badge_bg"])
        d.text((w * S / 2, h * S / 2), label, font=f, fill=t["badge_fg"], anchor="mm")
    save(img, theme, name)


# --- section headings --------------------------------------------------------------------------

def heading(theme, name, title, aside=None, construction=False):
    t = THEMES[theme]
    w, h = 880, 56
    img = canvas(w, h)
    bg = Image.new("RGBA", img.size, t["head_bg"])
    d = ImageDraw.Draw(bg)
    mountains(d, w, h, h - 14, DEEP if theme == "light" else "#0A2B30")
    spaced(d, (20 * S, 9 * S), title, gothic(34), t["head_fg"], 2)
    if aside:
        d.text(((w - 20) * S, h * S / 2), aside, font=script(17), fill=t["head_sub"], anchor="rm")
    img = rounded(bg, 14)
    if construction:
        tag = "UNDER CONSTRUCTION"
        f = quick(11)
        d = ImageDraw.Draw(img)
        tw = int(d.textlength(tag, font=f) / S) + 30
        x0, y0, th = w - 20 - tw, (h - 26) // 2, 26
        stripes = Image.new("RGBA", (tw * S, th * S), "#1D1D1F")
        sd = ImageDraw.Draw(stripes)
        for x in range(-th * S, tw * S + th * S, 12 * S):
            sd.polygon([(x, th * S), (x + 6 * S, th * S), (x + 6 * S + th * S, 0), (x + th * S, 0)], fill="#F6C445")
        stripes = rounded(stripes, 13)
        sdd = ImageDraw.Draw(stripes)
        sdd.rounded_rectangle((4 * S, 4 * S, (tw - 4) * S, (th - 4) * S), 9 * S, fill="#1D1D1F")
        sdd.text((tw * S / 2, th * S / 2), tag, font=f, fill="#FFFFFF", anchor="mm")
        img.alpha_composite(stripes, (x0 * S, y0 * S))
    save(img, theme, name)


# --- feature cards -----------------------------------------------------------------------------

def wrap(d, txt, f, width):
    words, lines, line = txt.split(), [], ""
    for word in words:
        trial = (line + " " + word).strip()
        if d.textlength(trial, font=f) <= width:
            line = trial
        else:
            lines.append(line)
            line = word
    lines.append(line)
    return lines


def card(theme, name, mark, mark_bg, mark_fg, title, body):
    t = THEMES[theme]
    if theme == "dark" and mark_bg == DEEP:
        mark_bg = "#24525A"  # the deep teal disappears on the dark card
    w, h = 432, 104
    img = canvas(w, h)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((0, 0, w * S - 1, h * S - 1), 18 * S, fill=t["card_bg"])
    d.rounded_rectangle((18 * S, 22 * S, 78 * S, 82 * S), 18 * S, fill=mark_bg)
    d.text((48 * S, 53 * S), mark, font=gothic(30), fill=mark_fg, anchor="mm")
    d.text((96 * S, 20 * S), title, font=quick(18), fill=t["card_title"])
    f = quick(13.5, 500)
    for i, line in enumerate(wrap(d, body, f, (w - 116) * S)[:3]):
        d.text((96 * S, (48 + i * 18) * S), line, font=f, fill=t["card_text"])
    save(img, theme, name)


FEATURES = [
    ("card-places.png", "PL", TEAL, CREAM, "Places", "Photos, notes, links, dates, ratings and categories - all searchable."),
    ("card-collections.png", "CO", CORAL, TEAL, "Collections", "Trips that gather places, transport, lodging, notes, checklists and a day-by-day plan."),
    ("card-map.png", "MA", SKY, TEAL, "Map and calendar", "Everything with coordinates, clustered; everything with a date, as an agenda."),
    ("card-world.png", "WO", DEEP, CORAL, "World", "Countries, regions and cities visited, with a stamp for each country."),
    ("card-search.png", "SE", "#D8DEFF", "#2D3A8C", "Search", "One box for places, collections, cities and countries."),
    ("card-tablets.png", "TA", "#9DEFFB", TEAL, "Phones and tablets", "Navigation rail, adaptive grids and list and detail side by side."),
]

HEADINGS = [
    ("h-screenshots.png", "SCREENSHOTS", "Android", False),
    ("h-ios.png", "iOS", "Liquid Glass", True),
    ("h-features.png", "FEATURES", None, False),
    ("h-install.png", "INSTALL", "self-hosted", False),
    ("h-architecture.png", "ARCHITECTURE", None, False),
    ("h-build.png", "BUILD FROM SOURCE", None, False),
    ("h-tests.png", "TESTS", None, False),
    ("h-issues.png", "KNOWN ISSUES", None, False),
    ("h-license.png", "LICENSE", None, False),
]

BADGES = [
    ("badge-selfhosted.png", "Self-hosted", False),
    ("badge-kmp.png", "Kotlin Multiplatform", False),
    ("badge-m3e.png", "Material 3 Expressive", False),
    ("badge-android.png", "Android 7.0+", False),
    ("badge-ios.png", "iOS - UNDER CONSTRUCTION", True),
    ("badge-servers.png", "Servers 0.13 and 0.14", False),
    ("badge-mit.png", "MIT", False),
]

BUTTONS = [
    ("btn-play.png", "Get it on Google Play", 0),
    ("btn-apk.png", "Download the APK", 1),
    ("btn-server.png", "Host the server", 2),
]


def main():
    banner()
    for theme in THEMES:
        for name, label, kind in BUTTONS:
            button(theme, name, label, kind)
        for name, label, construction in BADGES:
            badge(theme, name, label, construction)
        for name, title, aside, construction in HEADINGS:
            heading(theme, name, title, aside, construction)
        for spec in FEATURES:
            card(theme, *spec)
    print("written to", OUT)


if __name__ == "__main__":
    main()
