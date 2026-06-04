# ScalarTech Solutions

A faction content mod for the game **Starsector** — "a small faction at the edge of the core
worlds with an iron will to defend their people." Mod id `scalartech`, author Nia Tahl. Targets
Starsector `0.98a`. Shares lineage (and the `tahlan_` asset prefix) with the sibling mod
**Tahlan Shipworks**.

Unlike Shipworks (a sprawling "ship pack that suffers from feature creep"), ScalarTech is a focused
faction mod built in a narrow timeframe and **considered complete in its current state**. Default to
maintenance-minded changes that fit the existing design; expanding scope isn't off the table, but it's
a deliberate decision rather than the normal mode of work.

## Environment

- **Game install:** `G:\Starsector`
- **API sources:** `G:\Starsector\starsector-core\starfarer.api.zip` — unzip/browse this for the
  vanilla API surface (`com.fs.starfarer.api.*`) and the bundled `impl` classes that most mod code
  extends or copies from.
- **Local docs:** `.claude/rules/` holds worked references for the trickier game systems
  (see [below](#detailed-subsystem-guides)) — read the relevant one before implementing in that area.

## Languages & build

- **Java only** (~58 Java files, no Kotlin). Match the conventions of the file you're in; do not
  introduce Kotlin.
- Source lives under `jars/src/`. The main package is `org.niatahl.scalartech/`; one vanilla-shadowing
  rule command lives under `com/fs/starfarer/api/impl/campaign/rulecmd/`.
- There is **no Gradle/Ant build** — the project is an IntelliJ module (`tahlan-scalartech.iml`) that
  compiles to the committed artifact `jars/tahlan-scalartech.jar`. Rebuild the jar via IntelliJ's
  build/artifacts, not a CLI tool.
- The game loads the jar named in `mod_info.json` (`jars/tahlan-scalartech.jar`), so the jar **must be
  rebuilt and committed** for code changes to take effect in-game.

### Dependencies

- **Hard deps** (declared in `mod_info.json`, required to launch): LazyLib, MagicLib. These are
  re-checked in `ScalarModPlugin.onApplicationLoad`, which throws if they're missing.
- **Incompatibility:** the plugin hard-throws if `@_ss_rebal_@` (Starsector Rebal) is enabled.
- **Soft integrations** (project libraries in the `.iml`; guard usage with `isModEnabled` checks):
  Nexerelin/Exerelin, Industrial Evolution (IndEvo), Exiled Space, LunaLib, GraphicsLib (`shaderLib`).
  GraphicsLib is initialized only when present (`ShaderLib.init()` + the `data/lights/*` CSVs); code
  that touches any soft dep must stay behind an enablement check — never assume it's present.

## Layout

### `jars/src/org/niatahl/scalartech/` — compiled code

- `ScalarModPlugin.java` — the `modPlugin` from `mod_info.json`. Handles onApplicationLoad (dep checks,
  GraphicsLib init, shield-hullmod collection), onNewGame / onGameLoad (generates the `Spindle` system,
  initializes faction relations, registers `scalartech` with the bounty system), and `pickMissileAI`.
- `world/` — `ScalarRelationPlugin.java` (faction relationships) and `Spindle.java` (the mod's home
  star system generation).
- `campaign/` — campaign-side systems: `econ/` (`GateScar`, `industries/ScalarTechHQ`),
  `nexerelin/` (`ScalartechNamer`), `submarkets/` (`STDFMarketPlugin`).
- `hullmods/` — hullmod effects.
- `shipsystems/` — ship-system `*Stats` scripts and their `ai/`.
- `weapons/` — weapon/projectile scripts and on-hit effects.
- `ai/` — missile AI (`BaseMissileAI`, `EMPtorpedoAI`).
- `utils/` — helpers (`GraphicLibEffects`, `Scalar_txt`).

### `com/fs/starfarer/api/impl/campaign/rulecmd/`

- `tahlan_SilvysTurnInScript.java` — a custom rule command, placed in the vanilla `rulecmd` package so
  the rules engine resolves it by class name.

### `data/` — game-data definitions (CSV/JSON/`.ship`/`.variant`/`.system`)

- `hulls/` (`ship_data.csv`, `wing_data.csv`, `.ship`), `weapons/` (`proj/`), `shipsystems/`
  (`ship_systems.csv`, `.system`), `hullmods/`, `variants/`, `strings/`, `lights/` (GraphicsLib CSVs).
- `campaign/` — `rules.csv`, `industries.csv`, `market_conditions.csv`, `submarkets.csv`,
  `special_items.csv`, `sim_opponents.csv`, `terrain/`.
- `config/` — `settings.json` (Starsector mod settings), `hull_styles.json`, `engine_styles.json`,
  `planets.json`, per-integration config (`exerelin/`, `exerelinFactionConfig/`, `indEvo/`, `prism/`,
  `vayraBounties/`, `CommissionBonus/`, `modFiles/`).
- `world/factions/` — faction definitions. `missions/` — `mission_list.csv` and the `tahlan_scalartest`
  combat mission.

### Other

- `graphics/`, `sounds/` — assets.
- `scalartech.version` — version-checker manifest. `Changelog.txt` — player-facing changelog.

## Conventions

- Namespace ids, tags, and memory keys to avoid collisions with vanilla and other mods. The **`tahlan_`**
  prefix on many ids and asset files is essentially an author tag — its real job is to give asset files a
  pseudo-namespace. It carries no functional meaning beyond that; follow the prefix already used by the
  surrounding files rather than reasoning about it. Id constants are declared inline where they're used
  (there is no central IDs class).
- Prefer library helpers over hand-rolled boilerplate: MagicLib for fleets/bounties/combat, LazyLib for
  combat utility lookups.
- No attributions in commits

## Detailed subsystem guides

`.claude/rules/` contains worked references for the trickier systems (carried over from the sibling
Tahlan Shipworks project — examples may use the `org.niatahl.tahlan` package, but the game systems are
identical). Read the relevant one before implementing in that area:

- `fleets.md` — spawning fleets, assignments, memory flags, library shortcuts.
- `fleet_behavior.md` — fleet managers + assignment-AI state-machine patterns.
- `fleet_interaction_dialog.md` — `FleetInteractionDialogPluginImpl` overrides (boss encounters, etc.).
- `scripted_dialog.md` — Java-authored `InteractionDialogPlugin` setpieces.
- `rules.md` — the `rules.csv` system and custom rule commands.
- `balance.md` — ship stat-budget method (DP = `supplies/mo`, not `fleet pts`), the standard hullmod
  suite's effect on stat value, and the weapon-mount/damage-type economy. Read before evaluating or
  changing ship stats.
