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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class ChampionEnemy extends Buff {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	protected int color;
	protected int rays;

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(color);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.aura( color, rays );
		else target.sprite.clearAura();
	}

	public void onAttackProc(Char enemy ){

	}

	public boolean canAttackWithExtraReach( Char enemy ){
		return false;
	}

	public float meleeDamageFactor(){
		return 1f;
	}

	public float damageTakenFactor(){
		return 1f;
	}

	public float evasionAndAccuracyFactor(){
		return 1f;
	}

	{
		immunities.add(AllyBuff.class);
	}

	@SuppressWarnings("unchecked")
	public static final Class<? extends ChampionEnemy>[] POOL = new Class[]{
			Blazing.class, Projecting.class, AntiMagic.class, Giant.class, Blessed.class, Growing.class,
			Fanatic.class, Cursed.class, StoneSkin.class, ExtraFast.class,
			LightningEnchanted.class, ColdEnchanted.class, MagicResistant.class,
			SpectralHit.class, ManaBurn.class, Vampiric.class
	};

	public static void rollForChampion(Mob m){
		Dungeon.mobsToChampion--;

		//we roll for a champion enemy even if we aren't spawning one to ensure that
		//mobsToChampion does not affect levelgen RNG (number of calls to Random.Int() is constant)
		//roll: 1 modifier 75%, 2 modifiers 20%, 3 modifiers 5%
		float countRoll = Random.Float();
		int count;
		if (countRoll < 0.05f) count = 3;
		else if (countRoll < 0.25f) count = 2;
		else count = 1;

		ArrayList<Class<? extends ChampionEnemy>> pick = new ArrayList<>(Arrays.asList(POOL));
		ArrayList<Class<? extends ChampionEnemy>> chosen = new ArrayList<>();
		for (int i = 0; i < count && !pick.isEmpty(); i++) {
			chosen.add(pick.remove(Random.Int(pick.size())));
		}

		if (Dungeon.mobsToChampion <= 0 && Dungeon.isChallenged(Challenges.CHAMPION_ENEMIES)) {

			//we block certain standout enemies on floor <10 from becoming champions
			if (m instanceof Crab  && Dungeon.scalingDepth() <= 3) return;
			if (m instanceof Thief && Dungeon.scalingDepth() <= 4) return;
			if (m instanceof Guard && Dungeon.scalingDepth() <= 7) return;
			if (m instanceof Bat   && Dungeon.scalingDepth() <= 9) return;

			for (Class<? extends ChampionEnemy> cls : chosen) {
				Buff.affect(m, cls);
			}
			//numbers of mobs until a champion scales from 1/8 to 1/6 as depths increases
			Dungeon.mobsToChampion += 8 - Math.min(20, Dungeon.scalingDepth()-1)/10f;
			if (m.state != m.PASSIVE) {
				m.state = m.WANDERING;
			}
		}
	}

	public static class Blazing extends ChampionEnemy {

		{
			color = 0xFF8800;
			rays = 4;
		}

		@Override
		public void onAttackProc(Char enemy) {
			if (!Dungeon.level.water[enemy.pos]) {
				Buff.affect(enemy, Burning.class).reignite(enemy);
			}
		}

		@Override
		public void detach() {
			//don't trigger when killed by being knocked into a pit
			if (target.flying || !Dungeon.level.pit[target.pos]) {
				for (int i : PathFinder.NEIGHBOURS9) {
					if (!Dungeon.level.solid[target.pos + i] && !Dungeon.level.water[target.pos + i]) {
						GameScene.add(Blob.seed(target.pos + i, 2, Fire.class));
					}
				}
			}
			super.detach();
		}

		@Override
		public float meleeDamageFactor() {
			return 1.25f;
		}

		{
			immunities.add(Burning.class);
		}
	}

	public static class Projecting extends ChampionEnemy {

		{
			color = 0x8800FF;
			rays = 4;
		}

		@Override
		public float meleeDamageFactor() {
			return 1.25f;
		}

		@Override
		public boolean canAttackWithExtraReach(Char enemy) {
			if (Dungeon.level.distance( target.pos, enemy.pos ) > 4){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Char ch : Actor.chars()) {
					//our own tile is always passable
					passable[ch.pos] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.pos, passable, 4);

				return PathFinder.distance[target.pos] <= 4;
			}
		}
	}

	public static class AntiMagic extends ChampionEnemy {

		{
			color = 0x00FF00;
			rays = 5;
		}

		@Override
		public float damageTakenFactor() {
			return 0.5f;
		}

		{
			immunities.addAll(com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic.RESISTS);
		}

	}

	//Also makes target large, see Char.properties()
	public static class Giant extends ChampionEnemy {

		{
			color = 0x0088FF;
			rays = 5;
		}

		@Override
		public float damageTakenFactor() {
			return 0.2f;
		}

		@Override
		public boolean canAttackWithExtraReach(Char enemy) {
			if (Dungeon.level.distance( target.pos, enemy.pos ) > 2){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Char ch : Actor.chars()) {
					//our own tile is always passable
					passable[ch.pos] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.pos, passable, 2);

				return PathFinder.distance[target.pos] <= 2;
			}
		}
	}

	public static class Blessed extends ChampionEnemy {

		{
			color = 0xFFFF00;
			rays = 6;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return 4f;
		}
	}

	public static class Growing extends ChampionEnemy {

		{
			color = 0xFF2222; //a little white helps it stick out from background
			rays = 6;
		}

		private float multiplier = 1.19f;

		@Override
		public boolean act() {
			multiplier += 0.01f;
			spend(4*TICK);
			return true;
		}

		@Override
		public float meleeDamageFactor() {
			return multiplier;
		}

		@Override
		public float damageTakenFactor() {
			return 1f/multiplier;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return multiplier;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", (int)(100*(multiplier-1)), (int)(100*(1 - 1f/multiplier)));
		}

		private static final String MULTIPLIER = "multiplier";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(MULTIPLIER, multiplier);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			multiplier = bundle.getFloat(MULTIPLIER);
		}
	}

	public static class Fanatic extends ChampionEnemy {
		{
			color = 0xFF3333;
			rays = 5;
		}

		@Override
		public float meleeDamageFactor() {
			return 1.4f;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return 1.25f;
		}
	}

	public static class Cursed extends ChampionEnemy {
		{
			color = 0x6611AA;
			rays = 5;
		}

		@Override
		public void onAttackProc(Char enemy) {
			Buff.affect(enemy, Weakness.class, 4f);
		}
	}

	public static class StoneSkin extends ChampionEnemy {
		{
			color = 0x888899;
			rays = 6;
		}

		@Override
		public float damageTakenFactor() {
			return 0.35f;
		}
	}

	public static class ExtraFast extends ChampionEnemy {
		{
			color = 0x55FFAA;
			rays = 5;
		}

		@Override
		public boolean act() {
			//ExtraFast acts at 1.5x speed by spending less time
			spend(-TICK / 2f);
			return super.act();
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return 1.5f;
		}
	}

	public static class LightningEnchanted extends ChampionEnemy {
		{
			color = 0x99CCFF;
			rays = 5;
		}

		@Override
		public void onAttackProc(Char enemy) {
			GameScene.add(Blob.seed(enemy.pos, 2, Electricity.class));
		}
	}

	public static class ColdEnchanted extends ChampionEnemy {
		{
			color = 0xAAEEFF;
			rays = 5;
		}

		@Override
		public void onAttackProc(Char enemy) {
			Buff.affect(enemy, Chill.class, 3f);
		}

		@Override
		public void detach() {
			if (target.flying || !Dungeon.level.pit[target.pos]) {
				for (int i : PathFinder.NEIGHBOURS9) {
					if (!Dungeon.level.solid[target.pos + i]) {
						GameScene.add(Blob.seed(target.pos + i, 2, Freezing.class));
					}
				}
			}
			super.detach();
		}
	}

	public static class MagicResistant extends ChampionEnemy {
		{
			color = 0x00AAFF;
			rays = 6;
		}

		{
			immunities.addAll(com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic.RESISTS);
		}

		@Override
		public float damageTakenFactor() {
			return 0.7f;
		}
	}

	public static class SpectralHit extends ChampionEnemy {
		{
			color = 0xCCAAFF;
			rays = 5;
		}

		@Override
		public void onAttackProc(Char enemy) {
			//random elemental flavour: 50% burn, 50% chill
			if (Random.Int(2) == 0) Buff.affect(enemy, Burning.class).reignite(enemy);
			else Buff.affect(enemy, Chill.class, 2f);
		}
	}

	public static class ManaBurn extends ChampionEnemy {
		{
			color = 0x3366AA;
			rays = 5;
		}

		@Override
		public void onAttackProc(Char enemy) {
			Buff.affect(enemy, Hex.class, 5f);
		}
	}

	public static class Vampiric extends ChampionEnemy {
		{
			color = 0x550000;
			rays = 5;
		}

		@Override
		public void onAttackProc(Char enemy) {
			//heal a fraction of victim's remaining HP
			int heal = Math.max(1, target.HT / 25);
			target.HP = Math.min(target.HT, target.HP + heal);
		}
	}

}
