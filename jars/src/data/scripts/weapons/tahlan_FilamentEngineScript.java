package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class tahlan_FilamentEngineScript implements EveryFrameWeaponEffectPlugin {
    private static final float[] COLOR_NORMAL = {255f / 255f, 200f / 255f, 200f / 255f};
    private static final float MAX_OPACITY = 0.9f;

    private float FLAME_THROTTLE=1;

    private final IntervalUtil timer = new IntervalUtil(0.03f,0.05f);

    private boolean runOnce = false;
    private float LENGTH;
    private float WIDTH;

    private ShipEngineControllerAPI.ShipEngineAPI thruster;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }


        if (!runOnce) {
            weapon.getAnimation().setFrame(1);
            LENGTH = weapon.getSprite().getHeight();
            WIDTH = weapon.getSprite().getWidth();
            weapon.getAnimation().setFrame(0);
            runOnce = true;
        }


        ShipAPI SHIP = weapon.getShip();
        ShipEngineControllerAPI ENGINES = SHIP.getEngineController();

        ShipAPI ship = weapon.getShip();
        if (ship == null) {
            return;
        }

        for(ShipEngineControllerAPI.ShipEngineAPI e : SHIP.getEngineController().getShipEngines()){
            if(MathUtils.isWithinRange(e.getLocation(),weapon.getLocation(),2)){
                thruster = e;
            }
        }

        float currentBrightness = 1f;

        if (thruster != null) {
            if (thruster.isDisabled()) {
                currentBrightness = 0f;
            }
        }

        //A piece should never have glowing lights
        if (ship.isPiece() || !ship.isAlive() || ship.isPhased()) {
            currentBrightness = 0f;
        }

        //Glows off in refit screen
        if (ship.getOriginalOwner() == -1) {
            currentBrightness = 0f;
        }

        //Now, set the color to the one we want, and include opacity
        Color colorToUse = new Color(COLOR_NORMAL[0], COLOR_NORMAL[1], COLOR_NORMAL[2], currentBrightness * MAX_OPACITY);

        //Switches to the proper sprite
        if (currentBrightness > 0) {
            weapon.getAnimation().setFrame(1);
        } else {
            weapon.getAnimation().setFrame(0);
        }

        //And finally actually apply the color
        weapon.getSprite().setColor(colorToUse);


        timer.advance(amount);
        if(timer.intervalElapsed() && currentBrightness > 0) {
            //check the current behavior of the ship
            float throttle = 1;
            if (ENGINES.isFlamedOut() || ENGINES.isFlamingOut()) {
                throttle = 0;
            } else if (ENGINES.isAccelerating()) {
                throttle = 1;
            } else if (ENGINES.isAcceleratingBackwards()
                    || ENGINES.isDecelerating()
                    || ENGINES.isStrafingLeft()
                    || ENGINES.isStrafingRight()) {
                throttle = 0.66f;
            } else {
                throttle = 0.25f;
            }


            //lightly smooth out the flame behavior
            float offsetFlame = throttle -FLAME_THROTTLE;
            if(Math.abs(offsetFlame)<0.05f){
                FLAME_THROTTLE = throttle;
            } else {
                FLAME_THROTTLE = FLAME_THROTTLE+(offsetFlame)/10;
            }

            float sizeScale = 1f;
            switch (ship.getHullSize()) {
                case FRIGATE:
                    sizeScale = 0.3f;
                    break;
                case DESTROYER:
                    sizeScale = 0.5f;
                    break;
                case CRUISER:
                    sizeScale = 0.7f;
            }
            if (weapon.getShip().getHullSpec().getHullId().contains("tahlan_gown") || weapon.getShip().getHullSpec().getHullId().contains("tahlan_skye")) {
                sizeScale = 0.7f;
            }

            weapon.getSprite().setHeight(sizeScale*LENGTH*(1f - FLAME_THROTTLE/4));
            weapon.getSprite().setWidth(sizeScale*WIDTH*(1f - FLAME_THROTTLE/4));

            weapon.getSprite().setCenterY(weapon.getSprite().getHeight()/2-5f*FLAME_THROTTLE*sizeScale);
            weapon.getSprite().setCenterX(weapon.getSprite().getWidth()/2);
        }
    }
}