package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;

public class StitchBeamEffect implements BeamEffectPlugin {

	private IntervalUtil fireInterval = new IntervalUtil(0.1f, 0.2f);
	private boolean wasZero = true;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			float dur = beam.getDamage().getDpsDuration();
			// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			fireInterval.advance(dur);
			if (fireInterval.intervalElapsed()) {
				ShipAPI ship = (ShipAPI) target;
				boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
				float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
				pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

				boolean piercedShield = hitShield && (float) Math.random() < pierceChance;
				//piercedShield = true;

				if (!hitShield || piercedShield) {

					if (Math.random() > 0.5f) {

						float dam = beam.getWeapon().getDamage().getDamage() * 0.1f;
						float empdam = beam.getWeapon().getDamage().getFluxComponent() * 0.2f;
						EmpArcEntityAPI arc =  engine.spawnEmpArcPierceShields(beam.getSource(), beam.getTo(), beam.getDamageTarget(), beam.getDamageTarget(),
								DamageType.ENERGY,
								dam,
								empdam,
								10000f,
								"tachyon_lance_emp_impact",
								beam.getWidth(),
								beam.getFringeColor(),
								beam.getCoreColor()
						);
					}
				}
			}
		}
	}
}
