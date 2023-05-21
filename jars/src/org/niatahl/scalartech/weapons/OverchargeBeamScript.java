package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class OverchargeBeamScript implements EveryFrameWeaponEffectPlugin {


    private final IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.1f);
    private boolean hasFiredThisCharge = false;
    private final String CHARGE_SOUND_ID = "tahlan_ocbeam_loop";

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip().getFluxTracker().getFluxLevel() >= 0.4f) {
            weapon.setAmmo(1);
        } else {
            weapon.setAmmo(0);
        }

        //Saves handy variables used later
        float chargelevel = weapon.getChargeLevel();
        String sequenceState = "READY";
        if (chargelevel > 0 && (!weapon.isBeam() || weapon.isFiring())) {
            if (chargelevel >= 1f) {
                sequenceState = "FIRING";
            } else if (!hasFiredThisCharge) {
                sequenceState = "CHARGEUP";
            } else {
                sequenceState = "CHARGEDOWN";
            }
        } else if (weapon.getCooldownRemaining() > 0) {
            sequenceState = "COOLDOWN";
        }

        //Adjustment for burst beams, since they are a pain
        if (weapon.isBurstBeam() && sequenceState.contains("CHARGEDOWN")) {
            chargelevel = Math.max(0f, Math.min(Math.abs(weapon.getCooldownRemaining()-weapon.getCooldown()) / weapon.getSpec().getDerivedStats().getBurstFireDuration(), 1f));
        }

        if (hasFiredThisCharge && (chargelevel <= 0f || !weapon.isFiring())) {
            hasFiredThisCharge = false;
        }

        if (chargelevel >= 1f && !hasFiredThisCharge) {
            hasFiredThisCharge = true;
        }

        if (chargelevel > 0f && sequenceState=="CHARGEUP") {
            Global.getSoundPlayer().playLoop(CHARGE_SOUND_ID, weapon, (0.5f + weapon.getChargeLevel()), (0.5f + (weapon.getChargeLevel() * 0.4f)), weapon.getLocation(), new Vector2f(0f, 0f));

            effectInterval.advance(engine.getElapsedInLastFrame());
            if (effectInterval.intervalElapsed()){
                Vector2f arcPoint = MathUtils.getRandomPointInCircle(weapon.getLocation(),75f*chargelevel);
                EmpArcEntityAPI arc =  engine.spawnEmpArcPierceShields(weapon.getShip(), weapon.getLocation(), weapon.getShip(),
                        new SimpleEntity(arcPoint),
                        DamageType.FRAGMENTATION,
                        0f,
                        0f,
                        75f,
                        null,
                        5f+5f*chargelevel,
                        new Color(50,200,255),
                        Color.white

                );
            }
        }
    }
}
