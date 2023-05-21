package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class CrinkleScript implements OnFireEffectPlugin {

    private static final Color PARTICLE_COLOR = new Color(63, 158, 215);
    private static final Color GLOW_COLOR = new Color(205, 234, 255, 50);

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        Global.getCombatEngine().spawnExplosion(projectile.getLocation(), new Vector2f(0f, 0f), PARTICLE_COLOR, 80f, 0.2f);
        engine.addSmoothParticle(projectile.getLocation(), ZERO, 100f, 0.7f, 0.05f, PARTICLE_COLOR);
        engine.addSmoothParticle(projectile.getLocation(), ZERO, 120f, 0.7f, 0.1f, GLOW_COLOR);

    }
}
