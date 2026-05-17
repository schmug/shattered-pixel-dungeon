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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.prefixes;

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

//Diablo-style leading affix. Mirrors armor Suffix, but prefixes are
//primarily static stat modifiers rather than on-hit procs.
public abstract class Prefix implements Bundlable {

	public static final Class<?>[] common = new Class<?>[]{
			Sturdy.class, Reinforced.class, Padded.class, Lined.class };

	public static final Class<?>[] uncommon = new Class<?>[]{
			Fortified.class, Plated.class, Resilient.class, Bolstered.class };

	public static final Class<?>[] rare = new Class<?>[]{
			Impenetrable.class, Stalwart.class, Adamant.class };

	public static final float[] typeChances = new float[]{
			50, //12.5% each
			40, //10%   each
			10  //3.33% each
	};

	public static final Class<?>[] curses = new Class<?>[]{
			Brittle.class, Rusted.class, Frail.class
	};

	//static damage-reduction modifier; override for defense prefixes
	public int defenseFactor( int dr ) {
		return dr;
	}

	//static evasion modifier; override for evasion prefixes
	public float evasionFactor( float evasion ) {
		return evasion;
	}

	//kept for parity with Suffix; prefixes are static, so default is identity
	public int proc( Armor armor, Char attacker, Char defender, int damage ) {
		return damage;
	}

	public void activate( Hero hero, Armor armor ) {
	}

	public void deactivate( Hero hero, Armor armor ) {
	}

	public String name() {
		if (!curse())
			return name( Messages.get(this, "prefix"));
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

	//pure name-composition helper: prepends the prefix word to the rest of
	//the assembled item name with exactly one separating space, tolerating
	//an absent prefix. Asset-free so the ordering/spacing contract is unit
	//testable without the Gdx-coupled Messages.
	public static String joinNameParts( String prefixWord, String body ) {
		if (prefixWord == null || prefixWord.trim().isEmpty()) {
			return body;
		}
		return prefixWord.trim() + " " + body;
	}

	@SuppressWarnings("unchecked")
	public static Prefix random( Class<? extends Prefix> ... toIgnore ) {
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
	public static Prefix randomCommon( Class<? extends Prefix> ... toIgnore ) {
		ArrayList<Class<?>> pool = new ArrayList<>(Arrays.asList(common));
		pool.removeAll(Arrays.asList(toIgnore));
		if (pool.isEmpty()) return random();
		return (Prefix) Reflection.newInstance(Random.element(pool));
	}

	@SuppressWarnings("unchecked")
	public static Prefix randomUncommon( Class<? extends Prefix> ... toIgnore ) {
		ArrayList<Class<?>> pool = new ArrayList<>(Arrays.asList(uncommon));
		pool.removeAll(Arrays.asList(toIgnore));
		if (pool.isEmpty()) return random();
		return (Prefix) Reflection.newInstance(Random.element(pool));
	}

	@SuppressWarnings("unchecked")
	public static Prefix randomRare( Class<? extends Prefix> ... toIgnore ) {
		ArrayList<Class<?>> pool = new ArrayList<>(Arrays.asList(rare));
		pool.removeAll(Arrays.asList(toIgnore));
		if (pool.isEmpty()) return random();
		return (Prefix) Reflection.newInstance(Random.element(pool));
	}

	@SuppressWarnings("unchecked")
	public static Prefix randomCurse( Class<? extends Prefix> ... toIgnore ) {
		ArrayList<Class<?>> pool = new ArrayList<>(Arrays.asList(curses));
		pool.removeAll(Arrays.asList(toIgnore));
		if (pool.isEmpty()) return random();
		return (Prefix) Reflection.newInstance(Random.element(pool));
	}
}
