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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class OfRuin extends Suffix {

	private static ItemSprite.Glowing BLACKRED = new ItemSprite.Glowing( 0x660000 );

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		//heavy strike chance with self-bleed cost
		float chance = 0.15f * Weapon.Enchantment.genericProcChanceMultiplier(attacker);
		if (Random.Float() < chance) {
			damage = Math.round(damage * 1.5f);
			Buff.affect(attacker, Bleeding.class).set(2f);
		}
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BLACKRED;
	}
}
