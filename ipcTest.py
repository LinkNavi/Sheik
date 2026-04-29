import mmap
import struct
import time

SHM_PATH = "/dev/shm/sheik"

# Order matches writeState():
# int lookingAtBlock, int heldItem, float yaw, float pitch, int targetId, float targetHealth, byte inGui
FMT = "<iiffif?"
SIZE = struct.calcsize(FMT)

with open(SHM_PATH, "rb") as f:
    mm = mmap.mmap(f.fileno(), SIZE, access=mmap.ACCESS_READ)
    while True:
        mm.seek(0)
        data = struct.unpack(FMT, mm.read(SIZE))
        lookingAtBlock, heldItem, yaw, pitch, targetId, targetHealth, inGui = data
        print(
            f"Block: {bool(lookingAtBlock)} | Held: {heldItem} | Yaw: {yaw:.1f} | Pitch: {pitch:.1f} | Target: {targetId} (HP: {targetHealth:.1f}) | GUI: {inGui}"
        )
        time.sleep(0.1)
