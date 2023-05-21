package org.niatahl.scalartech.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class BansheeMounts extends BaseHullMod {

    static final float RANGE_BOOST = 150f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponRangeBonus().modifyFlat(id,RANGE_BOOST);
        stats.getBallisticWeaponRangeBonus().modifyFlat(id,RANGE_BOOST);
        stats.getBeamPDWeaponRangeBonus().modifyFlat(id,-RANGE_BOOST);
        stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id,-RANGE_BOOST);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "200"+txt("su");
        return null;
    }
}
