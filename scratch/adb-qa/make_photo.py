from pathlib import Path

# Minimal valid 1x1 JPEG
jpeg = bytes.fromhex(
    "ffd8ffe000104a46494600010100000100010000ffdb004300080606070605080707070909080a0c140d0c0b0b0c191213"
    "0f141d1a1f1e1d1a1c1c20242e2720222c231c1c2837292c30313434341f27393d38323c2e333432ffc0000b0800010001"
    "01011ffc4004310000000000000000ffda0008010100003f00d2cf20ffd9"
)
Path(r"C:\Users\edwar\OpenShouter\scratch\adb-qa\qa-photo.jpg").write_bytes(jpeg)
print("wrote", len(jpeg))
