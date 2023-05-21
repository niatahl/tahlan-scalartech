//Written the eve before the tournament round it was needed in due to terrible decisions and lack of foresight
//I want to sleep
//Kudos to DR for helping me figure out some of this stuff
//It's 3:30 in the morning and I just want to sleep
//Dreadnought go BRRRRRRR

package org.niatahl.scalartech.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class tahlan_BerserkAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;
    private IntervalUtil tracker = new IntervalUtil(0.5F, 1.0F);
    private float engageRange = 100f;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;

        for (WeaponAPI weapon : ship.getUsableWeapons()) {
            if (weapon.getType() == WeaponAPI.WeaponType.MISSILE) {
                continue;
            }
            if (weapon.getRange() > engageRange && weapon.getSize() == WeaponAPI.WeaponSize.LARGE) {
                engageRange = weapon.getRange();
            }
        }
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (!engine.isPaused() && !system.isActive()) {
            tracker.advance(amount);
            if (tracker.intervalElapsed()) {

                // We find our movement target here. Sort of
                CombatEntityAPI immediateTarget;
                if (flags.getCustom(AIFlags.MANEUVER_TARGET) instanceof CombatEntityAPI) {
                    immediateTarget = (CombatEntityAPI) flags.getCustom(AIFlags.MANEUVER_TARGET);
                } else {
                    immediateTarget = ship.getShipTarget();
                }

                CombatEntityAPI actualTarget = immediateTarget;
                if (immediateTarget != null && target != null) {
                    if (MathUtils.getDistance(ship, target) > MathUtils.getDistance(ship, immediateTarget)) {
                        actualTarget = target;
                    }
                }

                // Check for possible threats as we don't want to gimp our guns around enemies or even crash into them.
                List<ShipAPI> threats = CombatUtils.getShipsWithinRange(ship.getLocation(), engageRange * 2f);

                ShipAPI closestThreat = null;
                if (!threats.isEmpty()) {
                    for (ShipAPI threat : threats) {
                        if (threat.getOwner() != ship.getOwner()) { //Forgot this at first and the ship was really scared of itself there
                            if (closestThreat == null) {
                                closestThreat = threat;
                            } else if (MathUtils.getDistance(ship, closestThreat) > MathUtils.getDistance(ship, threat) &&
                                    (threat.isCruiser() || threat.isCapital())) { //We don't care about small stuff
                                closestThreat = threat;
                            }
                        }
                    }
                }

                // Calculate actual ranges
                float targetRange = engageRange+100f;
                float threatRange = engageRange+100f;
                if (actualTarget != null) {
                    targetRange = MathUtils.getDistance(ship,actualTarget);
                }
                if (closestThreat != null) {
                    threatRange = MathUtils.getDistance(ship,closestThreat);
                }

                /* Glorious debug code without which I would've hammered my head against a wall for another 3 hours
                Global.getCombatEngine().maintainStatusForPlayerShip(this.getClass().getName() + "_TOOLTIP1",
                        "graphics/icons/hullsys/high_energy_focus.png", "engage range",
                        "" + (int)engageRange, false);

                Global.getCombatEngine().maintainStatusForPlayerShip(this.getClass().getName() + "_TOOLTIP2",
                        "graphics/icons/hullsys/high_energy_focus.png", "target range",
                        "" + (int)targetRange, false);

                Global.getCombatEngine().maintainStatusForPlayerShip(this.getClass().getName() + "_TOOLTIP3",
                        "graphics/icons/hullsys/high_energy_focus.png", "threat range",
                        "" + (int)threatRange, false);
                */

                //Actual decision making

                    if ((targetRange < engageRange * 0.9f || threatRange < engageRange * 0.5f)
                            &&
                            ( flags.hasFlag(AIFlags.PURSUING) || flags.hasFlag(AIFlags.HARASS_MOVE_IN) || flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER))
                            &&
                            !flags.hasFlag(AIFlags.KEEP_SHIELDS_ON)) {
                        ship.useSystem();
                    }



            }


        }
    }
}
