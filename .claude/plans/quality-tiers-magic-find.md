# Spec: Item Quality Tiers + Magic Find

Status: APPROVED (open questions resolved 2026-05-17). Ready to implement.
Depends on the prefix affix system (.claude/plans/prefix-affix-system.md).
Follow-up issue for MF-as-gear-affix: #9.

## 1. Problem statement

Affixes (enchant/glyph, prefix, suffix) roll as independent ~10% events with
no unifying signal, so a player can't tell a plain drop from a multi-affix
jackpot at a glance, and nothing lets a player invest in finding better gear —
the loot has no chase. Players need a readable quality signal on items and a
stat that meaningfully tilts the odds toward good drops.

## 2. Scope

**In:**
- A `quality` classification on weapons/armor DERIVED from affix count:
  Common (0) -> Magic (1) -> Rare (2) -> Exalted (3 = prefix+suffix+
  enchant/glyph). 4 bands, confirmed. User-facing names/colors DELEGATED to
  implementation. Code must NOT reuse the word "tier" (collides with T1-T5
  power tiers) — use `quality`/`Quality`.
- Quality reflected in displayed item name color + a localized quality word
  in item info text.
- Magic Find stat that multiplies each affix roll chance (prefix, suffix,
  enchant/glyph) at generation. v1 carrier = extend existing `RingOfWealth`
  `Wealth` buff (option a). MF-as-gear-affix (option b) deferred to issue #9.
- Hero MF applied at item-generation for hero-caused drops, mirroring
  `RingOfWealth.dropChanceMultiplier` reading `Char` buffs.
- Tests: classification correctness, MF probability scaling, name-color
  mapping, save round-trip, no-MF regression.

**Out:**
- No unique/legendary/set items (issues #6-#8).
- No change to T1-T5 power-tier system or stat scaling.
- No base drop-rate change when no MF present.
- No quality on consumables/wands/rings/artifacts — weapons & armor only.
- No gambling vendor.

## 3. Resolved design decisions

1. **MF carrier:** Option (a) — fold MF into existing `RingOfWealth`
   (`Wealth` buff, `getBuffedBonus` pattern). Option (b) gear-affix MF filed
   as issue #9, depends on prefix system.
2. **Derive, not budget:** Keep the existing independent affix rolls in
   `Weapon.random()` / `Armor.random()`. Quality is COMPUTED from resulting
   affixes; MF is a MULTIPLIER on existing roll chances. Do NOT replace the
   three rolls with a single budgeted roll.
3. **4 bands** (Common/Magic/Rare/Exalted), names + colors DELEGATED to
   implementation; favor Diablo-recognizable naming and distinct colors.
4. **Curses & quality:** A cursed affix COUNTS toward the affix total, but a
   cursed item uses a distinct curse color/label (reuse curse-purple /
   `cursedKnown` gating) rather than the positive-quality color.
5. **MF diminishing returns:** MF scaling must diminish per industry standard
   — use a Diablo 2-style saturation curve (effective MF approaches an
   asymptote, e.g. effectiveMF = MF*factor/(MF+factor) form). Exact constant
   delegated to implementation; gear+ring MF (when #9 lands) must combine
   through this same shared curve, not an uncapped sum.

## 4. Constraints

- Builds on prefix spec; affix-count logic reads prefix+suffix+enchant/glyph.
- Derive-don't-re-architect: existing independent rolls
  (Weapon.java:436-475) stay; quality computed from results.
- "tier" name is taken by Generator T1-T5 — use `quality`.
- Item names are currently uncolored plain text; `ItemSlot.java:43-49` only
  hardlights level/status sub-text, not the title (`ItemSlot.java:355`).
  A new, carefully scoped name-color hook is required; must not break
  existing degraded/upgraded/curse-infused sub-text coloring.
- Save compat: prefer recomputing quality from affixes on load (no new
  bundle field); if a field is used it must be null-tolerant like suffix
  (Weapon.java:254).
- MF stacking follows `RingOfWealth.java:107` `1.20^bonus` precedent through
  the diminishing-returns curve; no unbounded compounding.
- Conventional commit `feat:`; PR not direct-push; tests reported with
  explicit pass/fail counts.

## 5. Acceptance criteria

1. 0 affixes -> Common, 1 -> Magic, 2 -> Rare, 3 -> Exalted; recomputed
   correctly after load with no required stored field (or null-tolerant
   field).
2. Item name renders in a quality-specific color in inventory and on the
   ground, quality word appears localized in item info; existing
   level/curse sub-text coloring unchanged.
3. With MF bonus N, each affix roll chance is multiplied by the defined
   diminishing MF function; with zero MF, affix roll probabilities are
   bit-for-bit identical to pre-change behavior.
4. Hero-equipped MF measurably raises mean affix count across a large
   simulated drop sample (statistical test); no-MF mean statistically
   unchanged vs. baseline.
5. Project compiles; suite (classification, MF scaling + diminishing,
   color mapping, save round-trip, no-MF regression) passes with counts
   reported before commit.

## Key file pointers

- `RingOfWealth.java:107` — `dropChanceMultiplier`, `getBuffedBonus`,
  `Wealth` buff (line 311) — MF v1 carrier
- `Generator.java:675-779` — `random()`, `randomWeapon`, `randomArmor`
- `Weapon.java:436-475` / `Armor.java:678-711` — independent affix rolls
- `Weapon.java:415-433` / `Armor.java:590-602` — name composition
- `ItemSlot.java:43-49` — hardlight color constants;
  `ItemSlot.java:355` — title text (currently uncolored)
- `Item.java:433` `visiblyUpgraded`, `:449` `isIdentified`,
  `:442` cursed/cursedKnown
- Prefix dependency: `.claude/plans/prefix-affix-system.md`
