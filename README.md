# HavocEco

A fully customizable economy plugin for Paper 1.21+ with a two-currency economy (money +
Rubies), a money-sink `/invest`, casino games, ruby transfers, scheduled double-ruby events,
combat/pearl rules, totem effect retention, a spawn jump-boost zone, and a cycling boss bar.

Everything lives in `config.yml` and a manually managed `database.yml`, matching the file style
of your existing configs.

---

## Building the jar

### Option A — automatically on GitHub (recommended)

This repo ships a GitHub Actions workflow (`.github/workflows/build.yml`) that builds the jar for
you. You don't need Java or Maven installed locally.

1. Create a new GitHub repository.
2. Push these files to it:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<you>/<repo>.git
   git push -u origin main
   ```
3. Go to the **Actions** tab on GitHub. The build runs automatically on every push.
4. When it finishes (green check), click the run, scroll to **Artifacts**, and download
   **HavocEco-jar**. Unzip it to get `HavocEco-1.0.0.jar`.

**Want a proper release download instead of an artifact?** Push a tag and the workflow also
attaches the jar to a GitHub Release:
```bash
git tag v1.0.0
git push origin v1.0.0
```
The jar then appears under the repo's **Releases** page.

You can also trigger a build by hand from the Actions tab (the workflow has `workflow_dispatch`).

### Option B — build locally

Needs JDK 21 and Maven:
```bash
mvn clean package
```
The jar lands in `target/HavocEco-1.0.0.jar`.

Either way, drop the jar into your server's `plugins/` folder and restart.

---

## Features

| Feature | Command / behavior |
|---|---|
| Money balance | `/balance` (alias `/bal`, `/money`) |
| Pay money | `/pay <player> <amount>` |
| Ruby transfer | `/rubypay <player> <amount>` |
| Money sink | `/invest <amount>`, `/invest claim` |
| Slots | `/slots <amount> [money|rubies]` |
| Jackpot | `/jackpot <amount> [money|rubies]` |
| Double-ruby events | scheduled via config + `/havoceco rubyevent on|off` |
| Pearls kept on death | automatic (config toggle) |
| Pearls/wind charges don't combat-tag | automatic (config toggle) |
| Keep Speed/Strength/Night Vision on totem pop | automatic (config toggle) |
| Spawn jump boost zone | automatic (config: center + radius) |
| Cycling boss bar (Havocsmp.club / /store / /discord) | automatic + per-player toggle hook |
| Admin | `/havoceco reload|givemoney|giverubies|rubyevent` |

### Still to build out

These have config + hooks but need your world/GUI specifics wired in: `/rubyshop` GUI, the bigger
Sell menu with multi-page categories, `/worth` GUI, baltop 1-50, `/live`, coordinate-hiding
toggle, wagers, and the custom amethyst tools (3x3x3 -> 12x12x12). The manager pattern used here
(a `Manager` class + a `Command` class + config in `Settings`) is the template to copy for each.

---

## database.yml

Created automatically on first run. Structure:

```yaml
players:
  <uuid>:
    name: Steve
    money: 1000.0
    rubies: 25
    moneySpent: 0.0
    moneyMade: 0.0
    invest:
      active: false
      principal: 0.0
      maturesAt: 0
```

It's flushed to disk every `STORAGE.SAVE-INTERVAL-SECONDS` (only when changed) and on shutdown.
The `Database` class is a thin abstraction — when you outgrow flat files, implement the same
method names against MySQL and swap it in `HavocEco#onEnable` without touching anything else.
