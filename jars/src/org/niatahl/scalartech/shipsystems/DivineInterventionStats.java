package org.niatahl.scalartech.shipsystems;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;


public class DivineInterventionStats extends BaseShipSystemScript {

    private static final Color FLICKER_COLOR = new Color(86, 125, 129, 131);
    private static final Color AFTERIMAGE_COLOR = new Color(101, 129, 128, 61);
    private Color color = new Color(137, 235, 255,255);

    private static Color LIGHTNING_CORE_COLOR = new Color(135, 255, 247, 150);
    private static Color LIGHTNING_FRINGE_COLOR = new Color(24, 136, 144, 200);

    public static final float MAX_TIME_MULT = 2f;

    private IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private IntervalUtil zapInterval = new IntervalUtil(0.05f, 0.05f);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        boolean player = false;
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        float TimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
        stats.getTimeMult().modifyMult(id, TimeMult);
        ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);

        float driftamount = engine.getElapsedInLastFrame();

        stats.getMaxTurnRate().modifyMult(id,5f);
        stats.getTurnAcceleration().modifyMult(id, 10f);

        if (state == State.IN) {

            ship.getMutableStats().getAcceleration().modifyFlat(id, 5000f);
            ship.getMutableStats().getDeceleration().modifyFlat(id, 5000f);

        } else if (state == State.ACTIVE) {

            interval.advance(engine.getElapsedInLastFrame());

            ship.setPhased(true);
            ship.setExtraAlphaMult(0.25f);
            ship.setApplyExtraAlphaToEngines(true);

            if (interval.intervalElapsed()) {
                // Sprite offset fuckery - Don't you love trigonometry?
                SpriteAPI sprite = ship.getSpriteAPI();
                float offsetX = sprite.getWidth() / 2 - sprite.getCenterX();
                float offsetY = sprite.getHeight() / 2 - sprite.getCenterY();

                float trueOffsetX = (float) FastTrig.cos(Math.toRadians(ship.getFacing() - 90f)) * offsetX - (float) FastTrig.sin(Math.toRadians(ship.getFacing() - 90f)) * offsetY;
                float trueOffsetY = (float) FastTrig.sin(Math.toRadians(ship.getFacing() - 90f)) * offsetX + (float) FastTrig.cos(Math.toRadians(ship.getFacing() - 90f)) * offsetY;

                Vector2f trueLocation = new Vector2f(ship.getLocation().getX() + trueOffsetX, ship.getLocation().getY() + trueOffsetY);

                MagicRender.battlespace(
                        Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()),
                        MathUtils.getRandomPointInCircle(trueLocation,MathUtils.getRandomNumberInRange(0f,20f)),
                        new Vector2f(0, 0),
                        new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
                        new Vector2f(0, 0),
                        ship.getFacing() - 90f,
                        0f,
                        AFTERIMAGE_COLOR,
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

            ship.setJitter(ship,FLICKER_COLOR,0.7f,10,25f,50f);

            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);

            float speed = ship.getVelocity().length();
            if (speed <= 0.1f) {
                ship.getVelocity().set(VectorUtils.getDirectionalVector(ship.getLocation(), ship.getVelocity()));
            }
            if (speed < 900f) {
                ship.getVelocity().normalise();
                ship.getVelocity().scale(speed + driftamount * 3600f);
            }

            zapInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (zapInterval.intervalElapsed()) {

                CombatEntityAPI target = null;

                //Finds a target, in case we are going to overkill our current one
                List<CombatEntityAPI> targetList = CombatUtils.getEntitiesWithinRange(ship.getLocation(), 800f);

                for (CombatEntityAPI potentialTarget : targetList) {
                    //Checks for dissallowed targets, and ignores them
                    if (!(potentialTarget instanceof ShipAPI) && !(potentialTarget instanceof MissileAPI)) {
                        continue;
                    }

                    if (potentialTarget.getOwner()==ship.getOwner()) {
                        continue;
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
                            null, //Impact sound
                            10f, // thickness of the lightning bolt
                            LIGHTNING_CORE_COLOR, //Central color
                            LIGHTNING_FRINGE_COLOR //Fringe Color
                    );
                } else {
                    Global.getCombatEngine().spawnEmpArc(ship, shipengine.getLocation(), ship, new SimpleEntity(MathUtils.getRandomPointInCircle(shipengine.getLocation(),100f)),
                            DamageType.ENERGY, //Damage type
                            100f, //Damage
                            200f, //Emp
                            100000f, //Max range
                            null, //Impact sound
                            10f, // thickness of the lightning bolt
                            LIGHTNING_CORE_COLOR, //Central color
                            LIGHTNING_FRINGE_COLOR //Fringe Color
                    );
                }
            }
        } else {
            float speed = ship.getVelocity().length();
            if (speed > ship.getMutableStats().getMaxSpeed().getModifiedValue()) {
                ship.getVelocity().normalise();
                ship.getVelocity().scale(speed - driftamount * 3600f);
            }
            stats.getMaxTurnRate().unmodify(id);
            stats.getTurnAcceleration().modifyMult(id,20f);
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);

        stats.getTimeMult().unmodify(id);

        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }


    public float getActiveOverride(ShipAPI ship) {
        return -1;
    }

    public float getInOverride(ShipAPI ship) {
        return -1;
    }

    public float getOutOverride(ShipAPI ship) {
        return -1;
    }

    public float getRegenOverride(ShipAPI ship) {
        return -1;
    }

    public int getUsesOverride(ShipAPI ship) {
        return -1;
    }
}


