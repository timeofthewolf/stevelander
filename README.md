# Stevelander

A minimal Fabric client mod for Minecraft **26.2** with exactly two features.

| Feature | Default bind | Behaviour |
| --- | --- | --- |
| Flight | `Right Ctrl` | Flies vehicles too, and cancels fall damage |
| X-Ray | `Right Shift` | Hides everything except ores, storage and points of interest; forces full-bright |
| AntiHunger | always on | Never lose hunger |

The binds are polled directly rather than registered as vanilla key mappings, so
they never show up in the controls screen. Both toggles are ignored while chat,
an inventory or any other screen holds focus.

## Configuration

`config/stevelander.json` is written with defaults on first launch and read at
startup — **edit it with the game closed**.

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

Key names are GLFW key names without the `GLFW_KEY_` prefix — `F`, `X`, `V`,
`RIGHT_CONTROL`, `LEFT_ALT`, `GRAVE_ACCENT`, `KP_0` and so on. An unknown name
logs an error and falls back to the default bind.

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
| `mode` | `SPOOF_LANDING` | `SPOOF_LANDING` or `NO_GROUND` — see below. An unrecognised name logs an error and falls back |
| `landingOffset` | `0 / 0 / 0` | Position displacement to provoke a setback on the swallowed landing. Off by default — it does not clear the fall distance and only causes a rubber-band. `1337/0/1337` for upstream's behaviour |
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

## Flight fidelity

- Enabling **revokes** `mayfly` rather than granting it, restoring whatever the
  server last handed out when switched off. It is not creative flight — that is
  a separate mode upstream.
- Movement is driven by writing `deltaMovement` every tick: horizontal velocity
  is replaced with a strafe along the movement keys' direction, vertical is
  `+speed` on jump, `-speed` on sneak, `glide` otherwise.
- `bypassVanillaCheck` reproduces the upstream coroutine exactly — arm on
  `tickCount % 40 == 0`, sink at `-0.04` the following tick, idle the tick after.
- Incoming `ClientboundPlayerAbilitiesPacket`s update the remembered flight
  permission, and `ClientboundPlayerPositionPacket`s trigger setback detection.

Fall damage cancellation is a port of upstream's separate `NoFall` module, wired
to switch on and off with flight rather than being its own toggle. Two of
upstream's ~20 modes are included; the rest are not.

Worth knowing what the server actually does, because which flag helps is not
obvious. `handleMovePlayer` first replays the movement through
`ServerPlayer.move`, whose internal `checkFallDamage` accumulates fall distance
from the server's **own collision result** — the packet flag has no say there.
Only afterwards does `doCheckFallDamage` consult the packet, and `onGround =
true` takes the branch that cashes the accumulated distance in as damage. So
reporting `true` while airborne is a synthetic landing that *causes* damage, and
reporting `false` only defers the bill until a real touchdown.

**`SPOOF_LANDING`** (default, after upstream's mode by jiuxian_baka) is the one
that actually cancels the damage. It leaves ordinary movement alone until the
touchdown of a fall that would have hurt, then:

1. Forces `onGround = false` on that packet and every packet for the next few
   ticks, so the server is never told the player landed and never bills it.
2. Makes flight climb briefly (`0.42`/tick). This is the part that clears the
   debt: `handleMovePlayer` calls `resetFallDistance()` on exactly one
   condition — that the movement it was told about went **upward**. The climb
   outranks the vanilla-check bypass, whose sinking tick would otherwise undo it.

The reset condition is worth spelling out because it makes upstream's forced
jump look cosmetic when it is in fact the whole mechanism. The 1337-block
position displacement is *not* what resets anything — the "moved too quickly"
branch returns before reaching the reset — so `landingOffset` defaults to zero
here, since the setback it provokes only costs a rubber-band. Set it to
`1337/0/1337` for upstream's exact behaviour; the self-inflicted setback is
recognised and will not trip `disableOnSetback`.

**`NO_GROUND`** reports `onGround = false` on every packet. Simpler, and enough
to stop damage while airborne, but it only defers: the server keeps accumulating
fall distance and can charge it when you genuinely touch down. Available as a
fallback.

The decision of whether a landing is dangerous is made against **our own copy of
the server's fall distance**, kept by the server's rules — grow by every
reported drop, zero on every reported climb. The client's `fallDistance` cannot
stand in for it: the client clears it the instant it touches the ground, which
is exactly when the decision is made, so a fast landing from just above the
ground looks harmless to the client while the server is still holding the whole
descent.

Upstream's default `SpoofGround` mode is deliberately **not** included. It
reports `true` past a trigger distance, which is harmless at ordinary falling
speeds — each synthetic landing is under the safe fall distance — but with a
high `vertical` speed a single tick's descent already exceeds it, so every
spoofed packet becomes a real hit.

Upstream's other ~20 modes — `Creative`, `Jetpack`, `AirWalk`, `Explosion`,
`Fireball`, `Enderpearl` and the anti-cheat-specific bypasses (Vulcan, Grim,
Spartan, Sentinel, Verus, NCP, Hypixel) — are **not** included, nor is the
`Visuals`/`Stride` group. Ask if you want any of them ported.

## Vehicles

While flight is on and the player is riding something, upstream's
`VehicleControl` — its "BoatFly" — takes over in place of player flight: the
vehicle's velocity is driven each tick instead of the player's, which the server
accepts because the client is the authority on a vehicle it is riding. Steering
follows the vehicle's own heading unless `mouseControl` is set. The glide is
suppressed while the vehicle is in water, since sinking a boat drowns it and a
drowned boat cannot be steered.

Sneak lowers the vehicle rather than dismounting. Sneaking is what leaves a
vehicle, so left alone it would drop you out the moment you asked to descend —
the sneak *input* is therefore withheld while the vehicle is still airborne, and
returns once the vehicle is resting on the ground or afloat, which is when
dismounting is worth doing. Only the input is withheld; the descent reads the
sneak key directly, so it still responds. Set `sneakDescends` to `false` for
vanilla dismount behaviour.

## AntiHunger

Always on, independent of flight, and a port of upstream's module of the same
name. The server charges exhaustion in `ServerPlayer`'s travel statistics, and
only for movement made while standing on the ground or swimming — so
`keepFloating` reports the player as airborne whenever they are settled enough
for it to be plausible, and `noSprint` removes the rest.

Note that `noSprint` means exactly what it says: **you cannot sprint on foot**
with it enabled. That is upstream's default too. Flight's own sprint-speed is
unaffected, since it reads the sprint *key* rather than the sprint state. The
flag is left alone while riding, mid-dig, or in water, where it means something
other than walking.

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

### Note on mappings

Minecraft 26.2 ships unobfuscated — Mojang publishes no ProGuard mappings for
it, and Fabric's intermediary is the identity mapping (`0.0.0`). Loom still
requires a mappings dependency exposing a `named` namespace, so `build.gradle.kts`
generates a tiny v2 file that declares the three namespaces and maps nothing.
Names then pass through unchanged.

The same shift means the game jar carries no `LocalVariableTable`, so
name-based `@ModifyVariable` injections cannot resolve; the full-bright hook
anchors on an instruction instead.
