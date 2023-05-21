package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.plugins.MagicTrailPlugin;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class WingTrailScript implements EveryFrameWeaponEffectPlugin {

    private IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.05f);
    private Float trailID = null;
    private SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "tahlan_trail_smooth");

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused()) {
            return;
        }

        ShipAPI ship = weapon.getShip();

        if (ship.isHulk() || !ship.isAlive() || ship.isPiece()) {
            weapon.getAnimation().setFrame(0);
            return;
        }

        float brightness = 0.3f+0.3f*ship.getSystem().getEffectLevel();

        effectInterval.advance(engine.getElapsedInLastFrame());
        float angle = Misc.getAngleInDegrees(new Vector2f(ship.getVelocity()));
        if (effectInterval.intervalElapsed()) {
            if (trailID == null) {
                trailID = MagicTrailPlugin.getUniqueID();
            }
            MagicTrailPlugin.addTrailMemberSimple(ship,
                    trailID,
                    trailSprite,
                    weapon.getLocation(),
                    0f,
                    angle,
                    30f,
                    15f,
                    Color.red,
                    brightness,
                    0f,
                    1f,
                    2f,
                    true);
        }

        //Glows off in refit screen
        if (ship.getOriginalOwner() == -1) {
            brightness = 0f;
            trailID = null;
        }

        //Switches to the proper sprite
        if (brightness > 0) {
            weapon.getAnimation().setFrame(1);
        } else {
            weapon.getAnimation().setFrame(0);
        }

        Color colorToUse = new Color(1f, 0f, 0f, brightness);
        weapon.getSprite().setColor(colorToUse);
    }
}
