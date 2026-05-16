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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Suffix implements Bundlable {

	public static final Class<?>[] common = new Class<?>[]{
			OfProtection.class, OfTheBear.class, OfDeflection.class, OfWarding.class };

	public static final Class<?>[] uncommon = new Class<?>[]{
			OfAbsorption.class, OfTheEagle.class, OfStability.class, OfTheMind.class };

	public static final Class<?>[] rare = new Class<?>[]{
			OfTheZodiac.class, OfTheTitans.class, OfTheWhale.class };

	public static final float[] typeChances = new float[]{
			50, //12.5% each
			40, //10%   each
			10  //3.33% each
	};

	public static final Class<?>[] curses = new Class<?>[]{
			OfTheVampire.class, OfDespair.class, OfTheJackal.class
	};

	public int proc( Armor armor, Char attacker, Char defender, int damage ) {
		return damage;
	}

	public void activate( Hero hero, Armor armor ) {
	}

	public void deactivate( Hero hero, Armor armor ) {
	}

	public String name() {
		if (!curse())
			return name( Messages.get(this, "suffix"));
		else
			return name( Messages.get(Item.class, "curse"));
	}

	public String name( String armorName ) {
		return Messages.get(this, "name", armorName);
	}

	public String desc() {
		return Messages.get(this, "desc");
	}

	public boolean curse() {
		return false;
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
	}

	public abstract ItemSprite.Glowing glowing();

	@SuppressWarnings("unchecked")
	public static Suffix random( Class<? extends Suffix> ... toIgnore ) {
		switch (Random.chances(typeChances)) {
			case 0: default:
				return randomCommon(toIgnore);
			case 1:
				return randomUncommon(toIgnore);
			case 2:
				return randomRare(toIgnore);
		}
	}

	@SuppressWarnings("unchecked")
	public static Suffix randomCommon( Class<? extends Suffix> ... toIgnore ) {
		ArrayList<Class<?>> pool = new ArrayList<>(Arrays.asList(common));
		pool.removeAll(Arrays.asList(toIgnore));
		if (pool.isEmpty()) return random();
		return (Suffix) Reflection.newInstance(Random.element(pool));
	}

	@SuppressWarnings("unchecked")
	public static Suffix randomUncommon( Class<? extends Suffix> ... toIgnore ) {
		ArrayList<Class<?>> pool = new ArrayList<>(Arrays.asList(uncommon));
		pool.removeAll(Arrays.asList(toIgnore));
		if (pool.isEmpty()) return random();
		return (Suffix) Reflection.newInstance(Random.element(pool));
	}

	@SuppressWarnings("unchecked")
	public static Suffix randomRare( Class<? extends Suffix> ... toIgnore ) {
		ArrayList<Class<?>> pool = new ArrayList<>(Arrays.asList(rare));
		pool.removeAll(Arrays.asList(toIgnore));
		if (pool.isEmpty()) return random();
		return (Suffix) Reflection.newInstance(Random.element(pool));
	}

	@SuppressWarnings("unchecked")
	public static Suffix randomCurse( Class<? extends Suffix> ... toIgnore ) {
		ArrayList<Class<?>> pool = new ArrayList<>(Arrays.asList(curses));
		pool.removeAll(Arrays.asList(toIgnore));
		if (pool.isEmpty()) return random();
		return (Suffix) Reflection.newInstance(Random.element(pool));
	}
}
