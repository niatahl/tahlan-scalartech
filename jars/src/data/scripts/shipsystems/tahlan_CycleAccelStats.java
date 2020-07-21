package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import static data.scripts.utils.tahlan_scalar_txt.txt;

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
