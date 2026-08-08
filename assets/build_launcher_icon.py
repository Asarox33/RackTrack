"""Build Play 512 + adaptive-safe launcher assets from icon C."""

from pathlib import Path

from PIL import Image

SRC = Path(__file__).with_name("racktrack-icon-c-rack.png")
OUT = Path(__file__).parent
RES = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"

FELT = (15, 74, 36, 255)  # #0F4A24 Forest
# Center safe zone for adaptive masks / Play iconography (~18% margin each side).
SAFE = 0.66
# Trim baked-in rounded-card margins from the generated source.
SOURCE_TRIM = 0.06


def prepare_source(src: Image.Image) -> Image.Image:
    w, h = src.size
    trim = int(min(w, h) * SOURCE_TRIM)
    return src.crop((trim, trim, w - trim, h - trim))


def make_icon(src: Image.Image, size: int, safe: float = SAFE) -> Image.Image:
    canvas = Image.new("RGBA", (size, size), FELT)
    content = int(round(size * safe))
    if content % 2 != size % 2:
        content -= 1
    scaled = src.resize((content, content), Image.Resampling.LANCZOS)
    off = (size - content) // 2
    canvas.alpha_composite(scaled, (off, off))
    return canvas.convert("RGB")


def main() -> None:
    src = prepare_source(Image.open(SRC).convert("RGBA"))

    make_icon(src, 512).save(OUT / "ic_launcher_play_512.png", "PNG", optimize=True)
    make_icon(src, 1024).save(OUT / "ic_launcher_adaptive_fg_1024.png", "PNG", optimize=True)

    drawable = RES / "drawable"
    drawable.mkdir(parents=True, exist_ok=True)
    # 108dp @ xxxhdpi = 432px adaptive foreground layer
    make_icon(src, 432).save(drawable / "ic_launcher_foreground.png", "PNG", optimize=True)

    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    for folder, size in densities.items():
        d = RES / folder
        d.mkdir(parents=True, exist_ok=True)
        img = make_icon(src, size)
        img.save(d / "ic_launcher.png", "PNG", optimize=True)
        img.save(d / "ic_launcher_round.png", "PNG", optimize=True)

    print(f"Wrote Play 512 + mipmaps + foreground (safe={SAFE}, trim={SOURCE_TRIM})")


if __name__ == "__main__":
    main()
