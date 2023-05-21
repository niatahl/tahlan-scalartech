//By Nicke535, spawns several wings of fighters to attack a target
package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.mission.FleetSide;
import org.niatahl.scalartech.ScalarModPlugin;
import org.magiclib.util.MagicLensFlare;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.util.Misc.ZERO;
import static org.niatahl.scalartech.utils.GraphicLibEffects.CustomRippleDistortion;

public class SwarmerStrikeStats extends BaseShipSystemScript {
    private final static int    WINGS_TO_DEPLOY = 2;
    private final static float  WING_SPAWN_DISTANCE_MIN = 600f;
    private final static float  WING_SPAWN_DISTANCE_MAX = 700f;
    private final static float  WING_SPAWN_ANGLE_DIFF = 180f;
    private final static float  MAX_RANGE = 10000f;

    private boolean hasDeployed = false;
    private List<ShipAPI> leadersToDespawn = new ArrayList<ShipAPI>();

    //Sounds for teleporting in/out
    private static final String TELEPORT_IN_SOUND = "system_phase_teleporter";
    private static final String TELEPORT_OUT_SOUND = "system_phase_teleporter";

    //Some basic teleport-flash config. More detailed config can be scripted in the function near the script's bottom
    private static final Color BASIC_FLASH_COLOR = new Color(84, 255, 218, 200);
    private static final Color BASIC_GLOW_COLOR = new Color(80, 161, 255, 200);

    //Can't use our system if we have no valid target, or our target is out of range
    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (ship.getShipTarget() == null || ship.getShipTarget().getOwner() == ship.getOwner()) {
            return false;
        }
        if (MathUtils.getDistance(ship.getShipTarget(), ship.getLocation()) > MAX_RANGE) {
            return false;
        }
        //On fallthrough, use default implementation
        return super.isUsable(system, ship);
    }

    //Can't use our system if we have no valid target, or our target is out of range
    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (ship.getShipTarget() == null) {
            return "NO TARGET";
        }
        if (ship.getShipTarget().getOwner() == ship.getOwner()) {
            return "INVALID TARGET";
        }
        if (MathUtils.getDistance(ship.getShipTarget(), ship.getLocation()) > MAX_RANGE) {
            return "OUT OF RANGE";
        }
        //On fallthrough, use default implementation
        return super.getInfoText(system, ship);
    }

    //Main apply loop
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        //Don't run when paused
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        //Ensures we have a ship
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        //If our system is on, and we haven't spawned a wing yet, spawn wings
        if (!hasDeployed && effectLevel >= 1f) {
            hasDeployed = true;

            //Dumb way to find the correct fleetside
            FleetSide rightFleetSide = FleetSide.ENEMY;
            if (Global.getCombatEngine().getFleetManager(ship.getOwner()) == Global.getCombatEngine().getFleetManager(FleetSide.PLAYER)) {
                rightFleetSide = FleetSide.PLAYER;
            }

            //Suppresses extra messages until we are done with deployment
            Global.getCombatEngine().getFleetManager(rightFleetSide).setSuppressDeploymentMessages(true);

            //Spawns several wings around the enemy target
            for (int i = 0; i < WINGS_TO_DEPLOY; i++) {
                //Gets a random location to spawn the wing
                Vector2f loc = MathUtils.getPointOnCircumference(ship.getShipTarget().getLocation(), ship.getShipTarget().getCollisionRadius() + MathUtils.getRandomNumberInRange(WING_SPAWN_DISTANCE_MIN, WING_SPAWN_DISTANCE_MAX),
                        VectorUtils.getAngle(ship.getShipTarget().getLocation(), ship.getLocation()) + MathUtils.getRandomNumberInRange(-WING_SPAWN_ANGLE_DIFF, WING_SPAWN_ANGLE_DIFF));
                //Gets the angle to the target from our location
                float facing = VectorUtils.getAngle(loc, ship.getShipTarget().getLocation());

                //Spawns the wing, sets correct variables and registers its leader in our list
                ShipAPI wingLeader = CombatUtils.spawnShipOrWingDirectly("tahlan_thread_swarmer_wing", FleetMemberType.FIGHTER_WING, rightFleetSide, 1f, loc, facing);
                //wingLeader.setShipTarget(ship.getShipTarget());
                if (ship.isAlly()) {
                    wingLeader.setAlly(true);
                }
                leadersToDespawn.add(wingLeader);

                for ( ShipAPI fighter : wingLeader.getWing().getWingMembers() ) {
                    spawnTeleportFlash(fighter,false);
                }
            }
            Global.getCombatEngine().getFleetManager(rightFleetSide).setSuppressDeploymentMessages(false);
        }

        //If our system is off, run unapply() once
        if (effectLevel <= 0f && hasDeployed) {
            unapply(stats, id);
        }
    }

    //Fixes variables and removes fighters
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

        hasDeployed = false;

        //Fighter removal code
        List<ShipAPI> removals = new ArrayList<ShipAPI>();
        for (ShipAPI leader : leadersToDespawn) {
            removals.add(leader);
        }
        for (ShipAPI leader : removals) {
            for ( ShipAPI fighter : leader.getWing().getWingMembers() ) {
                spawnTeleportFlash(fighter,true);
                Global.getCombatEngine().removeEntity(fighter);
            }
            //Global.getCombatEngine().removeEntity(leader);
        }

        leadersToDespawn.clear();
        removals.clear();
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0 && effectLevel > 0f) {
            return new StatusData("deploying nullspace drones", false);
        }
        return null;
    }

    //The "teleport flash" of a fighter
    //      Anything slapped in here will be spawned as visuals: go ham
    //      Also handles sounds
    private void spawnTeleportFlash(ShipAPI fighter, boolean isLanding) {
        //Sounds, based on if we're landing or not
        if (isLanding) {
            Global.getSoundPlayer().playSound(TELEPORT_OUT_SOUND, 1.2f, 0.3f, fighter.getLocation(), new Vector2f(0f, 0f));
        } else {
            Global.getSoundPlayer().playSound(TELEPORT_IN_SOUND, 1.2f, 0.3f, fighter.getLocation(), new Vector2f(0f, 0f));
        }

        CombatEngineAPI engine = Global.getCombatEngine();
        //Only spawn visuals when on-screen
        if (Global.getCombatEngine().getViewport().isNearViewport(fighter.getLocation(), 500f)) {
            MagicLensFlare.createSharpFlare(engine,fighter,fighter.getLocation(),5f,100f,0f,BASIC_FLASH_COLOR,Color.white);
            engine.addSmoothParticle(fighter.getLocation(), ZERO, 100f, 0.7f, 0.1f, BASIC_FLASH_COLOR);
            engine.addSmoothParticle(fighter.getLocation(), ZERO, 150f, 0.7f, 1f, BASIC_GLOW_COLOR);
            engine.addHitParticle(fighter.getLocation(), ZERO, 200f, 1f, 0.05f, Color.white);

            if (ScalarModPlugin.isGraphicsLibAvailable()) {
                CustomRippleDistortion(fighter.getLocation(),ZERO,60,2f,false,0f,360f,0.5f,0f,0.2f,0.2f,0.4f,0f);
            }
        }
    }
}