package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

import java.awt.*;
import java.util.List;

public class EMSuppression extends BaseShipSystemScript {
	public static final Object KEY_SHIP = new Object();
	public static final Object KEY_TARGET = new Object();
	
	public static final float PD_MULT = 0.25f;
	public static final float RANGE = 2000f;
	
	public static final Color TEXT_COLOR = new Color(255,55,55,255);
	
	public static final Color JITTER_COLOR = new Color(255,50,50,75);

	
	public static class TargetData {
		public ShipAPI ship;
		public ShipAPI target;
		public EveryFrameCombatPlugin targetEffectPlugin;
		public float currPDMult;
		public float elaspedAfterInState;
		public TargetData(ShipAPI ship, ShipAPI target) {
			this.ship = ship;
			this.target = target;
		}
	}
	
	
	public void apply(MutableShipStatsAPI stats, final String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		final String targetDataKey = ship.getId() + "_emsuppression_target_data";
		
		Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey); 
		if (state == State.IN && targetDataObj == null) {
			ShipAPI target = findTarget(ship);
			Global.getCombatEngine().getCustomData().put(targetDataKey, new TargetData(ship, target));
			if (target != null) {
				if (target.getFluxTracker().showFloaty() || 
						ship == Global.getCombatEngine().getPlayerShip() ||
						target == Global.getCombatEngine().getPlayerShip()) {
					target.getFluxTracker().showOverloadFloatyIfNeeded(txt("suppressed"), TEXT_COLOR, 4f, true);
				}
			}
		} else if (state == State.IDLE && targetDataObj != null) {
			Global.getCombatEngine().getCustomData().remove(targetDataKey);
			((TargetData)targetDataObj).currPDMult = 1f;
			targetDataObj = null;
		}
		if (targetDataObj == null || ((TargetData) targetDataObj).target == null) return;
		
		final TargetData targetData = (TargetData) targetDataObj;
		targetData.currPDMult = 1f - PD_MULT * effectLevel;
		if (targetData.targetEffectPlugin == null) {
			targetData.targetEffectPlugin = new BaseEveryFrameCombatPlugin() {
				@Override
				public void advance(float amount, List<InputEventAPI> events) {
					if (Global.getCombatEngine().isPaused()) return;
					if (targetData.target == Global.getCombatEngine().getPlayerShip()) { 
						Global.getCombatEngine().maintainStatusForPlayerShip(KEY_TARGET, 
								targetData.ship.getSystem().getSpecAPI().getIconSpriteName(),
								targetData.ship.getSystem().getDisplayName(), 
								txt("suppressed_1") + (int)((1f-targetData.currPDMult) * 100f) + txt("suppressed_2"), true);
					}
					
					if (targetData.currPDMult >= 1f || !targetData.ship.isAlive()) {
						targetData.target.getMutableStats().getNonBeamPDWeaponRangeBonus().unmodify(id);
						targetData.target.getMutableStats().getBeamPDWeaponRangeBonus().unmodify(id);
						targetData.target.getMutableStats().getDamageToFighters().unmodify(id);
						targetData.target.getMutableStats().getDamageToMissiles().unmodify(id);
						Global.getCombatEngine().removePlugin(targetData.targetEffectPlugin);
					} else {
						targetData.target.getMutableStats().getNonBeamPDWeaponRangeBonus().modifyMult(id, targetData.currPDMult);
						targetData.target.getMutableStats().getBeamPDWeaponRangeBonus().modifyMult(id, targetData.currPDMult);
						targetData.target.getMutableStats().getDamageToFighters().modifyMult(id, targetData.currPDMult);
						targetData.target.getMutableStats().getDamageToMissiles().modifyMult(id, targetData.currPDMult);
					}
				}
			};
			Global.getCombatEngine().addPlugin(targetData.targetEffectPlugin);
		}
		
		
		if (effectLevel > 0) {
			if (state != State.IN) {
				targetData.elaspedAfterInState += Global.getCombatEngine().getElapsedInLastFrame();
			}
			float shipJitterLevel = 0;
			if (state == State.IN) {
				shipJitterLevel = effectLevel;
			} else {
				float durOut = 0.5f;
				shipJitterLevel = Math.max(0, durOut - targetData.elaspedAfterInState) / durOut;
			}
			float targetJitterLevel = effectLevel;
			
			float maxRangeBonus = 50f;
			float jitterRangeBonus = shipJitterLevel * maxRangeBonus;
			
			Color color = JITTER_COLOR;
			if (shipJitterLevel > 0) {
				//ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
				ship.setJitter(KEY_SHIP, color, shipJitterLevel, 4, 0f, 0 + jitterRangeBonus * 1f);
			}
			
			if (targetJitterLevel > 0) {
				//target.setJitterUnder(KEY_TARGET, JITTER_UNDER_COLOR, targetJitterLevel, 5, 0f, 15f);
				targetData.target.setJitter(KEY_TARGET, color, targetJitterLevel, 3, 0f, 5f);
			}
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		
	}
	
	protected ShipAPI findTarget(ShipAPI ship) {
		float range = getMaxRange(ship);
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
		ShipAPI target = ship.getShipTarget();
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
			if (dist > range + radSum) target = null;
		} else {
			if (player) {
				target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FIGHTER, range, true);
			} else {
				Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
				if (test instanceof ShipAPI) {
					target = (ShipAPI) test;
					float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
					float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
					if (dist > range + radSum) target = null;
				}
			}
			if (target == null) {
				target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FIGHTER, range, true);
			}
		}
		
		return target;
	}
	
	
	protected float getMaxRange(ShipAPI ship) {
		return RANGE;
	}

	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (effectLevel > 0) {
			if (index == 0) {
				float damMult = PD_MULT * effectLevel;
				return new StatusData(txt("suppression_1") + (int)((damMult - 1f) * 100f) + txt("suppression_2"), false);
			}
		}
		return null;
	}


	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		ShipAPI target = findTarget(ship);
		if (target != null && target != ship) {
			return "READY";
		}
		if ((target == null) && ship.getShipTarget() != null) {
			return "OUT OF RANGE";
		}
		return "NO TARGET";
	}

	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		//if (true) return true;
		ShipAPI target = findTarget(ship);
		return target != null && target != ship;
	}

}








