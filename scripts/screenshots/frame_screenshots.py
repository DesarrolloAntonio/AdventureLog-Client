#!/usr/bin/env python3
"""
Puts raw phone captures into the plain phone frame the README uses: a dark rounded bezel with a
one-pixel highlight and a soft drop shadow, on a transparent background, 482 x 995.

    uv run --with pillow scripts/screenshots/frame_screenshots.py RAW_DIR

RAW_DIR holds full-resolution `adb exec-out screencap -p` PNGs named after the README's shots
(home.png, places.png, ...); each one is written to docs/screenshots/ under the same name.
No colour quantisation: it flattened the hero's gradient once.
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = os.path.join(ROOT, "docs", "screenshots")

SS = 4                                  # supersampling, for smooth corners
W, H = 482, 995                         # final canvas
OUTER = (21, 21, 461, 973)              # bezel, including its highlight
SCREEN = (31, 31, 451, 963)             # 420 x 932: a 1080 x 2400 capture scaled down
OUTER_R, SCREEN_R = 54, 46
HIGHLIGHT, BEZEL = (72, 76, 80, 255), (24, 26, 28, 255)
SHADOW_ALPHA, SHADOW_BLUR, SHADOW_DY = 90, 10, 4


def box(b, inset=0):
    return tuple(v * SS + (inset if i < 2 else -inset) for i, v in enumerate(b))


def frame(raw):
    shot = Image.open(raw).convert("RGB")
    sw, sh = (SCREEN[2] - SCREEN[0]) * SS, (SCREEN[3] - SCREEN[1]) * SS

    canvas = Image.new("RGBA", (W * SS, H * SS), (0, 0, 0, 0))

    shadow = Image.new("L", canvas.size, 0)
    ImageDraw.Draw(shadow).rounded_rectangle(box(OUTER), OUTER_R * SS, fill=SHADOW_ALPHA)
    shadow = shadow.filter(ImageFilter.GaussianBlur(SHADOW_BLUR * SS))
    black = Image.new("RGBA", canvas.size, (0, 0, 0, 255))
    canvas.paste(black, (0, SHADOW_DY * SS), shadow)

    d = ImageDraw.Draw(canvas)
    d.rounded_rectangle(box(OUTER), OUTER_R * SS, fill=HIGHLIGHT)
    d.rounded_rectangle(box(OUTER, SS), OUTER_R * SS - SS, fill=BEZEL)

    mask = Image.new("L", (sw, sh), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, sw - 1, sh - 1), SCREEN_R * SS, fill=255)
    canvas.paste(shot.resize((sw, sh), Image.LANCZOS), (SCREEN[0] * SS, SCREEN[1] * SS), mask)

    return canvas.resize((W, H), Image.LANCZOS)


def main(raw_dir):
    for name in sorted(os.listdir(raw_dir)):
        if name.endswith(".png"):
            frame(os.path.join(raw_dir, name)).save(os.path.join(OUT, name), optimize=True)
            print(name)


if __name__ == "__main__":
    main(sys.argv[1])
