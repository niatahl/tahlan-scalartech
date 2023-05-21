package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class RuffleOnHitEffect implements OnHitEffectPlugin {
    private static final Color CORE_EXPLOSION_COLOR = new Color(216, 156, 255, 255);
    private static final Color CORE_GLOW_COLOR = new Color(228, 213, 241, 150);
    private static final Color EXPLOSION_COLOR = new Color(232, 176, 255, 10);
    private static final String SOUND_ID = "tahlan_cashmere_impact";
    private static final Vector2f ZERO = new Vector2f();


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile.didDamage() && !(target instanceof MissileAPI)) {

            // Blast visuals
            float CoreExplosionRadius = 70f;
            float CoreExplosionDuration = 1f;
            float ExplosionRadius = 120f;
            float ExplosionDuration = 0.2f;
            float CoreGlowRadius = 200f;
            float CoreGlowDuration = 0.2f;

            engine.spawnExplosion(point, ZERO, CORE_EXPLOSION_COLOR, CoreExplosionRadius, CoreExplosionDuration);
            engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, ExplosionRadius, ExplosionDuration);
            engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_GLOW_COLOR);
            //Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, point, ZERO);

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","tahlan_ruffle_blast"),
                    point,
                    new Vector2f(),
                    new Vector2f(80,80),
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
                    new Vector2f(40,40),
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
                    new Vector2f(120,120),
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

        }
    }

}
