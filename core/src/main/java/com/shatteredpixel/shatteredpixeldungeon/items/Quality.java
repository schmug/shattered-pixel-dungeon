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

//Diablo-style item quality band, DERIVED from how many affixes
//(enchant/glyph + prefix + suffix) an item carries. There is deliberately no
//stored bundle field: quality is recomputed from the affixes on every load,
//so old saves need no migration (.claude/plans/quality-tiers-magic-find.md).
//
//Named "quality" rather than "tier" to avoid colliding with the Generator
//T1-T5 power tiers. Curse colouring is NOT a band here; a cursed item reuses
//the existing curse-purple via cursedKnown gating at the render site.
public enum Quality {

	//0 affixes - plain drop, neutral white (keeps the legacy uncoloured look)
	COMMON ( 0xFFFFFF ),
	//1 affix - Diablo "magic" blue
	MAGIC  ( 0x88AAFF ),
	//2 affixes - Diablo "rare" gold/yellow
	RARE   ( 0xFFD700 ),
	//3 affixes (prefix + suffix + enchant/glyph) - the jackpot, vivid orange
	EXALTED( 0xFF6600 );

	private final int color;

	Quality( int color ) {
		this.color = color;
	}

	//24-bit RGB title colour for this band
	public int color() {
		return color;
	}

	//lowercase band name, used as the i18n key suffix for the info-text word
	public String messageKey() {
		return name().toLowerCase();
	}

	//maps a raw affix count onto one of the four bands, clamped at both ends
	//so a missing/negative count never underflows and a hypothetical fourth
	//affix never throws.
	public static Quality fromAffixCount( int count ) {
		if (count <= 0) return COMMON;
		if (count == 1) return MAGIC;
		if (count == 2) return RARE;
		return EXALTED;
	}

	//convenience for the weapon/armor call sites: counts the present affixes
	//(enchant or glyph, prefix, suffix) and classifies. A cursed affix still
	//counts toward the total per spec decision 4.
	public static Quality of( boolean enchantOrGlyph, boolean prefix, boolean suffix ) {
		int count = (enchantOrGlyph ? 1 : 0) + (prefix ? 1 : 0) + (suffix ? 1 : 0);
		return fromAffixCount( count );
	}
}
