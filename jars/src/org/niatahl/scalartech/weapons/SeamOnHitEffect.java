package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.niatahl.scalartech.ScalarModPlugin;
import org.magiclib.util.MagicRender;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static org.niatahl.scalartech.utils.GraphicLibEffects.CustomRippleDistortion;

public class SeamOnHitEffect implements OnHitEffectPlugin {
    private static final Color CORE_EXPLOSION_COLOR = new Color(124, 242, 255, 255);
    private static final Color CORE_GLOW_COLOR = new Color(153, 200, 241, 150);
    private static final Color EXPLOSION_COLOR = new Color(183, 218, 255, 10);
    private static final String SOUND_ID = "tahlan_cashmere_impact";
    private static final Vector2f ZERO = new Vector2f();


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile.didDamage() && !(target instanceof MissileAPI) && Math.random()>0.95) {

            // Blast visuals
            float CoreExplosionRadius = 70f;
            float CoreExplosionDuration = 1f;
            float ExplosionRadius = 120f;
            float ExplosionDuration = 0.2f;
            float CoreGlowRadius = 300f;
            float CoreGlowDuration = 0.2f;

            engine.spawnExplosion(point, ZERO, CORE_EXPLOSION_COLOR, CoreExplosionRadius, CoreExplosionDuration);
            engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, ExplosionRadius, ExplosionDuration);
            engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_GLOW_COLOR);
            //Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, point, ZERO);

            float bonusDamage = projectile.getDamageAmount()*5f;
            DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                    200f,
                    100f,
                    bonusDamage,
                    bonusDamage/2f,
                    CollisionClass.PROJECTILE_FF,
                    CollisionClass.PROJECTILE_FIGHTER,
                    10f,
                    10f,
                    0f,
                    0,
                    CORE_EXPLOSION_COLOR,
                    null);
            blast.setDamageType(DamageType.HIGH_EXPLOSIVE);
            blast.setShowGraphic(false);
            engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","tahlan_ruffle_blast"),
                    point,
                    new Vector2f(),
                    new Vector2f(100,100),
                    new Vector2f(360,360),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(235, 87, 255,205),
                    true,
                    0,
                    0.05f,
                    0.05f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","tahlan_ruffle_blast"),
                    point,
                    new Vector2f(),
                    new Vector2f(80,80),
                    new Vector2f(240,240),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(139, 67, 255,175),
                    true,
                    0.1f,
                    0.0f,
                    0.1f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","tahlan_ruffle_blast"),
                    point,
                    new Vector2f(),
                    new Vector2f(140,140),
                    new Vector2f(100,100),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(255, 87, 146,150),
                    true,
                    0.2f,
                    0.0f,
                    0.2f
            );


            if (ScalarModPlugin.isGraphicsLibAvailable()) {
                CustomRippleDistortion(point,ZERO,200,3f,false,0f,360f,0.5f,0f,0.15f,0.15f,0.3f,0f);
            }

        }
    }
}
