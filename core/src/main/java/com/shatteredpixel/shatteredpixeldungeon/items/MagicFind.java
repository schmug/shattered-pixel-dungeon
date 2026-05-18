/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

//Pure Magic Find maths. The v1 carrier is the existing RingOfWealth Wealth
//buff (spec decision 1); RingOfWealth supplies the integer bonus and this
//class turns it into the multiplier that scales each affix roll chance.
//
//The curve is a Diablo-2-style saturation (spec decision 5): effective MF
//approaches an asymptote rather than summing without bound, so a future
//gear-affix MF (issue #9) can be added into the same bonus and still pass
//through one shared diminishing curve instead of compounding linearly.
public final class MagicFind {

	private MagicFind() {}

	//asymptotic ceiling on the BONUS multiplier: with infinite MF the affix
	//roll window at most doubles (multiplier -> 1 + MAX_BONUS). Delegated
	//constant; kept conservative so MF tilts odds without trivialising drops.
	public static final float MAX_BONUS = 1.0f;

	//Diablo-2 saturation constant: the MF bonus that yields half of MAX_BONUS.
	//Larger = slower saturation (each early point worth more for longer).
	public static final float SATURATION = 4f;

	//Multiplier applied to each affix roll chance at item generation.
	//
	//bonus <= 0 returns the LITERAL float 1.0f. This is the bit-for-bit
	//no-MF guarantee (acceptance criterion 3): IEEE x * 1.0f == x for every
	//float threshold, so with no Wealth buff the affix RNG outcomes are
	//identical to pre-change behaviour. Doing arithmetic here (even
	//1f + 0f) would also yield 1.0f, but the explicit early return documents
	//and locks the invariant against future curve tweaks.
	public static float multiplier( int bonus ) {
		if (bonus <= 0) {
			return 1f;
		}
		float effective = MAX_BONUS * bonus / (bonus + SATURATION);
		return 1f + effective;
	}

	//Debug-only: the spd.debugMagicFind system property forces a minimum
	//Wealth-equivalent MF bonus so high Magic Find can be exercised without
	//farming Rings of Wealth. Mirrors the spd.debugStart flag (PR #4).
	//"true" -> a strong default; pure string parse kept here in the tested,
	//Gdx-free class. Unset/blank/garbage -> 0, so the flag is strictly
	//opt-in and the no-MF bit-for-bit guarantee is untouched in production.
	public static final int DEBUG_DEFAULT_BONUS = 10;

	public static int parseDebugBonus( String prop ) {
		if (prop == null || prop.trim().isEmpty()) {
			return 0;
		}
		prop = prop.trim();
		if (prop.equalsIgnoreCase("true")) {
			return DEBUG_DEFAULT_BONUS;
		}
		try {
			return Math.max(0, Integer.parseInt(prop));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
