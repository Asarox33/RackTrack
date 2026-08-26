"""Build Play-aligned launcher mipmaps + adaptive FG from ic_launcher_play_512.png."""

from pathlib import Path

from PIL import Image

SRC = Path(__file__).with_name("ic_launcher_play_512.png")
OUT = Path(__file__).parent
RES = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"

# Blue glossy background (AppThemeMode.BLUE_GLOSSY)
BG = (16, 26, 40, 255)  # #101A28
SAFE = 0.66


def make_layer(src: Image.Image, size: int, *, transparent_pad: bool) -> Image.Image:
    """Scale artwork into the adaptive safe zone; pad with BG or transparency."""
    canvas = Image.new(
        "RGBA",
        (size, size),
        (0, 0, 0, 0) if transparent_pad else BG,
    )
    content = int(round(size * SAFE))
    if content % 2 != size % 2:
        content -= 1
    scaled = src.resize((content, content), Image.Resampling.LANCZOS)
    if scaled.mode != "RGBA":
        scaled = scaled.convert("RGBA")
    off = (size - content) // 2
    canvas.alpha_composite(scaled, (off, off))
    return canvas


def main() -> None:
    src = Image.open(SRC).convert("RGBA")

    # Legacy / Play listing master stays as-is; rebuild in-app layers only.
    drawable = RES / "drawable"
    drawable.mkdir(parents=True, exist_ok=True)

    # Adaptive FG @ xxxhdpi 108dp = 432px (safe-zone artwork).
    make_layer(src, 432, transparent_pad=False).save(
        drawable / "ic_launcher_foreground.png",
        "PNG",
        optimize=True,
    )
    # Splash animated icon (same art for cold start).
    make_layer(src, 288, transparent_pad=False).save(
        drawable / "ic_splash_brand.png",
        "PNG",
        optimize=True,
    )

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
        img = make_layer(src, size, transparent_pad=False).convert("RGB")
        img.save(d / "ic_launcher.png", "PNG", optimize=True)
        img.save(d / "ic_launcher_round.png", "PNG", optimize=True)

    # Keep a 1024 adaptive FG master next to Play assets for future re-exports.
    make_layer(src, 1024, transparent_pad=False).convert("RGB").save(
        OUT / "ic_launcher_adaptive_fg_1024.png",
        "PNG",
        optimize=True,
    )

    print(f"Wrote mipmaps + foreground + splash from {SRC.name} (safe={SAFE}, bg=#101A28)")


if __name__ == "__main__":
    main()
