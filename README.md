# Stevelander

A minimal Fabric client mod for Minecraft 26.2 that provides basic hacks

| Feature | Default bind | Behaviour |
| --- | --- | --- |
| Flight | `Right Ctrl` | Flies the player and vehicles |
| X-Ray | `Right Shift` | X-Ray |
| AntiHunger | always on | Never lose hunger |
| NoFall | always on | Never take fall damage |

The binds are polled directly rather than registered as vanilla key mappings, so
they never show up in the controls screen.

## Configuration

`config/stevelander.json` is written with defaults on first launch and read at startup.

```json
{
  "keybinds": {
    "flight": "RIGHT_CONTROL",
    "xray": "RIGHT_SHIFT"
  },
  "flight": {
    "glide": 0.0,
    "bypassVanillaCheck": true,
    "disableOnSetback": false,
    "baseSpeed": { "horizontal": 0.44, "vertical": 0.44 },
    "sprintSpeed": { "enabled": true, "horizontal": 1.0, "vertical": 1.0 },
    "noFall": {
      "enabled": true,
      "mode": "SPOOF_LANDING",
      "landingOffset": { "x": 0.0, "y": 0.0, "z": 0.0 },
      "resetFallDistance": true
    },
    "vehicle": {
      "enabled": true,
      "baseSpeed": { "horizontal": 0.5, "vertical": 0.35 },
      "sprintSpeed": { "enabled": true, "horizontal": 5.0, "vertical": 2.0 },
      "glide": -0.15,
      "mouseControl": false,
      "noGlideOnSprint": false,
      "sneakDescends": true
    }
  },
  "antiHunger": {
    "enabled": true,
    "keepFloating": true,
    "noSprint": true,
    "noSprintWhileSwimming": false
  }
}
```

Key names are GLFW key names without the `GLFW_KEY_` prefix

| Flight setting | Default | Meaning |
| --- | --- | --- |
| `glide` | `0.0` | Vertical drift when neither jump nor sneak is held; negative sinks |
| `bypassVanillaCheck` | `true` | Briefly sink every 40 ticks so a vanilla server does not kick for flying |
| `disableOnSetback` | `false` | Switch flight off when the server drags the player back |
| `baseSpeed` | `0.44` / `0.44` | Blocks per tick, horizontal and vertical |
| `sprintSpeed` | `1.0` / `1.0` | Speeds used while the sprint key is held; `enabled: false` to always use `baseSpeed` |

| `noFall` setting | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Cancel fall damage while flight is on |
| `mode` | `SPOOF_LANDING` | `SPOOF_LANDING` or `NO_GROUND` |
| `landingOffset` | `0 / 0 / 0` | Position displacement to provoke a setback on the swallowed landing. Off by default. |
| `resetFallDistance` | `true` | Also clear the client's own fall distance |

| `vehicle` setting | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Fly boats, minecarts, horses and anything else ridden, while flight is on |
| `baseSpeed` | `0.5` / `0.35` | Blocks per tick, horizontal and vertical |
| `sprintSpeed` | `5.0` / `2.0` | Speeds while the sprint key is held |
| `glide` | `-0.15` | Vertical drift with neither jump nor sneak held |
| `mouseControl` | `false` | Steer with the mouse rather than the vehicle's own heading |
| `noGlideOnSprint` | `false` | Suppress the glide while sprinting |
| `sneakDescends` | `true` | Sneak lowers the vehicle instead of dismounting; dismount still works once it is resting |

| `antiHunger` setting | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Master switch for the always-on hunger prevention |
| `keepFloating` | `true` | Report never standing on the ground, where movement exhaustion is charged |
| `noSprint` | `true` | Do not sprint — the other source of exhaustion. **You will not be able to sprint on foot** |
| `noSprintWhileSwimming` | `false` | Whether `noSprint` also applies while swimming |

## Building

```sh
./gradlew build
```

The mod jar lands in `build/libs/stevelander-1.0.0.jar`. Drop it into `.minecraft/mods`
alongside Fabric Loader 0.19.3+ and Fabric API.

To launch a development client:

```sh
./gradlew runClient
```