package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class SuperCruiseStats extends BaseShipSystemScript {

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
            stats.getMaxSpeed().modifyFlat(id, 40f * effectLevel);
        }

        stats.getAcceleration().modifyPercent(id, 50f * effectLevel);
        stats.getDeceleration().modifyPercent(id, 50f * effectLevel);
        stats.getTurnAcceleration().modifyMult(id, 1f + 0.5f * effectLevel);

        stats.getBallisticWeaponRangeBonus().modifyMult(id, 1f - 0.33f * effectLevel);
        stats.getEnergyWeaponRangeBonus().modifyMult(id, 1f - 0.33f * effectLevel);
        stats.getFluxDissipation().modifyMult(id, 1f- 0.33f * effectLevel);

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
        stats.getFluxDissipation().unmodify(id);

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("sys_supercruise"), false);
        }
        return null;
    }

}
