package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.niatahl.scalartech.ScalarModPlugin;
import org.magiclib.util.MagicRender;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static org.niatahl.scalartech.utils.GraphicLibEffects.CustomRippleDistortion;

public class TineolaOnHitEffect implements OnHitEffectPlugin {

    private static final Color CORE_EXPLOSION_COLOR = new Color(255, 156, 197, 255);
    private static final Color CORE_GLOW_COLOR = new Color(241, 213, 220, 150);
    private static final Color EXPLOSION_COLOR = new Color(243, 22, 80, 173);
    private static final Color FLASH_GLOW_COLOR = new Color(240, 215, 241, 200);
    private static final Color GLOW_COLOR = new Color(223, 172, 255, 50);
    private static final String SOUND_ID = "tahlan_tineola_blast";
    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile.didDamage() && !(target instanceof MissileAPI)) {

            // Blast visuals
            float CoreExplosionRadius = 150f;
            float CoreExplosionDuration = 1f;
            float ExplosionRadius = 300f;
            float ExplosionDuration = 1f;
            float CoreGlowRadius = 400f;
            float CoreGlowDuration = 1f;
            float GlowRadius = 400f;
            float GlowDuration = 1f;
            float FlashGlowRadius = 600f;
            float FlashGlowDuration = 0.05f;

            Global.getSoundPlayer().playSound(SOUND_ID,1f,2f,projectile.getLocation(),new Vector2f(0,0));

            engine.spawnExplosion(point, ZERO, CORE_EXPLOSION_COLOR, CoreExplosionRadius, CoreExplosionDuration);
            engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, ExplosionRadius, ExplosionDuration);
            engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_GLOW_COLOR);
            engine.addSmoothParticle(point, ZERO, GlowRadius, 1f, GlowDuration, GLOW_COLOR);
            engine.addHitParticle(point, ZERO, FlashGlowRadius, 1f, FlashGlowDuration, FLASH_GLOW_COLOR);


            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","tahlan_tineola_blast1"),
                    point,
                    new Vector2f(),
                    new Vector2f(220,220),
                    new Vector2f(150,150),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(236, 31, 151,100),
                    true,
                    0,
                    0.5f,
                    1.5f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","tahlan_tineola_blast2"),
                    point,
                    new Vector2f(),
                    new Vector2f(150,150),
                    new Vector2f(100,100),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(143, 15, 60,100),
                    true,
                    0.1f,
                    0.6f,
                    1.1f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","tahlan_tineola_blast2"),
                    point,
                    new Vector2f(),
                    new Vector2f(200,200),
                    new Vector2f(130,130),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(42, 174, 245,100),
                    true,
                    0.2f,
                    0.4f,
                    1.2f
            );
            if (ScalarModPlugin.isGraphicsLibAvailable()) {
                CustomRippleDistortion(point,ZERO,400,3f,false,0f,360f,0.5f,0f,0.15f,0.15f,0.4f,0f);
            }

        }
    }
}
