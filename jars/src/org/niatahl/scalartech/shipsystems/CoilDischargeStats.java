package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CoilDischargeStats extends BaseShipSystemScript {

    static final float DAMAGE = 200f;
    static final float COOLDOWN_MOD = 2f;
    static final float ARC_RANGE = 500f;

    private static final Color LIGHTNING_CORE_COLOR = new Color(255, 216, 190, 168);
    private static final Color LIGHTNING_FRINGE_COLOR = new Color(232, 116, 14, 200);

    boolean runOnce = false;

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        runOnce = false;
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (runOnce) {
            return;
        }
        runOnce = true;

        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        float arcDamage = DAMAGE;
        if (ship.getPhaseCloak().isCoolingDown()) arcDamage *= COOLDOWN_MOD;

        //Grab our firing ports
        List<WeaponSlotAPI> vents = new ArrayList<WeaponSlotAPI>();
        for (WeaponSlotAPI weaponSlotAPI : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            if (weaponSlotAPI.isSystemSlot()) {
                vents.add(weaponSlotAPI);
            }
        }

        //Fire an arc from each port, each picking target individually
        for (WeaponSlotAPI firingVent : vents) {

            CombatEntityAPI target = null;

            // Take ship's target if there is one and it is within range
            if (ship.getShipTarget() != null) {
                if (MathUtils.isWithinRange(ship, ship.getShipTarget(), ARC_RANGE)) {
                    target = ship.getShipTarget();
                }
            }

            // If not, we pick a nearby target at random.
            if (target == null) {
                List<CombatEntityAPI> targetList = CombatUtils.getEntitiesWithinRange(ship.getLocation(), ARC_RANGE);

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
                    } else if (MathUtils.getDistance(target, ship) > MathUtils.getDistance(potentialTarget, ship)) {
                        target = potentialTarget;
                    }
                }
            }

            if (target != null) {
                Global.getCombatEngine().spawnEmpArc(ship, firingVent.computePosition(ship), ship, target,
                        DamageType.ENERGY, //Damage type
                        arcDamage, //Damage
                        arcDamage, //Emp
                        100000f, //Max range
                        "tachyon_lance_emp_impact", //Impact sound
                        10f, // thickness of the lightning bolt
                        LIGHTNING_CORE_COLOR, //Central color
                        LIGHTNING_FRINGE_COLOR //Fringe Color
                );
            } else {
                Global.getCombatEngine().spawnEmpArc(ship,firingVent.computePosition(ship), ship, new SimpleEntity(MathUtils.getRandomPointInCircle(firingVent.computePosition(ship),ARC_RANGE/2f)),
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
