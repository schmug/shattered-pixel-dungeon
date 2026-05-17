# Spec: Prefix Affix System for Weapons & Armor

Status: APPROVED (open questions resolved 2026-05-17). Ready to implement.
Mirrors the Suffix system shipped in commit a33c20182.

## 1. Problem statement

Commit a33c20182 shipped suffixes "modeled on Diablo 1's prefix+suffix pattern
(e.g. *Blazing Sword of the Wolf*)" but only delivered the suffix half. Items
can roll a trailing affix ("of the Wolf") but never a leading stat affix
("Strong", "Vicious", "Godly"), so the advertised Diablo-style naming and the
build-variety payoff are only half-realized.

## 2. Scope

**In:**
- New abstract `Prefix` class for weapons (`items/weapon/prefixes/Prefix.java`)
  and armor (`items/armor/prefixes/Prefix.java`), structurally mirroring the
  existing `Suffix` classes (tiered `common`/`uncommon`/`rare` pools at
  50/40/10, a `curses` pool, `random*()` factory methods with `toIgnore`,
  `proc`/`activate`/`deactivate`, `glowing()`, Bundlable).
- Starter set of concrete prefixes per slot: weapon (damage/accuracy/crit
  flavored), armor (defense/resist flavored). ~10-12 non-curse + 3 curses per
  slot to match Suffix's footprint. **Concrete names/effects delegated to
  implementation.**
- Integration into `Weapon.java` / `Armor.java`: new `Prefix prefix` field,
  bundle store/restore, independent roll in `random()`, name composition, glow
  fallthrough, equip activate/deactivate, attack/defense proc hook.
- i18n keys in `items.properties` (`items.weapon.prefixes.*` /
  `items.armor.prefixes.*`) following the existing `name`/`prefix`/`desc`
  key pattern (mirror `items.armor.suffixes.*`).

**Out:**
- No global rarity-tier system (Magic/Rare labels) — separate next spec.
- No Magic Find stat.
- No changes to existing Suffix or enchantment/glyph systems.
- No rebalancing of existing drop rates beyond adding the new independent
  prefix roll.
- No new item art/sprites (reuse glow-color mechanism only).

## 3. Constraints

- **Mirror, don't refactor.** Suffix classes are duplicated per slot
  (weapon vs armor) by deliberate convention; Prefix follows the same
  per-slot duplication, no shared base. Keeps the diff consistent with a33c.
- **Save compatibility.** `restoreFromBundle` must tolerate old saves with
  no `prefix` key (Bundle returns null → no prefix; same pattern suffix
  relies on at `Weapon.java:254` / `Armor.java:165`).
- **Independent roll, separate RNG.** Prefix roll happens inside the existing
  `Random.pushGenerator(Random.Long())` block in `random()`
  (Weapon.java:457, Armor.java ~690) so ParchmentScrap variance doesn't
  perturb levelgen, exactly as the suffix roll does.
- **Curse visibility rules.** Name and glow gate cursed prefixes behind
  `cursedKnown`, matching `decorateWithSuffix` (Weapon.java:429) and
  `glowing()` (Weapon.java:552).
- **GPL header** block on every new file, copyright line
  `2014-2026 Evan Debenham`, matching every existing source file.
- Conventional-commit prefix `feat:` on the commit; PR not direct-push
  (global CLAUDE.md).

## 4. Resolved design decisions (open questions answered)

1. **Name ordering:** Prefix PREPENDS outside the enchant adjective and
   stacks with it. Result: `Vicious Blazing Sword of the Wolf`
   (prefix + enchant-adjective + base + suffix). Prefix-only and suffix-only
   cases must render with no stray spacing.
2. **Effect surface:** Prefixes lean toward STATIC STAT MODIFIERS (flat /
   percentage damage, accuracy, armor, resist) rather than proc effects.
   This likely requires new hook points beyond what suffix uses — e.g. a
   damage-factor hook in `Weapon.damageRoll` and an armor hook in
   `Armor.DRMax`/`DRMin`. Identify and add these cleanly during
   implementation; keep `proc()` available but secondary.
3. **Tests:** ROBUST TESTS REQUIRED, scoped to a MINIMAL harness.
   Repo has zero test infra; user chose a minimal scoped JUnit 5 harness
   for prefixes (issue #10 tracks repo-wide test coverage). Coverage:
   bundle round-trip (prefix survives save/load), name-composition across
   prefix×enchant×suffix combinations including empty cases (against a
   fake/stub Messages), roll-tier distribution sanity, cursed-visibility
   gating, stat-modifier math. Tests must pass before commit; report
   pass/fail counts explicitly.
4. **Concrete affix list:** DELEGATED to implementation. Use the tier counts
   above; favor recognizably Diablo-flavored stat names.

## 5. Acceptance criteria

1. A weapon or armor independently rolls a prefix at the same probability
   tier as suffix (weapon 10% non-curse / 5% curse; armor 12% / 5%), via a
   roll separate from the enchantment and suffix rolls.
2. An item with both prefix and suffix renders as
   `<Prefix> <base/enchant> of <Suffix>`; prefix-only and suffix-only cases
   render correctly with no stray spacing.
3. Save → quit → reload of an item carrying a prefix preserves the prefix
   type and its stat effect; a pre-existing save with no prefix loads with
   `prefix == null` and no error.
4. Equipping an item with a stat-modifier prefix applies its modifier to the
   relevant stat; unequipping removes it; a cursed prefix's name/glow stays
   hidden until `cursedKnown`.
5. Project compiles; a minimal scoped JUnit 5 harness exists under
   `core/src/test/java` and the prefix test suite (bundle round-trip,
   name composition, tier distribution, curse gating, stat math) passes;
   pass/fail counts reported explicitly before any commit. (Supersedes the
   earlier "same coverage as suffix = zero" wording; repo-wide coverage is
   issue #10.)

## Key file pointers

- `core/.../items/weapon/suffixes/Suffix.java` — template (155 lines)
- `core/.../items/armor/suffixes/Suffix.java` — template (141 lines)
- `core/.../items/weapon/suffixes/OfTheWolf.java` — concrete example
- `Weapon.java`: field 127, proc 178, bundle 240/253, name 415-433,
  glow 546-553, random 436-475, appendSuffix 479
- `Armor.java`: field 118, bundle 151/164, equip-activate 309, proc 528/541,
  name 590-602, random 678-711, appendSuffix 719, glow 822-829
- `core/src/main/assets/messages/items/items.properties:104+` — suffix i18n
  keys to mirror
