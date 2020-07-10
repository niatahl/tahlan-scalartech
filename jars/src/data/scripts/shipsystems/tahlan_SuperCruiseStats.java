package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

import static data.scripts.utils.tahlan_scalar_txt.txt;

public class tahlan_SuperCruiseStats extends BaseShipSystemScript {

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
        } else {
            stats.getMaxTurnRate().modifyMult(id, 1f + 0.5f * effectLevel);
            stats.getMaxSpeed().modifyFlat(id, 30f * effectLevel);
        }

        stats.getAcceleration().modifyPercent(id, 60f * effectLevel);
        stats.getDeceleration().modifyPercent(id, 60f * effectLevel);
        stats.getTurnAcceleration().modifyMult(id, 1f + 0.5f * effectLevel);

        stats.getBallisticWeaponRangeBonus().modifyMult(id, 1f - 0.33f * effectLevel);
        stats.getEnergyWeaponRangeBonus().modifyMult(id, 1f - 0.33f * effectLevel);
        stats.getBallisticRoFMult().modifyMult(id, 1f - 0.33f * effectLevel);
        stats.getEnergyWeaponDamageMult().modifyMult(id, 1f - 0.33f * effectLevel);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - 0.33f * effectLevel);
        stats.getFluxDissipation().modifyPercent(id, -33f * effectLevel);

        ship.getEngineController().extendFlame(id, 1.2f, 1.2f, 1.2f);
        ship.getEngineController().fadeToOtherColor(id, new Color(255,0,100), null, effectLevel, 0.7f);

    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);

        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getFluxDissipation().unmodify(id);

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("sys_supercruise"), false);
        }
        return null;
    }

}
