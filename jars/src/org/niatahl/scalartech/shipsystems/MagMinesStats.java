package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.magiclib.util.MagicLensFlare;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class MagMinesStats extends BaseShipSystemScript implements MineStrikeStatsAIInfoProvider {
	
	public static float MINE_RANGE = 1500f;
	
	public static final float MIN_SPAWN_DIST = 75f;
	
	public static final float LIVE_TIME = 5f;
	
	public static final Color JITTER_COLOR = new Color(175, 236,255,55);
	public static final Color JITTER_UNDER_COLOR = new Color(180, 241,255,125);

    //Some basic teleport-flash config. More detailed config can be scripted in the function near the script's bottom
    private static final Color BASIC_FLASH_COLOR = new Color(84, 255, 218, 200);
    private static final Color BASIC_GLOW_COLOR = new Color(80, 161, 255, 200);

	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		
		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		if (state == State.OUT) {
		}
		
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 6, 0f, 3f + jitterRangeBonus);
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus);
		
		if (state == State.IN) {
		} else if (effectLevel >= 1) {
			Vector2f target = ship.getMouseTarget();
			if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)){
				target = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
			}
			if (target != null) {
				float dist = Misc.getDistance(ship.getLocation(), target);
				float max = getMaxRange(ship) + ship.getCollisionRadius();
				if (dist > max) {
					float dir = Misc.getAngleInDegrees(ship.getLocation(), target);
					target = Misc.getUnitVectorAtDegreeAngle(dir);
					target.scale(max);
					Vector2f.add(target, ship.getLocation(), target);
				}

				for (int i = 0; i<5; i++) {
					target = findClearLocation(ship, target);

					if (target != null) {
						spawnMine(ship, target);
					}
				}
			}
			
		} else if (state == State.OUT ) {
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	public void spawnMine(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		Vector2f currLoc = Misc.getPointAtRadius(mineLoc, 30f + (float) Math.random() * 30f);
		//Vector2f currLoc = null;
		float start = (float) Math.random() * 360f;
		for (float angle = start; angle < start + 390; angle += 30f) {
			if (angle != start) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(50f + (float) Math.random() * 30f);
				currLoc = Vector2f.add(mineLoc, loc, new Vector2f());
			}
			for (MissileAPI other : Global.getCombatEngine().getMissiles()) {
				if (!other.isMine()) continue;
				
				float dist = Misc.getDistance(currLoc, other.getLocation());
				if (dist < other.getCollisionRadius() + 40f) {
					currLoc = null;
					break;
				}
			}
			if (currLoc != null) {
				break;
			}
		}
		if (currLoc == null) {
			currLoc = Misc.getPointAtRadius(mineLoc, 30f + (float) Math.random() * 30f);
		}
		
		
		
		//Vector2f currLoc = mineLoc;
		MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null, 
															  "tahlan_magminelayer",
															  currLoc, 
															  (float) Math.random() * 360f, null);
		spawnTeleportFlash(mine);

		if (source != null) {
			float extraDamageMult = source.getMutableStats().getMissileWeaponDamageMult().getModifiedValue();
			mine.getDamage().setMultiplier(mine.getDamage().getMultiplier() * extraDamageMult);
		}
		
		
		float fadeInTime = 0.5f;
		mine.getVelocity().scale(0);
		mine.fadeOutThenIn(fadeInTime);

		float liveTime = LIVE_TIME;
		//liveTime = 0.01f;
		mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		
		Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, mine.getLocation(), mine.getVelocity());
	}
	
	protected EveryFrameCombatPlugin createMissileJitterPlugin(final MissileAPI mine, final float fadeInTime) {
		return new BaseEveryFrameCombatPlugin() {
			float elapsed = 0f;
			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;
			
				elapsed += amount;
				
				float jitterLevel = mine.getCurrentBaseAlpha();
				if (jitterLevel < 0.5f) {
					jitterLevel *= 2f;
				} else {
					jitterLevel = (1f - jitterLevel) * 2f;
				}
				
				float jitterRange = 1f - mine.getCurrentBaseAlpha();
				//jitterRange = (float) Math.sqrt(jitterRange);
				float maxRangeBonus = 50f;
				float jitterRangeBonus = jitterRange * maxRangeBonus;
				Color c = JITTER_UNDER_COLOR;
				c = Misc.setAlpha(c, 70);
				//mine.setJitter(this, c, jitterLevel, 15, jitterRangeBonus * 0.1f, jitterRangeBonus);
				mine.setJitter(this, c, jitterLevel, 15, jitterRangeBonus * 0, jitterRangeBonus);
				
				if (jitterLevel >= 1 || elapsed > fadeInTime) {
					Global.getCombatEngine().removePlugin(this);
				}
			}
		};
	}

    //The "teleport flash" of a fighter
    //      Anything slapped in here will be spawned as visuals: go ham
    //      Also handles sounds
    private void spawnTeleportFlash(final MissileAPI mine) {

        CombatEngineAPI engine = Global.getCombatEngine();
        //Only spawn visuals when on-screen
        if (Global.getCombatEngine().getViewport().isNearViewport(mine.getLocation(), 500f)) {
            MagicLensFlare.createSharpFlare(engine,mine.getSource(),mine.getLocation(),5f,100f,0f,BASIC_FLASH_COLOR,Color.white);
            engine.addSmoothParticle(mine.getLocation(), ZERO, 100f, 0.7f, 0.1f, BASIC_FLASH_COLOR);
            engine.addSmoothParticle(mine.getLocation(), ZERO, 150f, 0.7f, 1f, BASIC_GLOW_COLOR);
            engine.addHitParticle(mine.getLocation(), ZERO, 200f, 1f, 0.05f, Color.white);


        }
    }
	
	
	protected float getMaxRange(ShipAPI ship) {
		return getMineRange(ship);
	}

	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		Vector2f target = ship.getMouseTarget();
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target);
			float max = getMaxRange(ship) + ship.getCollisionRadius();
			if (dist > max) {
				return "OUT OF RANGE";
			} else {
				return "READY";
			}
		}
		return null;
	}

	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return ship.getMouseTarget() != null;
	}
	
	
	private Vector2f findClearLocation(ShipAPI ship, Vector2f dest) {
		if (isLocationClear(dest)) return dest;
		
		float incr = 50f;

		WeightedRandomPicker<Vector2f> tested = new WeightedRandomPicker<Vector2f>();
		for (float distIndex = 1; distIndex <= 32f; distIndex *= 2f) {
			float start = (float) Math.random() * 360f;
			for (float angle = start; angle < start + 360; angle += 60f) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(incr * distIndex);
				Vector2f.add(dest, loc, loc);
				tested.add(loc);
				if (isLocationClear(loc)) {
					return loc;
				}
			}
		}
		
		if (tested.isEmpty()) return dest; // shouldn't happen
		
		return tested.pick();
	}
	
	private boolean isLocationClear(Vector2f loc) {
		for (ShipAPI other : Global.getCombatEngine().getShips()) {
			if (other.isShuttlePod()) continue;
			if (other.isFighter()) continue;

			Vector2f otherLoc = other.getShieldCenterEvenIfNoShield();
			float otherR = other.getShieldRadiusEvenIfNoShield();


			float dist = Misc.getDistance(loc, otherLoc);
			float r = otherR;
			//r = Math.min(r, Misc.getTargetingRadius(loc, other, false) + r * 0.25f);
			if (dist < r + MIN_SPAWN_DIST) {
				return false;
			}
		}
		for (CombatEntityAPI other : Global.getCombatEngine().getAsteroids()) {
			float dist = Misc.getDistance(loc, other.getLocation());
			if (dist < other.getCollisionRadius() + MIN_SPAWN_DIST) {
				return false;
			}
		}
		
		return true;
	}


	public float getFuseTime() {
		return 3f;
	}

	@Override
	public float getMineRange(ShipAPI ship) {
		return MINE_RANGE;
	}
	
}








