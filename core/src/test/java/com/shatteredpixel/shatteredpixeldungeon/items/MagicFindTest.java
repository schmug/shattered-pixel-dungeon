package com.shatteredpixel.shatteredpixeldungeon.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.watabou.utils.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Asset-free unit coverage for the Magic Find affix-chance multiplier.
 *
 * MF folds into the existing RingOfWealth Wealth buff (spec decision 1); the
 * Gdx-coupled buff lookup is out of scope here. This exercises the pure
 * Diablo-2-style diminishing-returns curve (spec decision 5) and the
 * bit-for-bit no-MF regression guarantee (acceptance criterion 3).
 */
public class MagicFindTest {

	@BeforeEach
	public void seed() {
		Random.pushGenerator(0xC0FFEEL);
	}

	@AfterEach
	public void unseed() {
		Random.popGenerator();
	}

	@Test
	public void zeroMagicFindIsBitForBitIdentity() {
		// must be the literal float 1.0f so threshold * multiplier == threshold
		// for ALL float thresholds (IEEE: x * 1.0f == x). Asserting on raw bits
		// catches a 1.0000001f that would silently perturb levelgen RNG.
		float m = MagicFind.multiplier(0);
		assertEquals(Float.floatToRawIntBits(1f), Float.floatToRawIntBits(m),
				"multiplier(0) must be the exact float 1.0f, was " + m);
		// negative / absent bonus also collapses to exact identity
		assertEquals(Float.floatToRawIntBits(1f),
				Float.floatToRawIntBits(MagicFind.multiplier(-3)));
	}

	@Test
	public void multiplierIsStrictlyIncreasingInBonus() {
		float prev = MagicFind.multiplier(0);
		for (int b = 1; b <= 50; b++) {
			float cur = MagicFind.multiplier(b);
			assertTrue(cur > prev,
					"multiplier must rise with MF bonus: m(" + b + ")=" + cur
							+ " !> m(" + (b - 1) + ")=" + prev);
			prev = cur;
		}
	}

	@Test
	public void returnsDiminish() {
		// Diablo-2 saturation: each extra MF point adds strictly less than the
		// previous one (concave curve, not an uncapped linear sum).
		float prevGain = MagicFind.multiplier(1) - MagicFind.multiplier(0);
		for (int b = 2; b <= 40; b++) {
			float gain = MagicFind.multiplier(b) - MagicFind.multiplier(b - 1);
			assertTrue(gain < prevGain,
					"marginal gain must shrink at b=" + b
							+ " (" + gain + " !< " + prevGain + ")");
			assertTrue(gain > 0f, "gain must stay positive at b=" + b);
			prevGain = gain;
		}
	}

	@Test
	public void multiplierIsBoundedByAsymptote() {
		float ceiling = 1f + MagicFind.MAX_BONUS;
		for (int b : new int[]{1, 10, 100, 1000, 100000}) {
			float m = MagicFind.multiplier(b);
			assertTrue(m < ceiling,
					"multiplier must never reach the asymptote: m(" + b + ")=" + m
							+ " >= " + ceiling);
		}
		// and it must actually approach it for very large MF
		assertTrue(MagicFind.multiplier(100000) > 1f + 0.99f * MagicFind.MAX_BONUS);
	}

	@Test
	public void noMagicFindProducesBaselineAffixRate() {
		// Acceptance criterion 3 / 4: with zero MF the affix roll outcome is
		// bit-for-bit the pre-change behaviour. Drive the EXACT threshold
		// arithmetic used in Weapon.random()/Armor.random() with and without
		// the multiplier and require an identical hit count off one RNG seed.
		final float window = 0.1f; // weapon non-curse affix window
		final int n = 200000;

		Random.pushGenerator(42L);
		int baseline = 0;
		for (int i = 0; i < n; i++) {
			if (Random.Float() >= 1f - window) baseline++;
		}
		Random.popGenerator();

		Random.pushGenerator(42L);
		int withIdentityMF = 0;
		float mf = MagicFind.multiplier(0);
		for (int i = 0; i < n; i++) {
			if (Random.Float() >= 1f - (window * mf)) withIdentityMF++;
		}
		Random.popGenerator();

		assertEquals(baseline, withIdentityMF,
				"zero-MF affix rate must be bit-for-bit identical to baseline");
	}

	@Test
	public void heroMagicFindRaisesMeanAffixCount() {
		// Acceptance criterion 4: a meaningful MF bonus measurably lifts the
		// mean affix count over a large simulated drop sample, while the no-MF
		// arm stays exactly at baseline.
		final float window = 0.1f;
		final int n = 300000;
		final int mfBonus = 8;

		long seed = 777L;

		Random.pushGenerator(seed);
		int withoutMF = 0;
		float idMF = MagicFind.multiplier(0);
		for (int i = 0; i < n; i++) {
			if (Random.Float() >= 1f - (window * idMF)) withoutMF++;
		}
		Random.popGenerator();

		Random.pushGenerator(seed);
		int withMF = 0;
		float mf = MagicFind.multiplier(mfBonus);
		for (int i = 0; i < n; i++) {
			if (Random.Float() >= 1f - (window * mf)) withMF++;
		}
		Random.popGenerator();

		double pNo = withoutMF / (double) n;
		double pYes = withMF / (double) n;
		// expected lift is large (window grows by the multiplier); a 5-sigma
		// guard rules out RNG noise without being flaky.
		double se = Math.sqrt(pNo * (1 - pNo) / n);
		assertTrue(pYes - pNo > 5 * se,
				"hero MF must measurably raise mean affix count: "
						+ pNo + " -> " + pYes);

		// no-MF arm must equal the untouched baseline exactly
		Random.pushGenerator(seed);
		int baseline = 0;
		for (int i = 0; i < n; i++) {
			if (Random.Float() >= 1f - window) baseline++;
		}
		Random.popGenerator();
		assertEquals(baseline, withoutMF);
	}

	@Test
	public void parseDebugBonusHandlesEveryInputForm() {
		// unset / blank -> disabled (0), so the debug flag is opt-in and the
		// no-MF bit-for-bit guarantee holds when it is absent.
		assertEquals(0, MagicFind.parseDebugBonus(null));
		assertEquals(0, MagicFind.parseDebugBonus(""));
		assertEquals(0, MagicFind.parseDebugBonus("   "));
		// "true" -> a strong default so it's useful without picking a number
		assertEquals(MagicFind.DEBUG_DEFAULT_BONUS, MagicFind.parseDebugBonus("true"));
		assertEquals(MagicFind.DEBUG_DEFAULT_BONUS, MagicFind.parseDebugBonus(" TRUE "));
		// explicit non-negative integer passes through
		assertEquals(25, MagicFind.parseDebugBonus("25"));
		assertEquals(0, MagicFind.parseDebugBonus("0"));
		// negatives clamp to 0; garbage is ignored (disabled), never throws
		assertEquals(0, MagicFind.parseDebugBonus("-7"));
		assertEquals(0, MagicFind.parseDebugBonus("garbage"));
	}

	@Test
	public void parseDebugBonusZeroIsStillBitForBitIdentity() {
		// a disabled/garbage debug flag must collapse to the exact 1.0f path
		float m = MagicFind.multiplier(MagicFind.parseDebugBonus(null));
		assertEquals(Float.floatToRawIntBits(1f), Float.floatToRawIntBits(m));
	}
}
