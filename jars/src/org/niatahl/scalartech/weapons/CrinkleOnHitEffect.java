package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CrinkleOnHitEffect implements OnHitEffectPlugin {
    private static final Color CORE_EXPLOSION_COLOR = new Color(181, 224, 255, 255);
    private static final Color CORE_GLOW_COLOR = new Color(210, 226, 241, 150);
    private static final Color EXPLOSION_COLOR = new Color(190, 199, 255, 10);
    private static final Color FLASH_GLOW_COLOR = new Color(213, 233, 241, 200);
    private static final Color GLOW_COLOR = new Color(192, 217, 255, 50);
    private static final Color ARC_FRINGE_COLOR = new Color(53, 218, 255);
    private static final Color ARC_CORE_COLOR = new Color(142, 252, 205);
    private static final Vector2f ZERO = new Vector2f();

    private static final int NUM_PARTICLES = 50;
    private static final int NUM_ARCS = 5;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile.didDamage() && !(target instanceof MissileAPI)) {

            // Blast visuals
            float CoreExplosionRadius = 70f;
            float CoreExplosionDuration = 1f;
            float ExplosionRadius = 200f;
            float ExplosionDuration = 1f;
            float CoreGlowRadius = 300f;
            float CoreGlowDuration = 1f;
            float GlowRadius = 400f;
            float GlowDuration = 1f;
            float FlashGlowRadius = 500f;
            float FlashGlowDuration = 0.05f;

            engine.spawnExplosion(point, ZERO, CORE_EXPLOSION_COLOR, CoreExplosionRadius, CoreExplosionDuration);
            engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, ExplosionRadius, ExplosionDuration);
            engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_GLOW_COLOR);
            engine.addSmoothParticle(point, ZERO, GlowRadius, 1f, GlowDuration, GLOW_COLOR);
            engine.addHitParticle(point, ZERO, FlashGlowRadius, 1f, FlashGlowDuration, FLASH_GLOW_COLOR);

            for (int x = 0; x < NUM_PARTICLES; x++) {
                engine.addHitParticle(point,
                        MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 150f), (float) Math.random() * 360f),
                        MathUtils.getRandomNumberInRange(4, 8), 1f, MathUtils.getRandomNumberInRange(0.4f, 0.9f), CORE_EXPLOSION_COLOR);
            }

            // Arcing stuff
            List<CombatEntityAPI> validTargets = new ArrayList<CombatEntityAPI>();
            for (CombatEntityAPI entityToTest : CombatUtils.getEntitiesWithinRange(point, 300)) {
                if (entityToTest instanceof ShipAPI || entityToTest instanceof AsteroidAPI || entityToTest instanceof MissileAPI) {
                    //Phased targets, and targets with no collision, are ignored
                    if (entityToTest instanceof ShipAPI) {
                        if (((ShipAPI) entityToTest).isPhased()) {
                            continue;
                        }
                    }
                    if (entityToTest.getCollisionClass().equals(CollisionClass.NONE)) {
                        continue;
                    }
                    if (entityToTest.getOwner() == projectile.getWeapon().getShip().getOwner()) {
                        continue;
                    }

                    validTargets.add(entityToTest);
                }
            }

            for (int x = 0; x < NUM_ARCS; x++) {
                //If we have no valid targets, zap a random point near us
                if (validTargets.isEmpty()) {
                    validTargets.add(new SimpleEntity(MathUtils.getRandomPointInCircle(point, 300)));
                }

                float bonusDamage = projectile.getDamageAmount()*0.1875f;

                //And finally, fire at a random valid target
                CombatEntityAPI arcTarget = validTargets.get(MathUtils.getRandomNumberInRange(0, validTargets.size() - 1));
                Global.getCombatEngine().spawnEmpArc(projectile.getSource(), point, projectile, arcTarget,
                        DamageType.ENERGY, //Damage type
                        MathUtils.getRandomNumberInRange(0.8f, 1.2f) * bonusDamage, //Damage
                        MathUtils.getRandomNumberInRange(0.8f, 1.2f) * bonusDamage, //Emp
                        100000f, //Max range
                        "tachyon_lance_emp_impact", //Impact sound
                        10f, // thickness of the lightning bolt
                        ARC_CORE_COLOR, //Central color
                        ARC_FRINGE_COLOR //Fringe Color
                );
            }
        }
    }
}
