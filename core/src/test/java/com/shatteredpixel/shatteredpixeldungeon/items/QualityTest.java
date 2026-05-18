package com.shatteredpixel.shatteredpixeldungeon.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Asset-free unit coverage for the derived item Quality classification and its
 * colour mapping. Quality is COMPUTED from the affix count (enchant/glyph +
 * prefix + suffix); there is no stored bundle field, so "save round-trip"
 * means: recomputing from the same affix count yields the same band. The
 * Gdx-coupled title render path (WndInfoItem / IconTitle) is out of scope for
 * this harness, consistent with how prefix name-composition was handled
 * (.claude/plans/quality-tiers-magic-find.md, issue #10).
 */
public class QualityTest {

	@Test
	public void affixCountMapsToFourBands() {
		assertSame(Quality.COMMON,  Quality.fromAffixCount(0));
		assertSame(Quality.MAGIC,   Quality.fromAffixCount(1));
		assertSame(Quality.RARE,    Quality.fromAffixCount(2));
		assertSame(Quality.EXALTED, Quality.fromAffixCount(3));
	}

	@Test
	public void affixCountIsClampedAtBothEnds() {
		// negative / absent affixes never underflow below Common
		assertSame(Quality.COMMON,  Quality.fromAffixCount(-1));
		// a fourth affix (future-proofing) saturates at Exalted, never throws
		assertSame(Quality.EXALTED, Quality.fromAffixCount(4));
		assertSame(Quality.EXALTED, Quality.fromAffixCount(99));
	}

	@Test
	public void ofCountsPresentAffixesIndependentOfOrder() {
		assertSame(Quality.COMMON,  Quality.of(false, false, false));
		assertSame(Quality.MAGIC,   Quality.of(true,  false, false));
		assertSame(Quality.MAGIC,   Quality.of(false, true,  false));
		assertSame(Quality.MAGIC,   Quality.of(false, false, true));
		assertSame(Quality.RARE,    Quality.of(true,  true,  false));
		assertSame(Quality.RARE,    Quality.of(false, true,  true));
		assertSame(Quality.EXALTED, Quality.of(true,  true,  true));
	}

	@Test
	public void recomputingFromSameAffixCountIsStable() {
		// derive-don't-store: a "reload" is just a recompute; it must be
		// deterministic and identical to the pre-save classification.
		for (int affixes = 0; affixes <= 3; affixes++) {
			Quality first  = Quality.fromAffixCount(affixes);
			Quality reload = Quality.fromAffixCount(affixes);
			assertSame(first, reload);
		}
	}

	@Test
	public void everyBandHasADistinctColour() {
		Set<Integer> seen = new HashSet<>();
		for (Quality q : Quality.values()) {
			assertEquals(true, seen.add(q.color()),
					q + " colour collides with another band");
		}
		// Magic/Rare/Exalted must not silently equal the neutral Common colour
		assertNotEquals(Quality.COMMON.color(), Quality.MAGIC.color());
		assertNotEquals(Quality.COMMON.color(), Quality.RARE.color());
		assertNotEquals(Quality.COMMON.color(), Quality.EXALTED.color());
	}

	@Test
	public void colourValuesAreOpaqueRgbInRange() {
		for (Quality q : Quality.values()) {
			int c = q.color();
			assertEquals(0, c & ~0xFFFFFF, q + " colour must be a 24-bit RGB value");
		}
	}

	@Test
	public void messageKeyIsLowercaseBandName() {
		assertEquals("common",  Quality.COMMON.messageKey());
		assertEquals("magic",   Quality.MAGIC.messageKey());
		assertEquals("rare",    Quality.RARE.messageKey());
		assertEquals("exalted", Quality.EXALTED.messageKey());
	}
}
