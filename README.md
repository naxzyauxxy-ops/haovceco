# HavocEco

Companion plugin for HavocSMP. Runs alongside **CuyVoyager** and adds the features Voyager
doesn't cover — `/invest`, casino games, a ruby shop, coinflip wagers, amethyst area-mining
tools, `/live` for Media+, `/coords` toggle, a cycling boss bar, pearl/wind-charge/totem rules,
and a spawn jump-boost zone.

**Money is shared with Voyager.** HavocEco hooks the Vault economy Voyager provides, so
`/invest`, `/slots`, `/jackpot`, and coinflips all read and write the same balance the player
sees in Voyager's `/balance` — no split wallets, no reconciliation.

**Rubies are separate.** Voyager's Ruby/Shard API isn't public, so HavocEco ships rubies as its
own currency in `database.yml`. If you later want to bridge to Voyager rubies you'll need Pi3ro's
API — happy to add the bridge then.

**No overlapping commands.** HavocEco does NOT register `/balance`, `/pay`, `/baltop`, `/sell`,
or `/worth`. Voyager keeps those.

---

## Building the jar

### Option A — automatically on GitHub (recommended)

`.github/workflows/build.yml` builds the jar on every push. Push this repo to GitHub, go to the
**Actions** tab, and download the **HavocEco-jar** artifact when the run finishes.

Push a tag (`git tag v1.0.0 && git push origin v1.0.0`) to also attach the jar to a GitHub
Release.

### Option B — build locally

Needs JDK 21 and Maven:
```bash
mvn clean package
```
Jar lands in `target/HavocEco-1.0.0.jar`. Drop it in `plugins/` next to CuyVoyager and Vault.

---

## Features

| Feature | Command |
|---|---|
| Money sink | `/invest <amount>`, `/invest claim` |
| Slots | `/slots <amount> [money|rubies]` |
| Jackpot | `/jackpot <amount> [money|rubies]` |
| Ruby transfer | `/rubypay <player> <amount>` |
| Ruby shop | `/rubyshop` |
| Coinflip wager | `/cf <amount> [money|rubies]`, `/cf accept <player>`, `/cf cancel` |
| Amethyst area tools | via `/rubyshop` or `/havoceco giveamethyst <id> [player]` |
| Live announce (Media+) | `/live <message>` (perm `havoceco.live`, 3h cooldown) |
| Hide coordinates toggle | `/coords` |
| Admin | `/havoceco reload|giverubies|giveamethyst|rubyevent` |
| Cycling boss bar | automatic |
| Double-ruby events | scheduled via config + `/havoceco rubyevent on\|off` |
| Pearls kept on death | automatic |
| Pearls/wind charges don't combat-tag | automatic |
| Keep Speed/Strength/Night Vision on totem pop | automatic |
| Spawn jump boost zone | automatic |

---

## Config files

- `config.yml` — every toggle and tunable.
- `tools.yml` — defines each amethyst tool: cube size (3/6/9/12), type (pickaxe/shovel/axe/sell_axe), auto-sell or drop, ruby price.
- `tools-worth.yml` — the price paid per block by amethyst AUTO-SELL tools. Intentionally
  separate from Voyager's `worth.yml`; keep the rates slightly below Voyager's `/worth` so tools
  don't undercut your economy tuning.
- `database.yml` — HavocEco's own storage (rubies, invest state, coord-hide preference).
  Voyager's money data is untouched.

---

## Notes on specific features

- **Coordinate hiding** is client-side in Minecraft. `/coords` stores the preference in
  `database.yml` and, on Java clients, enables reduced-debug-screen which hides the F3 coordinate
  readout. For Bedrock (Geyser) the stored flag is exposed for your Geyser/floodgate setup to
  consume, since Bedrock's coordinate display is governed by the world "show coordinates" rule.
- **Amethyst tools** restrict what they break by type (pickaxe → stone/ore, shovel → dirt/sand,
  axe → wood) and skip a blacklist (bedrock, containers, spawners, obsidian). Even cube sizes
  (6, 12) span N blocks by extending +1 on each axis from the mined block. Auto-sell tools use
  `tools-worth.yml`; drop-mode tools drop items naturally.
- **Missing Vault / Voyager?** The plugin still loads and rubies/coinflip-with-rubies still work.
  Money-based commands (`/invest`, money `/slots`, money `/jackpot`) refuse cleanly with a
  message pointing at the missing dependency.
