package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class LoaderJetsStats extends BaseShipSystemScript {

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			stats.getMaxTurnRate().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, 30f);
			stats.getAcceleration().modifyPercent(id, 100f * effectLevel);
			stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, 20f * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, 100f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 10f);
			stats.getMaxTurnRate().modifyPercent(id, 100f);
		}

	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("+50 top speed", false);
		}
		return null;
	}
}
