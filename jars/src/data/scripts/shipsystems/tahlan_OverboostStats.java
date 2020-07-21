package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static data.scripts.utils.tahlan_scalar_txt.txt;

public class tahlan_OverboostStats extends BaseShipSystemScript {

    private static final float SPEED_BOOST = 300f;

    private static Color OVERDRIVE_COLOR = new Color(0,255,255,40);
    private static Color ENGINE_COLOR = new Color(255,0,100);

    private static Color LIGHTNING_CORE_COLOR = new Color(135, 255, 247, 150);
    private static Color LIGHTNING_FRINGE_COLOR = new Color(24, 136, 144, 200);

    private IntervalUtil interval = new IntervalUtil(0.05f, 0.1f);

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

            if(Math.random()>0.25f){
                ship.addAfterimage(new Color(0, 255, 250,20), 0, 0, -ship.getVelocity().x, -ship.getVelocity().y, 5f, 0, 0, 1.2f*effectLevel, false, false, false);
            }

            interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (interval.intervalElapsed()) {

                CombatEntityAPI target = null;

                //Finds a target, in case we are going to overkill our current one
                List<CombatEntityAPI> targetList = CombatUtils.getEntitiesWithinRange(ship.getLocation(), 300f);

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
                    Global.getCombatEngine().spawnEmpArc(ship, shipengine.getLocation(), new SimpleEntity(ship.getLocation()), target,
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
                    Global.getCombatEngine().spawnEmpArc(ship, shipengine.getLocation(), new SimpleEntity(ship.getLocation()), new SimpleEntity(MathUtils.getRandomPointInCircle(ship.getLocation(),100f)),
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
