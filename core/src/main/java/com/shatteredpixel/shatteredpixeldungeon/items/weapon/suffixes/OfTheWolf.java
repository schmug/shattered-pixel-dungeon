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

public class OfTheWolf extends Suffix {

	private static ItemSprite.Glowing BROWN = new ItemSprite.Glowing( 0x885522 );

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		//small chance to leech a tiny bit of life on hit
		float chance = 0.10f * procChanceMultiplier(attacker);
		if (Random.Float() < chance && attacker.HP < attacker.HT) {
			attacker.HP = Math.min(attacker.HT, attacker.HP + 1 + weapon.buffedLvl());
		}
		return damage;
	}

	protected float procChanceMultiplier( Char attacker ) {
		return Weapon.Enchantment.genericProcChanceMultiplier(attacker);
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BROWN;
	}
}
