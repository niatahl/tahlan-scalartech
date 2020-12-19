package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import static data.scripts.utils.tahlan_scalar_txt.txt;

public class tahlan_CapacitorOverchargeStats extends BaseShipSystemScript {

    private static final float DAMAGE_MULT = 2f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        stats.getEnergyWeaponDamageMult().modifyMult(id,1f+(DAMAGE_MULT-1f)*effectLevel);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponDamageMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("capOvercharge") + (int)((DAMAGE_MULT-1f)*effectLevel*100f)+ txt("%"),false);
        }
        return null;
    }
}
