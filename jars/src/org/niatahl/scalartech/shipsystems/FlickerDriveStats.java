package org.niatahl.scalartech.shipsystems;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.niatahl.scalartech.ScalarModPlugin;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.util.Misc.ZERO;
import static org.niatahl.scalartech.utils.GraphicLibEffects.CustomRippleDistortion;


public class FlickerDriveStats extends BaseShipSystemScript {

    private static final Color FLICKER_COLOR = new Color(108, 127, 129, 131);
    public static final float MAX_TIME_MULT = 3f;
    public static final float MAX_MOBILITY_MULT = 2f;

    private static final Color LIGHTNING_CORE_COLOR = new Color(135, 255, 247, 150);
    private static final Color LIGHTNING_FRINGE_COLOR = new Color(24, 136, 144, 200);

    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private boolean runOnce = true;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        float EffectMult = 1f + (MAX_MOBILITY_MULT - 1f) * effectLevel;
        float TimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;

        ship.setExtraAlphaMult(1f-0.75f*effectLevel);
        ship.setApplyExtraAlphaToEngines(true);

        stats.getTimeMult().modifyMult(id,TimeMult);
        stats.getAcceleration().modifyMult(id, EffectMult);
        stats.getDeceleration().modifyMult(id, EffectMult);
        stats.getMaxSpeed().modifyMult(id, EffectMult);
        stats.getMaxTurnRate().modifyMult(id, EffectMult);
        stats.getTurnAcceleration().modifyMult(id, EffectMult);

        if (state == State.OUT) {

            stats.getMaxSpeed().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
            runOnce = true;

        } else if (state == State.ACTIVE) {

            ship.setPhased(true);
            ship.setJitter(ship,FLICKER_COLOR,0.7f,10,25f,50f);

            if (runOnce) {

                List<CombatEntityAPI> hitTargets = new ArrayList<CombatEntityAPI>();
                for (int x = 0; x < 20; x++) {

                    CombatEntityAPI target = null;

                    //Finds a target, in case we are going to overkill our current one
                    List<CombatEntityAPI> targetList = CombatUtils.getEntitiesWithinRange(ship.getLocation(), 600f);
                    List<CombatEntityAPI> validTargets = new ArrayList<CombatEntityAPI>();

                    for (CombatEntityAPI potentialTarget : targetList) {
                        if (hitTargets.contains(potentialTarget)) {
                            continue;
                        }

                        //Checks for dissallowed targets, and ignores them
                        if (!(potentialTarget instanceof ShipAPI) && !(potentialTarget instanceof DamagingProjectileAPI)) {
                            continue;
                        }

                        if (potentialTarget.getOwner() == ship.getOwner()) {
                            continue;
                        }

                        if (potentialTarget instanceof ShipAPI) {
                            if (((ShipAPI) potentialTarget).isPhased()) {
                                continue;
                            }
                        }

                        validTargets.add(potentialTarget);
                    }

                    //Choose a random vent port to send lightning from
                    ShipEngineControllerAPI.ShipEngineAPI shipengine = ship.getEngineController().getShipEngines().get(MathUtils.getRandomNumberInRange(0, ship.getEngineController().getShipEngines().size() - 1));

                    if (!validTargets.isEmpty()) {
                        target = validTargets.get(MathUtils.getRandomNumberInRange(0, validTargets.size() - 1));
                    }

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
                        if (target instanceof DamagingProjectileAPI && !(target instanceof MissileAPI)) {

                            engine.addHitParticle(target.getLocation(), ZERO, 100f, 1f, 0.1f, LIGHTNING_CORE_COLOR);
                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx","tahlan_ruffle_blast"),
                                    target.getLocation(),
                                    new Vector2f(),
                                    new Vector2f(40,40),
                                    new Vector2f(120,120),
                                    //angle,
                                    360*(float)Math.random(),
                                    10,
                                    LIGHTNING_FRINGE_COLOR,
                                    true,
                                    0.1f,
                                    0.0f,
                                    0.1f
                            );
                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx","tahlan_ruffle_blast"),
                                    target.getLocation(),
                                    new Vector2f(),
                                    new Vector2f(50,50),
                                    new Vector2f(100,100),
                                    //angle,
                                    360*(float)Math.random(),
                                    -10,
                                    LIGHTNING_CORE_COLOR,
                                    true,
                                    0.1f,
                                    0.0f,
                                    0.1f
                            );
                            MagicLensFlare.createSharpFlare(engine,ship,target.getLocation(),5f,100f,0f,LIGHTNING_CORE_COLOR,Color.white);


                            if (ScalarModPlugin.isGraphicsLibAvailable()) {
                                CustomRippleDistortion(target.getLocation(),ZERO,60,2f,false,0f,360f,0.5f,0f,0.2f,0.2f,0.4f,0f);
                            }

                            Global.getCombatEngine().removeEntity(target);
                            hitTargets.add(target);
                        }

                    } else {
                        Global.getCombatEngine().spawnEmpArc(ship, shipengine.getLocation(), ship, new SimpleEntity(MathUtils.getRandomPointInCircle(shipengine.getLocation(), 600f)),
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

                hitTargets.clear();
                runOnce = false;
            }

        } else {
            ship.setPhased(false);
        }


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


