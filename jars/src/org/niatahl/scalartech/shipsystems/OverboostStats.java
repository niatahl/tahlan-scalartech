package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class OverboostStats extends BaseShipSystemScript {

    private static final float SPEED_BOOST = 300f;

    private static final Color OVERDRIVE_COLOR = new Color(0,255,255,40);
    private static final Color ENGINE_COLOR = new Color(255,0,100);

    private static final Color LIGHTNING_CORE_COLOR = new Color(135, 255, 247, 150);
    private static final Color LIGHTNING_FRINGE_COLOR = new Color(24, 136, 144, 200);

    private final IntervalUtil interval = new IntervalUtil(0.05f, 0.1f);
    private final IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.05f);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = null;
        boolean player = false;

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        } else {
            return;
        }

        ship.setJitterShields(false);

        if (state == State.OUT) {

            stats.getMaxSpeed().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);

        } else {
            stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST * effectLevel);
            stats.getAcceleration().modifyFlat(id, SPEED_BOOST * 2f * effectLevel);
            stats.getDeceleration().modifyFlat(id, SPEED_BOOST * effectLevel);

            ship.setJitter(id,OVERDRIVE_COLOR,0.5f*effectLevel, 3, 10f);
            ship.setJitterUnder(id, OVERDRIVE_COLOR, 0.5f*effectLevel, 10, 10f);
            ship.getEngineController().extendFlame(id, 1.2f, 1.2f, 1.2f);
            ship.getEngineController().fadeToOtherColor(id, ENGINE_COLOR, null, effectLevel, 0.7f);

            //if(Math.random()>0.25f){
            //    ship.addAfterimage(new Color(0, 255, 250,20), 0, 0, -ship.getVelocity().x, -ship.getVelocity().y, 5f, 0, 0, 1.2f*effectLevel, false, false, false);
            //}

            effectInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (effectInterval.intervalElapsed()) {

                // Sprite offset fuckery - Don't you love trigonometry?
                SpriteAPI sprite = ship.getSpriteAPI();
                float offsetX = sprite.getWidth() / 2 - sprite.getCenterX();
                float offsetY = sprite.getHeight() / 2 - sprite.getCenterY();

                float trueOffsetX = (float) FastTrig.cos(Math.toRadians(ship.getFacing() - 90f)) * offsetX - (float) FastTrig.sin(Math.toRadians(ship.getFacing() - 90f)) * offsetY;
                float trueOffsetY = (float) FastTrig.sin(Math.toRadians(ship.getFacing() - 90f)) * offsetX + (float) FastTrig.cos(Math.toRadians(ship.getFacing() - 90f)) * offsetY;

                MagicRender.battlespace(
                        Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()),
                        new Vector2f(ship.getLocation().getX() + trueOffsetX, ship.getLocation().getY() + trueOffsetY),
                        new Vector2f(0, 0),
                        new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
                        new Vector2f(0, 0),
                        ship.getFacing() - 90f,
                        0f,
                        new Color(0, 255, 250,40),
                        true,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0.1f,
                        0.1f,
                        1f,
                        CombatEngineLayers.BELOW_SHIPS_LAYER);

            }

            interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (interval.intervalElapsed()) {

                CombatEntityAPI target = null;

                List<CombatEntityAPI> targetList = CombatUtils.getEntitiesWithinRange(ship.getLocation(), 500f);

                for (CombatEntityAPI potentialTarget : targetList) {
                    //Checks for dissallowed targets, and ignores them
                    if (!(potentialTarget instanceof ShipAPI) && !(potentialTarget instanceof MissileAPI)) {
                        continue;
                    }

                    if (potentialTarget.getOwner()==ship.getOwner()) {
                        continue;
                    }

                    if (potentialTarget instanceof ShipAPI) {
                        if ( ((ShipAPI) potentialTarget).isPhased() ) {
                            continue;
                        }
                    }

                    //If we found any applicable targets, pick the closest one
                    if (target == null) {
                        target = potentialTarget;
                    } else if (MathUtils.getDistance(target, ship) > MathUtils.getDistance(potentialTarget, ship)) {
                        target = potentialTarget;
                    }
                }

                //Choose a random vent port to send lightning from
                ShipEngineControllerAPI.ShipEngineAPI shipengine = ship.getEngineController().getShipEngines().get(MathUtils.getRandomNumberInRange(0,ship.getEngineController().getShipEngines().size()-1));

                if (target != null) {
                    Global.getCombatEngine().spawnEmpArc(ship, shipengine.getLocation(), ship, target,
                            DamageType.ENERGY, //Damage type
                            100f, //Damage
                            200f, //Emp
                            100000f, //Max range
                            "tachyon_lance_emp_impact", //Impact sound
                            10f, // thickness of the lightning bolt
                            LIGHTNING_CORE_COLOR, //Central color
                            LIGHTNING_FRINGE_COLOR //Fringe Color
                    );
                } else {
                    Global.getCombatEngine().spawnEmpArc(ship, MathUtils.getRandomPointInCircle(shipengine.getLocation(),200f), null, ship,
                            DamageType.ENERGY, //Damage type
                            0f, //Damage
                            0f, //Emp
                            100000f, //Max range
                            null, //Impact sound
                            10f, // thickness of the lightning bolt
                            LIGHTNING_CORE_COLOR, //Central color
                            LIGHTNING_FRINGE_COLOR //Fringe Color
                    );
                }
            }



        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("overboost"),true);
        }
        return null;
    }
}
