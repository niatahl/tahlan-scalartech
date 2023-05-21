package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class CycleAccelStats extends BaseShipSystemScript {

    private static final float ROF_MULT = 3f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        float currMult = 1f+(ROF_MULT-1f)*effectLevel;
        stats.getEnergyWeaponFluxCostMod().modifyMult(id,1f/currMult);
        stats.getEnergyRoFMult().modifyMult(id,currMult);
        stats.getEnergyAmmoRegenMult().modifyMult(id,currMult);

//        for (WeaponAPI w : ship.getAllWeapons()) {
//            //only bother with ammo regenerators
//
//            float reloadRate = w.getSpec().getAmmoPerSecond();
//            float nuCharge = reloadRate * ROF_MULT;
//            if (w.getType() == WeaponAPI.WeaponType.ENERGY && w.usesAmmo() && reloadRate > 0) {
//                w.getAmmoTracker().setAmmoPerSecond(nuCharge);
//            }
//        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getEnergyAmmoRegenMult().unmodify(id);

//        for (WeaponAPI w : ship.getAllWeapons()) {
//            //only bother with ammo regenerators
//
//            float reloadRate = w.getSpec().getAmmoPerSecond();
//            if (w.getType() == WeaponAPI.WeaponType.ENERGY && w.usesAmmo() && reloadRate > 0) {
//                w.getAmmoTracker().setAmmoPerSecond(reloadRate);
//            }
//        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("cycleaccel_rof") + (int)((ROF_MULT-1f)*effectLevel*100f)+ txt("%"),false);
        }
        if (index == 1) {
            return new StatusData(txt("cycleaccel_flux") + (int)((1f-(1f/(1f+(ROF_MULT-1f)*effectLevel)))*100f) + txt("%"),false);
        }
        return null;
    }
}
