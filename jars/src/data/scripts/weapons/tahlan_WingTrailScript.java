package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.plugins.MagicTrailPlugin;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class tahlan_WingTrailScript implements EveryFrameWeaponEffectPlugin {

    private IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.05f);
    private Float trailID = null;
    private SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "tahlan_trail_smooth");

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI ship = weapon.getShip();
        Float brightness = 0.3f+0.3f*ship.getSystem().getEffectLevel();

        effectInterval.advance(engine.getElapsedInLastFrame());
        if (effectInterval.intervalElapsed()) {
            if (trailID == null) {
                trailID = MagicTrailPlugin.getUniqueID();
            }
            MagicTrailPlugin.AddTrailMemberSimple(ship, trailID, trailSprite,
                    weapon.getLocation(),
                    0f,
                    weapon.getCurrAngle(),
                    30f,
                    15f,
                    Color.red,
                    brightness,
                    2f,
                    true,
                    ZERO);
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
