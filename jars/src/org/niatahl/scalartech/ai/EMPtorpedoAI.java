package org.niatahl.scalartech.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Torpedo AI for a proximity-fused emp torpedo
 *
 * based on script by MesoTronik, with fusing logic by Nicke535
 * further modified by Nia
 */
public class EMPtorpedoAI extends BaseMissileAI {
    /* Configurations */
    private static final float ENGINE_DEAD_TIME_MAX = 0.5f;  // Max time until engine burn starts
    private static final float ENGINE_DEAD_TIME_MIN = 0.25f; // Min time until engine burn starts
    private static final float FIRE_INACCURACY = 3f; // Set-once for entire shot lifetime leading offset
    private static final float LEAD_GUIDANCE_FACTOR = 0.35f;
    private static final float LEAD_GUIDANCE_FACTOR_FROM_ECCM = 0.35f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.1f;
    private static final float WEAVE_FALLOFF_DISTANCE = 500f; // Weaving stops entirely at 0 distance
    private static final float WEAVE_SINE_A_AMPLITUDE = 15f; // Degrees offset
    private static final float WEAVE_SINE_A_PERIOD = 5f;
    private static final float WEAVE_SINE_B_AMPLITUDE = 20f; // Degrees offset
    private static final float WEAVE_SINE_B_PERIOD = 10f;
    private static final float PROXIMITY_FUSE_RANGE = 75f;
    private static final int NUM_ARCS = 10;
    private static final float ARC_DAMAGE = 300f;
    private static final float EXPLOSION_SIZE_OUTER = 200f;
    private static final float EXPLOSION_SIZE_INNER = 100f;
    private static final float EXPLOSION_DAMAGE_MAX = 2000f;
    private static final float EXPLOSION_DAMAGE_MIN = 1000f;
    private static final float EXPLOSION_DURATION = 0.25f;
    private static final float PARTICLE_DURATION = 0.5f;
    private static final int PARTICLE_COUNT = 25;
    private static final int PARTICLE_SIZE_MIN = 2;
    private static final int PARTICLE_SIZE_RANGE = 5;
    private static final float HEATGLOW_SIZE = 500f;
    private static final Color HEATGLOW_COLOR = new Color(0, 219, 255, 205);
    private static final float HEATGLOW_DURATION = 0.5f;
    private static final Color VFX_COLOR = new Color(0, 0, 0, 0);
    private static final String SOUND_ID = "tahlan_empblast";
    private static final float ARMING_TIME = 1f;

    private static final Color ARC_FRINGE_COLOR = new Color(53, 218, 255);
    private static final Color ARC_CORE_COLOR = new Color(142, 252, 205);

    /* Internal script variables */
    private static final Vector2f ZERO = new Vector2f();
    private boolean aspectLocked = true;
    private float engineDeadTime;
    private float timeAccum = 0f;
    private final float weaveSineAPhase;
    private final float weaveSineBPhase;
    private final float inaccuracy;
    protected final float eccmMult;

    public EMPtorpedoAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);

        weaveSineAPhase = (float) (Math.random() * Math.PI * 2.0);
        weaveSineBPhase = (float) (Math.random() * Math.PI * 2.0);
        engineDeadTime = MathUtils.getRandomNumberInRange(ENGINE_DEAD_TIME_MIN, ENGINE_DEAD_TIME_MAX);

        eccmMult = 0.5f; // How much ECCM affects FIRE_INACCURACY

        inaccuracy = MathUtils.getRandomNumberInRange(-FIRE_INACCURACY, FIRE_INACCURACY);
    }

    public float getInaccuracyAfterECCM() {
        float eccmEffectMult = 1;
        if (launchingShip != null) {
            eccmEffectMult = 1 - eccmMult * launchingShip.getMutableStats().getMissileGuidance().getModifiedValue();
        }
        if (eccmEffectMult < 0) {
            eccmEffectMult = 0;
        }

        return inaccuracy * eccmEffectMult;
    }

    @Override
    public void advance(float amount) {
        if (missile.isFizzling() || missile.isFading()) {
            return;
        }

        if (engineDeadTime > 0f) {
            engineDeadTime -= amount;
            return;
        }

        timeAccum += amount;

        if (!acquireTarget(amount)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float maxSpeed = missile.getMaxSpeed();
        float guidance = LEAD_GUIDANCE_FACTOR;
        if (missile.getSource() != null) {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * LEAD_GUIDANCE_FACTOR_FROM_ECCM;
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), missile.getVelocity().length(), target.getLocation(), target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float weaveSineA = WEAVE_SINE_A_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_A_PERIOD) + weaveSineAPhase);
        float weaveSineB = WEAVE_SINE_B_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_B_PERIOD) + weaveSineBPhase);
        float weaveOffset = (weaveSineA + weaveSineB) * Math.min(1f, distance / WEAVE_FALLOFF_DISTANCE);

        float angularDistance;
        if (aspectLocked) {
            angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                    MathUtils.clampAngle(VectorUtils.getAngle(missile.getLocation(), guidedTarget) + getInaccuracyAfterECCM() + weaveOffset));
        } else {
            angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                    MathUtils.clampAngle(VectorUtils.getAngle(missile.getLocation(), guidedTarget)));
        }
        float absDAng = Math.abs(angularDistance);

        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

        if (aspectLocked && absDAng > 60f) {
            aspectLocked = false;
        }

        if (!aspectLocked && absDAng <= 45f) {
            aspectLocked = true;
        }

        if (aspectLocked || missile.getVelocity().length() <= maxSpeed * 0.4f) {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }

        if (absDAng < 5) {
            float MFlightAng = VectorUtils.getAngle(ZERO, missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20) {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }

        //Runs fusing logic
        runFusingLogic();
    }

    private void explode(boolean shieldHit, ShipAPI fuseTarget) {
        // This destroys the missile
        Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 100f, DamageType.FRAGMENTATION, 0f, false, false, missile);

        // This spawns some custom vfx stacked with the "normal" ones done by spawnDamagingExplosion
        Global.getCombatEngine().spawnExplosion(missile.getLocation(), ZERO, VFX_COLOR, EXPLOSION_SIZE_INNER, EXPLOSION_DURATION);
        Global.getCombatEngine().addSmoothParticle(missile.getLocation(), ZERO, HEATGLOW_SIZE, 1f, HEATGLOW_DURATION, HEATGLOW_COLOR);

        DamagingExplosionSpec boom = new DamagingExplosionSpec(
                EXPLOSION_DURATION,
                EXPLOSION_SIZE_OUTER,
                EXPLOSION_SIZE_INNER,
                missile.getDamageAmount(),
                missile.getDamageAmount()/2f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                PARTICLE_SIZE_MIN,
                PARTICLE_SIZE_RANGE,
                PARTICLE_DURATION,
                PARTICLE_COUNT,
                VFX_COLOR,
                VFX_COLOR
        );
        boom.setDamageType(DamageType.ENERGY);
        boom.setShowGraphic(true);
        boom.setSoundSetId(SOUND_ID);

        Global.getCombatEngine().spawnDamagingExplosion(boom, missile.getSource(), missile.getLocation(), false);

        // Arcing stuff
        List<CombatEntityAPI> validTargets = new ArrayList<CombatEntityAPI>();
        for (CombatEntityAPI entityToTest : CombatUtils.getEntitiesWithinRange(missile.getLocation(), 250)) {
            if (entityToTest instanceof ShipAPI || entityToTest instanceof AsteroidAPI || entityToTest instanceof MissileAPI) {
                //Phased targets, and targets with no collision, are ignored
                if (entityToTest instanceof ShipAPI) {
                    if (((ShipAPI) entityToTest).isPhased()) {
                        continue;
                    }
                }
                if (entityToTest.getCollisionClass().equals(CollisionClass.NONE)) {
                    continue;
                }
                if (entityToTest.getOwner() == missile.getWeapon().getShip().getOwner()) {
                    continue;
                }

                validTargets.add(entityToTest);
            }
        }

        for (int x = 0; x < NUM_ARCS; x++) {
            Global.getCombatEngine().addNebulaParticle(
                    missile.getLocation(),
                    ZERO,
                    MathUtils.getRandomNumberInRange(100f,200f),
                    1.2f,
                    0.1f,
                    0.4f,
                    MathUtils.getRandomNumberInRange(3f,5f),
                    new Color(255,255,255,30)
            );
            //If we have no valid targets, zap a random point near us
            if (validTargets.isEmpty()) {
                validTargets.add(new SimpleEntity(MathUtils.getRandomPointInCircle(missile.getLocation(), 250)));
            }

            //And finally, fire at a random valid target
            CombatEntityAPI arcTarget = validTargets.get(MathUtils.getRandomNumberInRange(0, validTargets.size() - 1));

            //Arcs can pierce shields of the main target

            float bonusDamage = missile.getDamageAmount()/NUM_ARCS;
            if (shieldHit && arcTarget == fuseTarget) {
                float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.5f;
                pierceChance *= fuseTarget.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                boolean piercedShield = (float) Math.random() < pierceChance;


                if (piercedShield) {
                    Global.getCombatEngine().spawnEmpArcPierceShields(missile.getSource(), missile.getLocation(), missile, arcTarget,
                            DamageType.ENERGY, //Damage type
                            bonusDamage, //Damage
                            bonusDamage, //Emp
                            100000f, //Max range
                            "tachyon_lance_emp_impact", //Impact sound
                            10f, // thickness of the lightning bolt
                            ARC_CORE_COLOR, //Central color
                            ARC_FRINGE_COLOR //Fringe Color
                    );
                } else {
                        Global.getCombatEngine().spawnEmpArc(missile.getSource(), missile.getLocation(), missile, arcTarget,
                                DamageType.ENERGY, //Damage type
                                bonusDamage, //Damage
                                bonusDamage, //Emp
                                100000f, //Max range
                                "tachyon_lance_emp_impact", //Impact sound
                                10f, // thickness of the lightning bolt
                                ARC_CORE_COLOR, //Central color
                                ARC_FRINGE_COLOR //Fringe Color
                        );
                }
            } else {

                Global.getCombatEngine().spawnEmpArc(missile.getSource(), missile.getLocation(), missile, arcTarget,
                        DamageType.ENERGY, //Damage type
                        bonusDamage, //Damage
                        bonusDamage, //Emp
                        100000f, //Max range
                        "tachyon_lance_emp_impact", //Impact sound
                        10f, // thickness of the lightning bolt
                        ARC_CORE_COLOR, //Central color
                        ARC_FRINGE_COLOR //Fringe Color
                );

            }
        }
    }

    @Override
    protected boolean acquireTarget(float amount) {
        if (!isTargetValidAlternate(target)) {
            if (target instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) target;
                if (ship.isPhased() && ship.isAlive()) {
                    return false;
                }
            }
            setTarget(findBestTarget());
            if (target == null) {
                setTarget(findBestTargetAlternate());
            }
            if (target == null) {
                return false;
            }
        } else {
            if (!isTargetValid(target)) {
                if (target instanceof ShipAPI) {
                    ShipAPI ship = (ShipAPI) target;
                    if (ship.isPhased() && ship.isAlive()) {
                        return false;
                    }
                }
                CombatEntityAPI newTarget = findBestTarget();
                if (newTarget != null) {
                    target = newTarget;
                }
            }
        }
        return true;
    }

    // This is some bullshit weighted random picker that favors larger ships
    @Override
    protected ShipAPI findBestTarget() {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValid(tmp)) {
                mod = 0f;
            } else {
                switch (tmp.getHullSize()) {
                    default:
                    case FIGHTER:
                        mod = 1f;
                        break;
                    case FRIGATE:
                        mod = 10f;
                        break;
                    case DESTROYER:
                        mod = 50f;
                        break;
                    case CRUISER:
                        mod = 100f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 125f;
                        break;
                }
            }
            weight = (1500f / Math.max(MathUtils.getDistance(tmp, missile.getLocation()), 750f)) * mod;
            if (weight > bestWeight) {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    protected ShipAPI findBestTargetAlternate() {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValidAlternate(tmp)) {
                mod = 0f;
            } else {
                switch (tmp.getHullSize()) {
                    default:
                    case FIGHTER:
                        mod = 1f;
                        break;
                    case FRIGATE:
                        mod = 10f;
                        break;
                    case DESTROYER:
                        mod = 50f;
                        break;
                    case CRUISER:
                        mod = 100f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 125f;
                        break;
                }
            }
            weight = (1500f / Math.max(MathUtils.getDistance(tmp, missile.getLocation()), 750f)) * mod;
            if (weight > bestWeight) {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    @Override
    protected boolean isTargetValid(CombatEntityAPI target) {
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            if (ship.isFighter() || ship.isDrone()) {
                return false;
            }
        }
        return super.isTargetValid(target);
    }

    protected boolean isTargetValidAlternate(CombatEntityAPI target) {
        return super.isTargetValid(target);
    }


    //Fusing logic
    private void runFusingLogic() {
        //We can't detonate before we're armed
        if (timeAccum < ARMING_TIME || !missile.isArmed()) {
            return;
        }

        //Against non-ship targets, just run a *very* simple targeting check
        if (!(target instanceof ShipAPI)) {
            if (MathUtils.getDistance(missile.getLocation(), target.getLocation()) < PROXIMITY_FUSE_RANGE + target.getCollisionRadius()) {
                explode(false,null);
            }
        } else {
            ShipAPI targetShip = (ShipAPI) target;
            //First of all, we can't detonate without being close enough to the enemy's collision radius
            if (MathUtils.getDistance(missile.getLocation(), targetShip.getLocation()) > PROXIMITY_FUSE_RANGE + targetShip.getCollisionRadius()) {
                return;
            }

            //Then, check if the enemy has shields we could potentially impact
            boolean potShieldImpact = false;
            if (targetShip.getShield() != null
                    && (targetShip.getShield().getType() == ShieldAPI.ShieldType.FRONT || targetShip.getShield().getType() == ShieldAPI.ShieldType.OMNI)
                    && targetShip.getShield().isOn()) {
                potShieldImpact = true;
            }

            //If there was potential for a shield hit, check that first
            if (potShieldImpact) {
                //We cannot possibly hit a shield we're too far away from
                float distanceToShieldCenter = MathUtils.getDistance(targetShip.getShieldCenterEvenIfNoShield(), missile.getLocation());
                if (distanceToShieldCenter <= PROXIMITY_FUSE_RANGE + targetShip.getShieldRadiusEvenIfNoShield()) {
                    //If we're in the shield arc, we're in fuse range: detonate
                    if (targetShip.getShield().isWithinArc(missile.getLocation())) {
                        explode(true,targetShip);
                        return;
                    }

                    //Otherwise, we get the two points that are closest to the missile on each shield edge and check
                    // the distance to those points
                    Vector2f corner1 = MathUtils.getPoint(targetShip.getShieldCenterEvenIfNoShield(),
                            targetShip.getShield().getRadius(),
                            targetShip.getShield().getFacing() - (targetShip.getShield().getActiveArc() / 2f));
                    Vector2f closestPoint1 = Misc.closestPointOnSegmentToPoint(targetShip.getShieldCenterEvenIfNoShield(),
                            corner1, missile.getLocation());
                    Vector2f corner2 = MathUtils.getPoint(targetShip.getShieldCenterEvenIfNoShield(),
                            targetShip.getShield().getRadius(),
                            targetShip.getShield().getFacing() - (targetShip.getShield().getActiveArc() / 2f));
                    Vector2f closestPoint2 = Misc.closestPointOnSegmentToPoint(targetShip.getShieldCenterEvenIfNoShield(),
                            corner2, missile.getLocation());

                    if (MathUtils.getDistance(closestPoint1, missile.getLocation()) < PROXIMITY_FUSE_RANGE
                            || MathUtils.getDistance(closestPoint2, missile.getLocation()) < PROXIMITY_FUSE_RANGE) {
                        explode(true,targetShip);
                        return;
                    }
                }
            }

            //Check for hull impact, if we didn't trigger on the shield
            float distanceToBounds = MathUtils.getDistance(CollisionUtils.getNearestPointOnBounds(missile.getLocation(), targetShip),
                                                            missile.getLocation());
            if (distanceToBounds < PROXIMITY_FUSE_RANGE) {
                explode(false,targetShip);
            }
        }
    }
}
