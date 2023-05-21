package org.niatahl.scalartech.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class LimitReleaseStats extends BaseShipSystemScript {

    private static final float ROF_MULT = 2f;
    private static final float SPEED_BOOST = 50f;
    private static final float FLUX_MULT = 0.50f;
    public static final float HARD_FLUX_DISSIPATION_PERCENT = 25f;

    private static final Color OVERDRIVE_COLOR = new Color(0,255,255,40);
    private static final Color COOLDOWN_COLOR = new Color(255, 84, 89,50);
    private static final Color ENGINE_COLOR = new Color(255,0,100);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship;

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        ship.setJitterShields(false);

        if (state == State.OUT) {
            stats.getEnergyWeaponFluxCostMod().unmodify(id);
            stats.getEnergyRoFMult().modifyMult(id,1f/ROF_MULT);
            stats.getEnergyAmmoRegenMult().modifyMult(id,1f/ROF_MULT);
            //stats.getBallisticRoFMult().modifyMult(id,1f/ROF_MULT);
            //stats.getBallisticWeaponFluxCostMod().unmodify(id);
            stats.getMaxSpeed().modifyFlat(id,-SPEED_BOOST/2);
            stats.getAcceleration().unmodify(id);
            stats.getFluxDissipation().modifyMult(id,1f-FLUX_MULT/2);
            stats.getHardFluxDissipationFraction().unmodify(id);

            ship.setJitter(id,COOLDOWN_COLOR,0.2f+0.3f*effectLevel, 3, 10f);
            ship.setJitterUnder(id, COOLDOWN_COLOR, 0.2f+0.3f*effectLevel, 10, 10f);
            ship.getEngineController().extendFlame(id, -0.4f, -0.4f, -0.4f);

//            for (WeaponAPI w : ship.getAllWeapons()) {
//                //only bother with ammo regenerators
//
//                float reloadRate = w.getSpec().getAmmoPerSecond();
//                float nuCharge = reloadRate / ROF_MULT;
//                if (w.getType() == WeaponAPI.WeaponType.ENERGY && w.usesAmmo() && reloadRate > 0) {
//                    w.getAmmoTracker().setAmmoPerSecond(nuCharge);
//                }
//            }

        } else {
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f / (1f + (ROF_MULT - 1f) * effectLevel));
            stats.getEnergyRoFMult().modifyMult(id, 1f + (ROF_MULT - 1f) * effectLevel);
            stats.getEnergyAmmoRegenMult().modifyMult(id,ROF_MULT);
            //stats.getBallisticRoFMult().modifyMult(id, 1f + (ROF_MULT - 1f) * effectLevel);
            //stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f / (1f + (ROF_MULT - 1f) * effectLevel));
            stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST * effectLevel);
            stats.getAcceleration().modifyFlat(id, SPEED_BOOST * 2f * effectLevel);
            stats.getFluxDissipation().modifyMult(id, 1f + FLUX_MULT * effectLevel);
            stats.getHardFluxDissipationFraction().modifyFlat(id, HARD_FLUX_DISSIPATION_PERCENT * 0.01f);

            ship.setJitter(id,OVERDRIVE_COLOR,0.5f*effectLevel, 3, 10f);
            ship.setJitterUnder(id, OVERDRIVE_COLOR, 0.5f*effectLevel, 10, 10f);
            ship.getEngineController().extendFlame(id, 1.2f, 1.2f, 1.2f);
            ship.getEngineController().fadeToOtherColor(id, ENGINE_COLOR, null, effectLevel, 0.7f);

            if(Math.random()>0.25f){
                ship.addAfterimage(new Color(0, 255, 250,20), 0, 0, -ship.getVelocity().x, -ship.getVelocity().y, 5f, 0, 0, 1.2f*effectLevel, false, false, false);
            }

//            for (WeaponAPI w : ship.getAllWeapons()) {
//                //only bother with ammo regenerators
//
//                float reloadRate = w.getSpec().getAmmoPerSecond();
//                float nuCharge = reloadRate * ROF_MULT;
//                if (w.getType() == WeaponAPI.WeaponType.ENERGY && w.usesAmmo() && reloadRate > 0) {
//                    w.getAmmoTracker().setAmmoPerSecond(nuCharge);
//                }
//            }

        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

//        ShipAPI ship = null;
//
//        if (stats.getEntity() instanceof ShipAPI) {
//            ship = (ShipAPI) stats.getEntity();
//        } else {
//            return;
//        }

        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getEnergyAmmoRegenMult().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        stats.getHardFluxDissipationFraction().unmodify(id);

//        for (WeaponAPI w : ship.getAllWeapons()) {
//            //only bother with ammo regenerators
//
//            float reloadRate = w.getSpec().getAmmoPerSecond();
//            if (w.getType() == WeaponAPI.WeaponType.ENERGY && w.usesAmmo() && reloadRate > 0) {
//                w.getAmmoTracker().setAmmoPerSecond(reloadRate);
//            }
//        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            if (state == State.OUT) {
                return new StatusData(txt("limit_debuff"),true);
            } else {
                return new StatusData(txt("limit_buff"),false);
            }
        }
        return null;
    }
}
