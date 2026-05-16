"""
One-shot logo asset generator.

Reads docs/logo samples/unilosttablogo.png, removes the near-white background,
trims to the visible mark, and writes:

- res/drawable-{mdpi..xxxhdpi}/ic_unilost_logo.png       (transparent, for AuthLogoHeader)
- res/mipmap-{mdpi..xxxhdpi}/ic_launcher.webp            (square, white background)
- res/mipmap-{mdpi..xxxhdpi}/ic_launcher_round.webp      (circle-masked white background)
- res/mipmap-{mdpi..xxxhdpi}/ic_launcher_foreground.png  (transparent, 108dp viewport with 72dp safe-zone padding)

Adaptive icon XML already references @drawable/ic_launcher_foreground (currently a
placeholder vector). After running this we'll edit the adaptive-icon xmls to point
at @mipmap/ic_launcher_foreground instead, and replace the slate background with white.
"""
from __future__ import annotations

from pathlib import Path
from PIL import Image, ImageDraw

REPO = Path(__file__).resolve().parents[2]
SRC = REPO / "docs" / "logo samples" / "unilosttablogo.png"
RES = REPO / "mobile" / "app" / "src" / "main" / "res"

# Density buckets and their mdpi multipliers.
DENSITIES = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}

# Auth header is rendered at 28dp in the Row. We export the mark at 32dp baseline
# so it has a hair of padding room and stays crisp if the size is ever bumped.
AUTH_HEADER_BASE_DP = 32

# Launcher legacy raster sizes (square) per density. Mirrors Android Studio output.
LAUNCHER_SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

# Adaptive icon foreground viewport is 108dp; the visible "safe zone" is the
# centered 72dp circle/square. We render the mark inside the 72dp safe zone
# with transparent padding out to 108dp so the launcher can crop freely.
ADAPTIVE_VIEWPORT_DP = 108
ADAPTIVE_SAFE_ZONE_DP = 72


def load_transparent_logo() -> Image.Image:
    """Open the source PNG, drop near-white pixels to transparent, trim."""
    img = Image.open(SRC).convert("RGBA")
    px = img.load()
    w, h = img.size
    # The source background is pure white (1024x1024 with the mark centered).
    # Any pixel where all RGB channels are above the threshold gets alpha=0.
    threshold = 245
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if r >= threshold and g >= threshold and b >= threshold:
                px[x, y] = (r, g, b, 0)
    # Trim transparent padding so downstream resizing is tight.
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    return img


def fit_into_square(logo: Image.Image, size_px: int, padding_ratio: float = 0.0) -> Image.Image:
    """Center `logo` into a transparent square of `size_px`, preserving aspect."""
    inner = max(1, int(round(size_px * (1.0 - padding_ratio))))
    lw, lh = logo.size
    scale = min(inner / lw, inner / lh)
    new_w = max(1, int(round(lw * scale)))
    new_h = max(1, int(round(lh * scale)))
    resized = logo.resize((new_w, new_h), Image.LANCZOS)
    canvas = Image.new("RGBA", (size_px, size_px), (0, 0, 0, 0))
    canvas.paste(resized, ((size_px - new_w) // 2, (size_px - new_h) // 2), resized)
    return canvas


def write_auth_drawables(logo: Image.Image) -> None:
    for density, mult in DENSITIES.items():
        target_px = int(round(AUTH_HEADER_BASE_DP * mult))
        out_dir = RES / f"drawable-{density}"
        out_dir.mkdir(parents=True, exist_ok=True)
        out = fit_into_square(logo, target_px)
        out.save(out_dir / "ic_unilost_logo.png", format="PNG", optimize=True)
        print(f"drawable-{density}/ic_unilost_logo.png  ({target_px}x{target_px})")


def write_adaptive_foreground(logo: Image.Image) -> None:
    # The XHDPI mdpi-multiplier is 2.0, so 108dp at xxxhdpi == 432 px etc.
    # We allocate enough pixels for the *viewport* and inset the logo to the safe zone.
    padding_ratio = 1.0 - (ADAPTIVE_SAFE_ZONE_DP / ADAPTIVE_VIEWPORT_DP)  # ~0.333
    for density, mult in DENSITIES.items():
        viewport_px = int(round(ADAPTIVE_VIEWPORT_DP * mult))
        out_dir = RES / f"mipmap-{density}"
        out_dir.mkdir(parents=True, exist_ok=True)
        out = fit_into_square(logo, viewport_px, padding_ratio=padding_ratio)
        out.save(out_dir / "ic_launcher_foreground.png", format="PNG", optimize=True)
        print(f"mipmap-{density}/ic_launcher_foreground.png  ({viewport_px}x{viewport_px})")


def write_legacy_launcher(logo: Image.Image) -> None:
    """Legacy square + round mipmap launcher icons on a white background."""
    # The legacy icon includes the visual padding itself; we inset the mark a
    # little so it doesn't touch the edges (matches Android Studio's behavior).
    inset_ratio = 0.18
    for density, size_px in LAUNCHER_SIZES.items():
        out_dir = RES / f"mipmap-{density}"
        out_dir.mkdir(parents=True, exist_ok=True)

        # --- Square ---
        square_bg = Image.new("RGBA", (size_px, size_px), (255, 255, 255, 255))
        mark = fit_into_square(logo, size_px, padding_ratio=inset_ratio)
        square_bg.paste(mark, (0, 0), mark)
        square_bg.save(out_dir / "ic_launcher.webp", format="WEBP", quality=95, method=6)

        # --- Round (mask onto a circle, transparent corners) ---
        round_bg = Image.new("RGBA", (size_px, size_px), (255, 255, 255, 255))
        round_bg.paste(mark, (0, 0), mark)
        mask = Image.new("L", (size_px, size_px), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, size_px - 1, size_px - 1), fill=255)
        round_out = Image.new("RGBA", (size_px, size_px), (0, 0, 0, 0))
        round_out.paste(round_bg, (0, 0), mask)
        round_out.save(out_dir / "ic_launcher_round.webp", format="WEBP", quality=95, method=6)

        print(f"mipmap-{density}/ic_launcher.webp + ic_launcher_round.webp  ({size_px}x{size_px})")


def main() -> None:
    if not SRC.exists():
        raise SystemExit(f"Source logo not found: {SRC}")
    print(f"Source: {SRC}")
    logo = load_transparent_logo()
    print(f"Trimmed transparent logo: {logo.size}")
    write_auth_drawables(logo)
    write_adaptive_foreground(logo)
    write_legacy_launcher(logo)
    print("Done.")


if __name__ == "__main__":
    main()
