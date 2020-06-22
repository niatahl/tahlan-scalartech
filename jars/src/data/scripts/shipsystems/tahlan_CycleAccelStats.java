package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class tahlan_CycleAccelStats extends BaseShipSystemScript {

    private static final float ROF_MULT = 3f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getEnergyWeaponFluxCostMod().modifyMult(id,1f/(1f+(ROF_MULT-1f)*effectLevel));
        stats.getEnergyRoFMult().modifyMult(id,1f+(ROF_MULT-1f)*effectLevel);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
    }
}
