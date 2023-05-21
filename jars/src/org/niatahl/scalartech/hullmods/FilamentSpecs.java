package org.niatahl.scalartech.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class FilamentSpecs extends BaseHullMod {

	private static final float REFIT_BONUS = 15f;
	private static final float RATE_DECREASE_MODIFIER = 15f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getFighterRefitTimeMult().modifyMult(id, 1-(REFIT_BONUS/100));
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f - RATE_DECREASE_MODIFIER / 100f);

	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)REFIT_BONUS + txt("%");
		return null;
	}
	

}
