package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class LaceScript implements EveryFrameWeaponEffectPlugin {

    private static final Color PARTICLE_COLOR = new Color(100,200,255);
    private static final Color GLOW_COLOR = new Color(69, 206, 255, 50);
    private static final Color FLASH_COLOR = new Color(223, 250, 255);

    private boolean hasFiredThisCharge = false;

    private int currentBarrel = 0;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon == null) {
            return;
        }

        float chargelevel = weapon.getChargeLevel();

        if (hasFiredThisCharge && (chargelevel <= 0f || !weapon.isFiring())) {
            hasFiredThisCharge = false;
        }

        //Muzzle location calculation
        Vector2f point = new Vector2f();
        Vector2f point2 = new Vector2f();

        if (weapon.getSlot().isHardpoint()) {
            point.x = weapon.getSpec().getHardpointFireOffsets().get(0).x;
            point.y = weapon.getSpec().getHardpointFireOffsets().get(0).y;
        } else if (weapon.getSlot().isTurret()) {
            point.x = weapon.getSpec().getTurretFireOffsets().get(0).x;
            point.y = weapon.getSpec().getTurretFireOffsets().get(0).y;
        } else {
            point.x = weapon.getSpec().getHiddenFireOffsets().get(0).x;
            point.y = weapon.getSpec().getHiddenFireOffsets().get(0).y;
        }

        if (weapon.getSlot().isHardpoint()) {
            point2.x = weapon.getSpec().getHardpointFireOffsets().get(1).x;
            point2.y = weapon.getSpec().getHardpointFireOffsets().get(1).y;
        } else if (weapon.getSlot().isTurret()) {
            point2.x = weapon.getSpec().getTurretFireOffsets().get(1).x;
            point2.y = weapon.getSpec().getTurretFireOffsets().get(1).y;
        } else {
            point2.x = weapon.getSpec().getHiddenFireOffsets().get(1).x;
            point2.y = weapon.getSpec().getHiddenFireOffsets().get(1).y;
        }

        point = VectorUtils.rotate(point, weapon.getCurrAngle(), new Vector2f(0f, 0f));
        point.x += weapon.getLocation().x;
        point.y += weapon.getLocation().y;

        point2 = VectorUtils.rotate(point2, weapon.getCurrAngle(), new Vector2f(0f, 0f));
        point2.x += weapon.getLocation().x;
        point2.y += weapon.getLocation().y;


        //Firing visuals
        if (chargelevel >= 1f && !hasFiredThisCharge) {
            hasFiredThisCharge = true;

            if (currentBarrel == 0) {
                Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f, 0f), PARTICLE_COLOR, 20f, 0.2f);
                //Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f, 0f), FLASH_COLOR, 15f, 0.2f);
                engine.addSmoothParticle(point, ZERO, 40f, 0.7f, 0.1f, PARTICLE_COLOR);
                engine.addSmoothParticle(point, ZERO, 60f, 0.7f, 1f, GLOW_COLOR);
                //engine.addHitParticle(point, ZERO, 200f, 1f, 0.05f, FLASH_COLOR);
                currentBarrel = 1;
            } else {
                Global.getCombatEngine().spawnExplosion(point2, new Vector2f(0f, 0f), PARTICLE_COLOR, 20f, 0.2f);
                //Global.getCombatEngine().spawnExplosion(point2, new Vector2f(0f, 0f), FLASH_COLOR, 15f, 0.2f);
                engine.addSmoothParticle(point2, ZERO, 40f, 0.7f, 0.1f, PARTICLE_COLOR);
                engine.addSmoothParticle(point2, ZERO, 60f, 0.7f, 1f, GLOW_COLOR);
                //engine.addHitParticle(point2, ZERO, 200f, 1f, 0.05f, FLASH_COLOR);
                currentBarrel = 0;
            }
        }


    }
}


