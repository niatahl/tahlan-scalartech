package org.niatahl.scalartech.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import org.magiclib.util.MagicIncompatibleHullmods;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class ScalarAutoforge extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

            if (ship.getVariant().getHullMods().contains("missleracks")) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(),"missleracks","tahlan_scalarautoforge");
            }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if ( index == 0 ) return txt("hmd_ScalarForge1");
        if ( index == 1 ) return txt("hmd_ScalarForge2");
        if ( index == 2 ) return txt("hmd_ScalarForge3");
        if ( index == 3 ) return txt("hmd_ScalarForge4");
        if ( index == 4 ) return txt("EMR");
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
