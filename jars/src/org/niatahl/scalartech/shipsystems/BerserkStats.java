package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.EnumSet;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class BerserkStats extends BaseShipSystemScript {

    public static final float SPEED_BOOST = 50f;
    public static final float DAMAGE_MULT = 0.33f;
    public static final float WEAPON_DAM_MULT = 1.5f;
    public static final float RANGE_MULT = 0.8f;

    private static final Color OVERDRIVE_COLOR = new Color(255, 63, 60,60);
    private static final Color COOLDOWN_COLOR = new Color(255, 58, 58,40);
    private static final Color ENGINE_COLOR = new Color(255, 42, 42);
    private static final Color GLOW_COLOR = new Color(255, 93, 47);
    private static final Color SMOKE_COLOR = new Color(87, 43, 42, 20);

    private IntervalUtil interval = new IntervalUtil(0.05f, 0.1f);

    boolean  runOnce = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = null;
        boolean player = false;

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        } else {
            return;
        }

        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down

            if (!runOnce) {
                ship.getFluxTracker().beginOverloadWithTotalBaseDuration(ship.getSystem().getChargeDownDur());
                runOnce = true;
            }

        } else {
            runOnce = false;
            stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST * effectLevel);
            stats.getAcceleration().modifyFlat(id, SPEED_BOOST * 2* effectLevel);
            stats.getDeceleration().modifyFlat(id, SPEED_BOOST * effectLevel);
            stats.getEmpDamageTakenMult().modifyMult(id, 1f-((1f-DAMAGE_MULT)*effectLevel));
            stats.getArmorDamageTakenMult().modifyMult(id, 1f-((1f-DAMAGE_MULT)*effectLevel));
            stats.getHullDamageTakenMult().modifyMult(id, 1f-((1f-DAMAGE_MULT)*effectLevel));
            stats.getEnergyWeaponDamageMult().modifyMult(id, WEAPON_DAM_MULT);
            stats.getBallisticWeaponDamageMult().modifyMult(id, WEAPON_DAM_MULT);
            stats.getEnergyWeaponRangeBonus().modifyMult(id, 1f-((1f-RANGE_MULT)*effectLevel));
            stats.getBallisticWeaponRangeBonus().modifyMult(id, 1f-((1f-RANGE_MULT)*effectLevel));

        }

        //Global.getSoundPlayer().playLoop("tahlan_berserk_loop",ship,0.5f+0.5f*effectLevel,5f,ship.getLocation(),ship.getVelocity());

        ship.setJitter(id,COOLDOWN_COLOR,0.8f*effectLevel, 2, 10f);
        ship.setJitterUnder(id, OVERDRIVE_COLOR, 1f*effectLevel, 10, 15f);
        ship.getEngineController().extendFlame(id, 1.1f, 1.1f, 1.1f);
        ship.getEngineController().fadeToOtherColor(id, ENGINE_COLOR, SMOKE_COLOR, effectLevel, 0.9f);

        EnumSet<WeaponAPI.WeaponType> WEAPON_TYPES = EnumSet.of(WeaponAPI.WeaponType.BALLISTIC,WeaponAPI.WeaponType.ENERGY);
        ship.setWeaponGlow(0.5f*effectLevel, GLOW_COLOR, WEAPON_TYPES);


        if(Math.random()>0.5f){
            ship.addAfterimage(COOLDOWN_COLOR, 0, 0, -ship.getVelocity().x, -ship.getVelocity().y, 10f, 0, 0, 1.5f*effectLevel, false, false, false);
        }

        //Choose a random vent port to send lightning from
        ShipEngineControllerAPI.ShipEngineAPI shipengine = ship.getEngineController().getShipEngines().get(MathUtils.getRandomNumberInRange(0,ship.getEngineController().getShipEngines().size()-1));

        interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if (interval.intervalElapsed()) {
            Global.getCombatEngine().spawnEmpArc(ship, MathUtils.getRandomPointInCircle(shipengine.getLocation(), 200f), null, ship,
                    DamageType.ENERGY, //Damage type
                    0f, //Damage
                    0f, //Emp
                    100000f, //Max range
                    null, //Impact sound
                    8f, // thickness of the lightning bolt
                    ENGINE_COLOR, //Central color
                    OVERDRIVE_COLOR //Fringe Color
            );
        }
    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        boolean player = false;

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        } else {
            return;
        }

        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getBallisticWeaponDamageMult().unmodify(id);
        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);

        EnumSet<WeaponAPI.WeaponType> WEAPON_TYPES = EnumSet.of(WeaponAPI.WeaponType.BALLISTIC,WeaponAPI.WeaponType.ENERGY);
        ship.setWeaponGlow(0f, GLOW_COLOR, WEAPON_TYPES);

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("berserk_def") + (int)((1f-DAMAGE_MULT)*100f) + txt("%"), false);
        }
        if (index == 1) {
            return new StatusData(txt("berserk_speed") + (int)SPEED_BOOST, false);
        }
        if (index == 2) {
            return new StatusData(txt("berserk_dam") + (int)((WEAPON_DAM_MULT-1f)*100f) + txt("%"), false);
        }
        if (index == 3) {
            return new StatusData(txt("berserk_range") + (int)((1f-RANGE_MULT)*100f) + txt("%"), false);
        }
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

}


