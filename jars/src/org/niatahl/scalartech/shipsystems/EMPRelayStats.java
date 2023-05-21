package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EMPRelayStats extends BaseShipSystemScript {

    private static final Object KEY_JITTER = new Object();
    private static final Color JITTER_UNDER_COLOR = new Color(47, 218, 255, 125);
    private static final Color JITTER_COLOR = new Color(0, 197, 255, 75);

    private static final Color LIGHTNING_CORE_COLOR = new Color(135, 255, 247, 150);
    private static final Color LIGHTNING_FRINGE_COLOR = new Color(24, 136, 144, 200);

    private final IntervalUtil interval = new IntervalUtil(0.05f, 0.1f);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }


        if (effectLevel > 0) {
            float maxRangeBonus = 5f;
            float jitterRangeBonus = effectLevel * maxRangeBonus;

            List<ShipAPI> fighters = getFighters(ship);
            ShipAPI fighter;

            if (!fighters.isEmpty()) {
                fighter = fighters.get(MathUtils.getRandomNumberInRange(0, fighters.size() - 1));

                //for (ShipAPI fighter : getFighters(ship)) {
                //if (fighter.isHulk()) continue;
                //if (Math.random() > 0.1) continue;

                interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
                if (interval.intervalElapsed()) {

                    CombatEntityAPI target = null;

                    //Finds a target, in case we are going to overkill our current one
                    List<CombatEntityAPI> targetList = CombatUtils.getEntitiesWithinRange(fighter.getLocation(), 500f);

                    for (CombatEntityAPI potentialTarget : targetList) {
                        //Checks for dissallowed targets, and ignores them
                        if (!(potentialTarget instanceof ShipAPI) && !(potentialTarget instanceof MissileAPI)) {
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

                        //If we found any applicable targets, pick the closest one
                        if (target == null) {
                            target = potentialTarget;
                        } else if (MathUtils.getDistance(target, fighter) > MathUtils.getDistance(potentialTarget, fighter)) {
                            target = potentialTarget;
                        }
                    }

                    if (target != null) {
                        Global.getCombatEngine().spawnEmpArc(fighter, fighter.getLocation(), fighter, target,
                                DamageType.FRAGMENTATION, //Damage type
                                100f, //Damage
                                200f, //Emp
                                100000f, //Max range
                                null, //Impact sound
                                10f, // thickness of the lightning bolt
                                LIGHTNING_CORE_COLOR, //Central color
                                LIGHTNING_FRINGE_COLOR //Fringe Color
                        );
                    } else {
                        Global.getCombatEngine().spawnEmpArc(fighter, fighter.getLocation(), fighter, new SimpleEntity(MathUtils.getRandomPointInCircle(fighter.getLocation(), 100f)),
                                DamageType.FRAGMENTATION, //Damage type
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

                fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, effectLevel, 5, 0f, jitterRangeBonus);
                fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, effectLevel, 2, 0f, 0 + jitterRangeBonus * 1.2f);
                Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
            }
        }
    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) continue;
            if (ship.getWing() == null) continue;
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            MutableShipStatsAPI fStats = fighter.getMutableStats();
        }

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("EMP relay active", false);
        }
        return null;
    }
}
