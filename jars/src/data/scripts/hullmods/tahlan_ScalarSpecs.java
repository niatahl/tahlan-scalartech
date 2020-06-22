package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static data.scripts.utils.tahlan_txt.txt;

public class tahlan_ScalarSpecs extends BaseHullMod {

	private static final float ZERO_FLUX_BOOST = 20f;
	private static final float EMERGENCY_BOOST = 1.1f;

	private static final String ID = "ScalarDrivesID";

    private final String INNERLARGE = "graphics/tahlan/fx/tahlan_scalarshield.png";
    private final String OUTERLARGE = "graphics/tahlan/fx/tahlan_scalarshield_ring.png";

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getZeroFluxSpeedBoost().modifyFlat(id,ZERO_FLUX_BOOST);

	}

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getShield().setRadius(ship.getShieldRadiusEvenIfNoShield(), INNERLARGE, OUTERLARGE);
        if (ship.getVariant().hasHullMod("unstable_injector")) {
            ship.getMutableStats().getZeroFluxSpeedBoost().modifyFlat(id,ZERO_FLUX_BOOST/2);
        }
        if (ship.getVariant().hasHullMod("safetyoverrides")) {
            ship.getMutableStats().getZeroFluxSpeedBoost().unmodify(id);
        }
    }

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
	    float uiFactor = 1f;
	    if (ship.getVariant().hasHullMod("unstable_injector")) {
	        uiFactor = 0.5f;
        }
		if (ship.getCurrentCR()<ship.getCRAtDeployment() && !ship.getVariant().hasHullMod("safetyoverrides")) {
			ship.getMutableStats().getMaxSpeed().modifyMult(ID,EMERGENCY_BOOST*uiFactor);
		} else {
			ship.getMutableStats().getMaxSpeed().unmodify(ID);
		}
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)ZERO_FLUX_BOOST;
		if (index == 1) return "" + (int)(100f*(EMERGENCY_BOOST-1f)) + txt("%");
		if (index == 2) return txt("UI");
		if (index == 3) return txt("halve");
		if (index == 4) return txt("SO");
		return null;
	}
	

}
