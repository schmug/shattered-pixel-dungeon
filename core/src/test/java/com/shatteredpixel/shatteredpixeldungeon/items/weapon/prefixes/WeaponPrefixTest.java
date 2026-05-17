package com.shatteredpixel.shatteredpixeldungeon.items.weapon.prefixes;

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

/**
 * Asset-free unit coverage for the weapon Prefix system.
 * Name rendering through Messages is Gdx-coupled and out of scope for the
 * minimal harness (see .claude/plans/prefix-affix-system.md, issue #10);
 * the ordering/spacing contract is covered via the pure join helper.
 */
public class WeaponPrefixTest {

	@BeforeEach
	public void seed() {
		Random.pushGenerator(12345L);
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
		// 50 / 40 / 10 split, allow generous tolerance
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
			Prefix p = Prefix.randomCommon(Sharp.class, Heavy.class, Balanced.class);
			// only Keen left in common; must never be an ignored class
			assertFalse(p instanceof Sharp);
			assertFalse(p instanceof Heavy);
			assertFalse(p instanceof Balanced);
		}
	}

	@Test
	public void randomCommonFallsBackWhenPoolExhausted() {
		// ignoring every common forces fallback to random() rather than crash
		Prefix p = Prefix.randomCommon(Sharp.class, Heavy.class, Balanced.class, Keen.class);
		assertNotNull(p);
	}

	@Test
	public void bundleRoundTripPreservesPrefixType() throws Exception {
		Bundle out = new Bundle();
		out.put("prefix", new Vicious());

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Bundle.write(out, bytes);
		Bundle in = Bundle.read(new ByteArrayInputStream(bytes.toByteArray()));

		Object restored = in.get("prefix");
		assertNotNull(restored);
		assertSame(Vicious.class, restored.getClass());
	}

	@Test
	public void staticDamageModifierIsApplied() {
		// Sharp is a flat +2 weapon-damage prefix; pure, deterministic
		assertEquals(12, new Sharp().damageFactor(10));
	}

	@Test
	public void curseFlagDistinguishesCursedPrefixes() {
		assertFalse(new Sharp().curse());
		assertTrue(new Dull().curse());
	}

	@Test
	public void joinNamePartsOrdersPrefixEnchantBaseSuffixSingleSpaced() {
		assertEquals("Vicious Blazing Sword of the Wolf",
				Prefix.joinNameParts("Vicious", "Blazing Sword of the Wolf"));
		// prefix absent
		assertEquals("Blazing Sword",
				Prefix.joinNameParts(null, "Blazing Sword"));
		// no stray/double spaces
		assertEquals("Sharp Dagger",
				Prefix.joinNameParts("Sharp", "Dagger"));
	}

	private static boolean contains(Class<?>[] arr, Object o) {
		for (Class<?> c : arr) if (c == o.getClass()) return true;
		return false;
	}
}
