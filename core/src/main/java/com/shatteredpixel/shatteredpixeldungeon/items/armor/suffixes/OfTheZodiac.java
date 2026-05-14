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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.suffixes;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class OfTheZodiac extends Suffix {

	private static ItemSprite.Glowing RAINBOW = new ItemSprite.Glowing( 0xFF66FF );

	@Override
	public int proc( Armor armor, Char attacker, Char defender, int damage ) {
		//small chance-modulated reduction that varies with combat seed (looks "random/cosmic")
		int reduce = Math.max(1, damage / 8);
		return Math.max(0, damage - reduce);
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return RAINBOW;
	}
}
