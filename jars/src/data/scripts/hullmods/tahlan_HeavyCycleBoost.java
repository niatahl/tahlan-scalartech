package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import static data.scripts.utils.tahlan_scalar_txt.txt;

public class tahlan_HeavyCycleBoost extends BaseHullMod {

	private static final float ROF_BOOST = 1.5f;
	private static final float BEAM_BOOST = 1.25f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getEnergyRoFMult().modifyMult(id,ROF_BOOST);
		stats.getBallisticRoFMult().modifyMult(id,ROF_BOOST);
		stats.getBeamWeaponDamageMult().modifyMult(id,BEAM_BOOST);
		stats.getBeamWeaponFluxCostMult().modifyMult(id,BEAM_BOOST);

	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)((ROF_BOOST-1f)*100f) + txt("%");
		if (index == 1) return "" + (int)((BEAM_BOOST-1f)*100f) + txt("%");
		return null;
	}
	

}
