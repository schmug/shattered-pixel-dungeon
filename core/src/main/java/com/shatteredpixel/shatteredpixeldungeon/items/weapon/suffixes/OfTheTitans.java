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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.suffixes;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class OfTheTitans extends Suffix {

	private static ItemSprite.Glowing CRIMSON = new ItemSprite.Glowing( 0xCC2222 );

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		//flat bonus damage scaling with weapon level
		float chance = 0.20f * Weapon.Enchantment.genericProcChanceMultiplier(attacker);
		if (Random.Float() < chance) {
			int bonus = 2 + weapon.buffedLvl();
			damage += bonus;
		}
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return CRIMSON;
	}
}
