package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

import static data.scripts.utils.tahlan_scalar_txt.txt;

public class tahlan_BerserkStats extends BaseShipSystemScript {

    public static final float SPEED_BOOST = 50f;
    public static final float DAMAGE_MULT = 0.33f;
    public static final float WEAPON_DAM_MULT = 1.5f;
    public static final float RANGE_MULT = 0.8f;

    private static final Color OVERDRIVE_COLOR = new Color(255, 84, 89,60);
    private static final Color COOLDOWN_COLOR = new Color(255, 84, 89,40);
    private static final Color ENGINE_COLOR = new Color(255, 65, 65);

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
                ship.getFluxTracker().beginOverloadWithTotalBaseDuration(2f);
                runOnce = true;
            }
        } else {
            runOnce = false;
            stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST * effectLevel);
            stats.getAcceleration().modifyFlat(id, SPEED_BOOST * 2* effectLevel);
            stats.getDeceleration().modifyFlat(id, SPEED_BOOST * effectLevel);
            stats.getEmpDamageTakenMult().modifyMult(id, DAMAGE_MULT);
            stats.getArmorDamageTakenMult().modifyMult(id, DAMAGE_MULT);
            stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_MULT);
            stats.getEnergyWeaponDamageMult().modifyMult(id, WEAPON_DAM_MULT);
            stats.getBallisticWeaponDamageMult().modifyMult(id, WEAPON_DAM_MULT);
            stats.getEnergyWeaponRangeBonus().modifyMult(id, RANGE_MULT);
            stats.getBallisticWeaponRangeBonus().modifyMult(id, RANGE_MULT);
        }

        ship.setJitter(id,COOLDOWN_COLOR,0.8f*effectLevel, 2, 6f);
        ship.setJitterUnder(id, OVERDRIVE_COLOR, 1f*effectLevel, 10, 8f);
        ship.getEngineController().extendFlame(id, 1.2f, 1.2f, 1.2f);
        ship.getEngineController().fadeToOtherColor(id, ENGINE_COLOR, null, effectLevel, 0.9f);

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


