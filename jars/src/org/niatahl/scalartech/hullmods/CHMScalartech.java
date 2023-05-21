package org.niatahl.scalartech.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class CHMScalartech extends BaseHullMod {

    private static final float EFFICIENCY_MULT = 0.05f;
    private static final float CAPACITY_MULT = 0.05f;
    private static final float SPEED_BONUS = 10f;
    private static final float SPEED_THRESHOLD = 0.10f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFuelUseMod().modifyMult(id,1f-EFFICIENCY_MULT);
        stats.getFuelMod().modifyPercent(id,CAPACITY_MULT*100f);
        stats.getSuppliesPerMonth().modifyMult(id,1f-EFFICIENCY_MULT);
        stats.getCargoMod().modifyPercent(id,CAPACITY_MULT*100f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getVariant().hasHullMod("CHM_commission")) {
            ship.getVariant().removeMod("CHM_commission");
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        String id = "CHM_scalartech_id";
        if (ship.getFluxLevel() < SPEED_THRESHOLD) {
            ship.getMutableStats().getMaxSpeed().modifyPercent(id,SPEED_BONUS);
        } else {
            ship.getMutableStats().getMaxSpeed().unmodify(id);
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int)(CAPACITY_MULT*100f) + txt("%");
        if (index == 1) return "" + (int)(EFFICIENCY_MULT*100f) + txt("%");
        if (index == 2) return "" + (int)SPEED_BONUS + txt("%");
        if (index == 3) return "" + (int)(SPEED_THRESHOLD*100f) + txt("%");
        return null;
    }
}
