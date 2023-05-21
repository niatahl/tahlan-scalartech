package org.niatahl.scalartech.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

public class CentralTargetingCore extends BaseHullMod {

	private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);

	static {
		BLOCKED_HULLMODS.add("dedicated_targeting_core");
		BLOCKED_HULLMODS.add("targetingunit");
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}
	
	static float RANGE_BONUS = 80f;
	static final float PD_MINUS = 20f;
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round(RANGE_BONUS) + "%";
		if (index == 1) return "" + (int)Math.round(RANGE_BONUS-PD_MINUS) + "%";
		return null;
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(),tmp,"tahlan_centraltargeting");
			}
		}
	}
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);

        stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, -PD_MINUS);
        stats.getBeamPDWeaponRangeBonus().modifyPercent(id, -PD_MINUS);
	}

}
