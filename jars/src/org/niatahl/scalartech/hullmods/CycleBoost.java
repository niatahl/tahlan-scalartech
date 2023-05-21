package org.niatahl.scalartech.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class CycleBoost extends BaseHullMod {

	private static final float ROF_BOOST = 2f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getEnergyRoFMult().modifyMult(id,ROF_BOOST);
		stats.getEnergyAmmoRegenMult().modifyMult(id,ROF_BOOST);

	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)((ROF_BOOST-1f)*100f) + txt("%");
		return null;
	}
//
//	@Override
//	public void advanceInCombat(ShipAPI ship, float amount) {
//		CombatEngineAPI engine = Global.getCombatEngine();
//		if (engine.isPaused() || !ship.isAlive()) {
//			return;
//		}
//		for (WeaponAPI w : ship.getAllWeapons()) {
//			//only bother with ammo regenerators
//
//			float reloadRate = w.getSpec().getAmmoPerSecond();
//			float nuCharge = reloadRate * ROF_BOOST;
//			if (w.getType() == WeaponAPI.WeaponType.ENERGY && w.usesAmmo() && reloadRate > 0) {
//				w.getAmmoTracker().setAmmoPerSecond(nuCharge);
//			}
//		}
//	}
	

}
