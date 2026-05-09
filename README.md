# 🛠️ AdminTools

> Lightweight admin utilities for **Paper 1.21.1** SMP servers.  
> InvSee · EnderChest · Vanish · FakePlayer — all in one plugin, zero dependencies.

---

## ✨ Features

### 📦 `/invsee <player>`
Opens a player's full inventory in a clean GUI — armor, offhand, hotbar, and all 36 main slots laid out exactly as the player sees them. Structural slots are blocked with barriers so nothing ends up in the wrong place by accident.

- ✅ Works for **online and offline** players
- ✅ Changes are saved back instantly on close
- ✅ Can be set to **read-only** in the config

### 🟣 `/ecsee <player>`
Opens a player's enderchest in a 27-slot GUI.

- ✅ Works for **online and offline** players  
- ✅ Enderchest contents are cached to disk on every logout  
- ✅ Live-synced when the target is online, persisted to disk when offline  
- ✅ Can be set to **read-only** in the config

### 👻 `/vanish <on|off>`
Disappear from the view of regular players without a trace.

- Completely hidden from players without the vanish permission
- `/msg` to a vanished player returns *"not online"* for non-admins
- Admins with the permission always see each other
- Vanish state is restored automatically on reconnect

### 🎭 `/fakeplayer <join|leave|list> [name]`
Simulate a player joining or leaving the server.

- Join/leave messages use the **vanilla translation key** — every client sees them in their own language with the correct yellow colour, identical to real messages
- Fake players appear in the **tab list** via NMS packets, indistinguishable from real players — no prefix, no colour difference
- `/msg <fakeplayer> <text>` is silently intercepted and forwarded to all online ops
- Multiple fake players can be active simultaneously

### ⚙️ `/admintools help` · `/admintools reload`
In-game help page listing every command with full descriptions, current config state, and clickable command suggestions. Config can be reloaded live without restarting the server.

---

## 🔑 Permissions & Config

All permissions default to **op only** and are fully configurable in `config.yml` — no need to touch `plugin.yml` or a permissions plugin for basic setups.

```yaml
# config.yml
permissions:
  invsee:      op        # op | all | your.custom.node
  ecsee:       op
  vanish:      op
  fakeplayer:  op
  admintools:  op

fakeplayer:
  show-in-tab: true     # false = join/leave message only, no tab entry

invsee:
  editable: true        # false = read-only GUI

ecsee:
  editable: true        # false = read-only GUI
```

Reload at any time with `/admintools reload`.

---

## 📋 Command Reference

| Command | Description |
|---|---|
| `/invsee <player>` | Open & edit inventory (online or offline) |
| `/ecsee <player>` | Open & edit enderchest (online or offline) |
| `/vanish <on\|off>` | Toggle vanish mode |
| `/fakeplayer join <name>` | Simulate a player joining |
| `/fakeplayer leave <name>` | Simulate a player leaving |
| `/fakeplayer list` | List all active fake players |
| `/admintools help` | Show in-game help (alias: `/at help`) |
| `/admintools reload` | Reload config.yml |

---

## ⚙️ Installation

1. Download the latest `.jar` from [Releases](../../releases)
2. Drop it into your server's `plugins/` folder
3. Restart the server
4. Done — no additional configuration required

Offline data is stored in `plugins/AdminTools/inventories/` and `plugins/AdminTools/enderchests/`.

---

## 🔧 Building from Source

**Requirements:** Java 21, Maven 3.8+

```bash
git clone https://github.com/your-user/AdminTools.git
cd AdminTools
mvn package
```

The compiled `.jar` will be at `target/AdminTools-1.1.0.jar`.

---

## 📝 Technical Notes

- **Offline editing** requires the target player to have logged in at least once after the plugin was installed, so their data can be cached.
- **FakePlayer tab entries** are injected via NMS (`ClientboundPlayerInfoUpdatePacket`) and therefore tied to Paper 1.21.1 internals. A recompile may be needed for future major versions.
- The plugin uses the **main scoreboard** — no conflicts with other scoreboard-based plugins as long as they don't overwrite the same team names.

---

## 📄 License

MIT — do whatever you want, attribution appreciated.
