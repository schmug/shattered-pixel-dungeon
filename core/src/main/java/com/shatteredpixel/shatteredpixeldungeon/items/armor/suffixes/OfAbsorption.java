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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class OfAbsorption extends Suffix {

	private static ItemSprite.Glowing TURQUOISE = new ItemSprite.Glowing( 0x33CCCC );

	@Override
	public int proc( Armor armor, Char attacker, Char defender, int damage ) {
		//convert a small portion of damage into a temporary barrier
		if (damage > 2 && Random.Float() < 0.20f) {
			int shield = Math.max(1, damage / 5);
			Buff.affect(defender, Barrier.class).incShield(shield);
		}
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return TURQUOISE;
	}
}
