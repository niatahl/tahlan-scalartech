package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class OverchargeBeamEffect implements BeamEffectPlugin {

	private IntervalUtil effectInterval = new IntervalUtil(0.08f, 0.12f);
	private boolean wasZero = true;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

		if (engine.isPaused()) {
			return;
		}

		if (beam.getBrightness() >= 1) {
		    effectInterval.advance(engine.getElapsedInLastFrame());
		    if (effectInterval.intervalElapsed()){
				Vector2f dir = Vector2f.sub(beam.getTo(), beam.getFrom(), new Vector2f());
				dir.scale(MathUtils.getRandomNumberInRange(0f,1f));

				Vector2f point = Vector2f.add(beam.getFrom(),dir,new Vector2f());

				EmpArcEntityAPI arc =  engine.spawnEmpArcPierceShields(beam.getSource(), beam.getFrom(), beam.getSource(),
						new SimpleEntity(point),
						DamageType.FRAGMENTATION,
						0f,
						0f,
						10000f,
						null,
						beam.getWidth()*0.5f,
						beam.getFringeColor(),
						beam.getCoreColor()
				);

		        point = MathUtils.getRandomPointInCircle(beam.getFrom(),50f);
		        arc =  engine.spawnEmpArcPierceShields(beam.getSource(), beam.getFrom(), beam.getSource(),
                        new SimpleEntity(point),
                        DamageType.FRAGMENTATION,
                        0f,
                        0f,
                        75f,
                        null,
                        beam.getWidth()*0.5f,
                        beam.getFringeColor(),
                        beam.getCoreColor()
                );

		        if (beam.getDamageTarget() != null) {
		        	engine.addHitParticle(beam.getTo(),new Vector2f(0,0),MathUtils.getRandomNumberInRange(10,20),1f,0.1f,beam.getFringeColor());
				}
            }
        }

        CombatEntityAPI target = beam.getDamageTarget();
        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            float dur = beam.getDamage().getDpsDuration();
            // needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
            if (!wasZero) dur = 0;
            wasZero = beam.getDamage().getDpsDuration() <= 0;
            if (effectInterval.intervalElapsed()) {
                ShipAPI ship = (ShipAPI) target;
                boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
                pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                boolean piercedShield = hitShield && (float) Math.random() < pierceChance;
                //piercedShield = true;

                if (!hitShield || piercedShield) {

                        float dam = beam.getWeapon().getDamage().getDamage() * 0.25f;
                        float empdam = beam.getWeapon().getDamage().getFluxComponent() * 0.5f;
                        EmpArcEntityAPI arc =  engine.spawnEmpArc(beam.getSource(), beam.getTo(), beam.getDamageTarget(), beam.getDamageTarget(),
                                DamageType.ENERGY,
                                dam,
                                dam,
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
