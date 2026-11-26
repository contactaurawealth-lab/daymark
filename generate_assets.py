import os
from PIL import Image, ImageDraw

SIZE = 1024
SCALE = 4

# Create high-resolution canvas
img = Image.new(
    "RGBA",
    (SIZE * SCALE, SIZE * SCALE),
    (245, 242, 234, 255)
)

draw = ImageDraw.Draw(img)

# Simple elegant tick
points = [
    (270 * SCALE, 530 * SCALE),
    (440 * SCALE, 690 * SCALE),
    (760 * SCALE, 340 * SCALE)
]

draw.line(
    points,
    fill="#91A68A",
    width=75 * SCALE,
    joint="curve"
)

# Rounded endpoints
for x, y in [(270, 530), (440, 690), (760, 340)]:
    r = 37 * SCALE
    draw.ellipse(
        (x*SCALE-r, y*SCALE-r, x*SCALE+r, y*SCALE+r),
        fill="#91A68A"
    )

# Smooth edges
full_logo = img.resize(
    (SIZE, SIZE),
    Image.Resampling.LANCZOS
)

os.makedirs("/root/daymark", exist_ok=True)
full_logo.save("/root/daymark/daymark_tick.png")
print("Saved /root/daymark/daymark_tick.png")

# Also create round launcher icons
mask = Image.new("L", (SIZE, SIZE), 0)
mask_draw = ImageDraw.Draw(mask)
mask_draw.ellipse((0, 0, SIZE, SIZE), fill=255)
round_logo = full_logo.copy()
round_logo.putalpha(mask)

# Android mipmap densities:
densities = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

res_dir = "/root/daymark/app/src/main/res"

for folder, dim in densities.items():
    target_dir = os.path.join(res_dir, folder)
    os.makedirs(target_dir, exist_ok=True)
    
    # ic_launcher.png
    resized = full_logo.resize((dim, dim), Image.Resampling.LANCZOS)
    resized.save(os.path.join(target_dir, "ic_launcher.png"))
    
    # ic_launcher_round.png
    resized_round = round_logo.resize((dim, dim), Image.Resampling.LANCZOS)
    resized_round.save(os.path.join(target_dir, "ic_launcher_round.png"))

# Notification small icon (white tick on transparent background)
noti_size = 96
noti_img = Image.new("RGBA", (noti_size * 4, noti_size * 4), (0, 0, 0, 0))
noti_draw = ImageDraw.Draw(noti_img)
noti_scale = noti_size * 4 / SIZE

noti_points = [(p[0] * noti_scale / SCALE, p[1] * noti_scale / SCALE) for p in points]
noti_draw.line(noti_points, fill="#FFFFFF", width=int(75 * noti_scale), joint="curve")
for x, y in [(270, 530), (440, 690), (760, 340)]:
    r = int(37 * noti_scale)
    cx, cy = int(x * noti_scale), int(y * noti_scale)
    noti_draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill="#FFFFFF")

noti_final = noti_img.resize((noti_size, noti_size), Image.Resampling.LANCZOS)
os.makedirs(os.path.join(res_dir, "drawable"), exist_ok=True)
noti_final.save(os.path.join(res_dir, "drawable", "ic_stat_notification.png"))
noti_final.save(os.path.join(res_dir, "drawable", "ic_stat_timer.png"))

print("All Android launcher assets generated successfully!")
