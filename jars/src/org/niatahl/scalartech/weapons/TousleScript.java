package org.niatahl.scalartech.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class TousleScript implements EveryFrameWeaponEffectPlugin {

    private static final Color PARTICLE_COLOR = new Color(124, 242, 255);
    private static final Color GLOW_COLOR = new Color(104, 216, 255, 50);
    private static final Color FLASH_COLOR = new Color(221, 255, 248);

    private static final int SPLIT_COUNT = 5;
    private static final float SPLIT_INACCURACY = 8f;
    private static final float SPLIT_SPEED_VARIATION = 0.1f;
    private static final String SPLIT_WEAPON_NAME = "tahlan_tousle_split";

    private boolean hasFiredThisCharge = false;

    private List<DamagingProjectileAPI> registeredProjectiles = new ArrayList<DamagingProjectileAPI>();


    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon == null) {
            return;
        }

        float chargelevel = weapon.getChargeLevel();

        if (hasFiredThisCharge && (chargelevel <= 0f || !weapon.isFiring())) {
            hasFiredThisCharge = false;
        }

        //Muzzle location calculation
        Vector2f point = new Vector2f();

        if (weapon.getSlot().isHardpoint()) {
            point.x = weapon.getSpec().getHardpointFireOffsets().get(0).x;
            point.y = weapon.getSpec().getHardpointFireOffsets().get(0).y;
        } else if (weapon.getSlot().isTurret()) {
            point.x = weapon.getSpec().getTurretFireOffsets().get(0).x;
            point.y = weapon.getSpec().getTurretFireOffsets().get(0).y;
        } else {
            point.x = weapon.getSpec().getHiddenFireOffsets().get(0).x;
            point.y = weapon.getSpec().getHiddenFireOffsets().get(0).y;
        }

        point = VectorUtils.rotate(point, weapon.getCurrAngle(), new Vector2f(0f, 0f));
        point.x += weapon.getLocation().x;
        point.y += weapon.getLocation().y;


        //Firing visuals
        if (chargelevel >= 1f && !hasFiredThisCharge) {
            hasFiredThisCharge = true;

            Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f, 0f), PARTICLE_COLOR, 40f, 0.2f);
            Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f, 0f), FLASH_COLOR, 20f, 0.2f);
            engine.addSmoothParticle(point, ZERO, 80f, 0.7f, 0.1f, PARTICLE_COLOR);
            engine.addSmoothParticle(point, ZERO, 120f, 0.7f, 1f, GLOW_COLOR);
            //engine.addHitParticle(point, ZERO, 400f, 1f, 0.05f, FLASH_COLOR);
        }


        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 100f)) {
            if (weapon == proj.getWeapon() && !registeredProjectiles.contains(proj) && !proj.getProjectileSpecId().contains(SPLIT_WEAPON_NAME)) {
                registeredProjectiles.add(proj);
                proj.getVelocity().x *= 0.5f;
                proj.getVelocity().y *= 0.5f;
                proj.getDamage().setType(DamageType.FRAGMENTATION);
            }
        }

        ShipAPI target = null;
        ShipAPI source = weapon.getShip();

        if(source.getWeaponGroupFor(weapon)!=null ){
            //WEAPON IN AUTOFIRE
            if(source.getWeaponGroupFor(weapon).isAutofiring()  //weapon group is autofiring
                    && source.getSelectedGroupAPI()!=source.getWeaponGroupFor(weapon)){ //weapon group is not the selected group
                target = source.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
            }
            else {
                target = source.getShipTarget();
            }
        }

        //List for cleaning up dead projectiles from memory
        List<DamagingProjectileAPI> cleanList = new ArrayList<>();

        //Splits shots that should be splitting
        for (DamagingProjectileAPI proj : registeredProjectiles) {
            //Calculates split range : hard-coded to match up with range properly
            Vector2f loc = proj.getLocation();
            float projAngle = proj.getFacing();
            float projDamage = proj.getBaseDamageAmount();

            float splitDuration = (weapon.getRange() / weapon.getProjectileSpeed());

            if (target != null) {
                if (target.getShield() != null) {
                    splitDuration = Math.min((MathUtils.getDistance(proj.getLocation(), target.getLocation()) - target.getShield().getRadius() / 2) / weapon.getProjectileSpeed(), splitDuration);
                } else {
                    splitDuration = Math.min(MathUtils.getDistance(proj.getLocation(), target.getLocation()) / weapon.getProjectileSpeed(), splitDuration);
                }
            }

                //Split once our duration has passed; spawn a bunch of shots
            if (proj.getElapsed() > splitDuration && !proj.didDamage()) {
                //Hide the explosion with some muzzle flash
                engine.addSmoothParticle(loc, ZERO, 100f, 0.5f, 0.1f, PARTICLE_COLOR);
                engine.addHitParticle(loc, ZERO, 50f, 0.5f, 0.25f, FLASH_COLOR);
                Global.getSoundPlayer().playSound("sabot_srm_split",1.2f,0.7f,loc,proj.getVelocity());

                //Actually spawn shots
                for (int i = 0; i < SPLIT_COUNT; i++) {
                    //Spawns the shot, with some inaccuracy
                    float angleOffset = MathUtils.getRandomNumberInRange(-SPLIT_INACCURACY / 2, SPLIT_INACCURACY / 2) + MathUtils.getRandomNumberInRange(-SPLIT_INACCURACY / 2, SPLIT_INACCURACY / 2);
                    DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(weapon.getShip(), weapon, SPLIT_WEAPON_NAME, loc, projAngle + angleOffset, new Vector2f(0, 0));
                    //Varies the speed slightly, for a more artillery-esque look
                    float rand = MathUtils.getRandomNumberInRange(1 - SPLIT_SPEED_VARIATION, 1 + SPLIT_SPEED_VARIATION);
                    newProj.getVelocity().x *= rand*2;
                    newProj.getVelocity().y *= rand*2;
                    //Splits up the damage
                    newProj.setDamageAmount(projDamage / (float) SPLIT_COUNT);
                    //Removes the original projectile
                    engine.removeEntity(proj);
                }
                cleanList.add(proj);
                continue;
            }


            //If this projectile is not loaded in memory, cleaning time!
            if (!engine.isEntityInPlay(proj)) {
                cleanList.add(proj);
            }
        }

        //Runs the cleaning
        for (DamagingProjectileAPI proj : cleanList) {
            registeredProjectiles.remove(proj);
        }


    }
}


