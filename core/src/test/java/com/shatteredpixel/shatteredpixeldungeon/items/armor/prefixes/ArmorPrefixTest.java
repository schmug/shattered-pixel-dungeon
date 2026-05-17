package com.shatteredpixel.shatteredpixeldungeon.items.armor.prefixes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Asset-free unit coverage for the armor Prefix system (mirrors weapon). */
public class ArmorPrefixTest {

	@BeforeEach
	public void seed() {
		Random.pushGenerator(67890L);
	}

	@AfterEach
	public void unseed() {
		Random.popGenerator();
	}

	@Test
	public void randomFollowsTierChances() {
		Map<String, Integer> tier = new HashMap<>();
		int n = 20000;
		for (int i = 0; i < n; i++) {
			Prefix p = Prefix.random();
			assertNotNull(p);
			String t;
			if (contains(Prefix.common, p)) t = "common";
			else if (contains(Prefix.uncommon, p)) t = "uncommon";
			else if (contains(Prefix.rare, p)) t = "rare";
			else t = "other";
			tier.merge(t, 1, Integer::sum);
		}
		assertEquals(0, tier.getOrDefault("other", 0));
		assertTrue(tier.get("common") > n * 0.42 && tier.get("common") < n * 0.58,
				"common ~50%, got " + tier.get("common"));
		assertTrue(tier.get("uncommon") > n * 0.32 && tier.get("uncommon") < n * 0.48,
				"uncommon ~40%, got " + tier.get("uncommon"));
		assertTrue(tier.get("rare") > n * 0.04 && tier.get("rare") < n * 0.18,
				"rare ~10%, got " + tier.get("rare"));
	}

	@Test
	public void randomCommonRespectsToIgnore() {
		for (int i = 0; i < 500; i++) {
			Prefix p = Prefix.randomCommon(Sturdy.class, Reinforced.class, Padded.class);
			assertFalse(p instanceof Sturdy);
			assertFalse(p instanceof Reinforced);
			assertFalse(p instanceof Padded);
		}
	}

	@Test
	public void randomCommonFallsBackWhenPoolExhausted() {
		Prefix p = Prefix.randomCommon(Sturdy.class, Reinforced.class, Padded.class, Lined.class);
		assertNotNull(p);
	}

	@Test
	public void bundleRoundTripPreservesPrefixType() throws Exception {
		Bundle out = new Bundle();
		out.put("prefix", new Fortified());

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Bundle.write(out, bytes);
		Bundle in = Bundle.read(new ByteArrayInputStream(bytes.toByteArray()));

		Object restored = in.get("prefix");
		assertNotNull(restored);
		assertSame(Fortified.class, restored.getClass());
	}

	@Test
	public void staticDefenseModifierIsApplied() {
		// Sturdy is a flat +2 damage-reduction prefix
		assertEquals(12, new Sturdy().defenseFactor(10));
	}

	@Test
	public void curseFlagDistinguishesCursedPrefixes() {
		assertFalse(new Sturdy().curse());
		assertTrue(new Brittle().curse());
	}

	@Test
	public void joinNamePartsOrdersPrefixGlyphBaseSuffixSingleSpaced() {
		assertEquals("Sturdy Stone Plate of Protection",
				Prefix.joinNameParts("Sturdy", "Stone Plate of Protection"));
		assertEquals("Stone Plate",
				Prefix.joinNameParts(null, "Stone Plate"));
		assertEquals("Sturdy Mail",
				Prefix.joinNameParts("Sturdy", "Mail"));
	}

	private static boolean contains(Class<?>[] arr, Object o) {
		for (Class<?> c : arr) if (c == o.getClass()) return true;
		return false;
	}
}
